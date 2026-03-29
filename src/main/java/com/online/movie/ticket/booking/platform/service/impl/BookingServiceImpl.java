package com.online.movie.ticket.booking.platform.service.impl;

import com.online.movie.ticket.booking.platform.dto.BookingRequestDTO;
import com.online.movie.ticket.booking.platform.dto.BookingResponseDTO;
import com.online.movie.ticket.booking.platform.entity.*;
import com.online.movie.ticket.booking.platform.exception.*;
import com.online.movie.ticket.booking.platform.mapper.EntityMapper;
import com.online.movie.ticket.booking.platform.repository.*;
import com.online.movie.ticket.booking.platform.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of BookingService providing ticket booking functionality.
 *
 * WRITE SCENARIO IMPLEMENTATION:
 * - Create booking with seat reservation
 * - Handle concurrent booking conflicts
 * - Confirm booking after payment
 * - Cancel booking with seat release
 *
 * Design Patterns Used:
 * - Transaction Pattern: ACID compliance for booking operations
 * - Pessimistic Locking: Prevent double-booking of seats
 * - Optimistic Locking: Handle concurrent updates
 *
 * Concurrency Handling:
 * - Uses database-level pessimistic locks for seat selection
 * - Uses optimistic locking with version fields for updates
 * - Isolation level SERIALIZABLE for critical booking operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final EntityMapper entityMapper;

    // Constants
    private static final int MAX_SEATS_PER_BOOKING = 10;
    private static final BigDecimal PREMIUM_MULTIPLIER = new BigDecimal("1.5");
    private static final BigDecimal VIP_MULTIPLIER = new BigDecimal("2.0");
    private static final BigDecimal RECLINER_MULTIPLIER = new BigDecimal("2.5");

    /**
     * Creates a new booking with seat reservation.
     *
     * WRITE SCENARIO - Main Implementation
     *
     * Algorithm:
     * 1. Validate user and show exist
     * 2. Validate seat selection (max seats, availability)
     * 3. Lock seats using pessimistic locking to prevent double-booking
     * 4. Calculate pricing based on seat categories
     * 5. Create booking record
     * 6. Update seat status to BLOCKED
     * 7. Return booking confirmation
     *
     * Concurrency Handling:
     * - SERIALIZABLE isolation prevents phantom reads
     * - Pessimistic write lock on seats prevents concurrent booking
     * - Transaction rollback on any failure releases locks
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        log.info("Creating booking for user {} for show {} with {} seats",
                request.getUserId(), request.getShowId(), request.getSeatIds().size());

        // Step 1: Validate inputs
        validateBookingRequest(request);

        // Step 2: Fetch and validate user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        // Step 3: Fetch and validate show
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", request.getShowId()));

        validateShowIsBookable(show);

        // Step 4: Check for existing pending booking
        if (bookingRepository.hasExistingPendingBooking(request.getUserId(), request.getShowId())) {
            throw new BookingException("You already have a pending booking for this show. Please complete or cancel it first.");
        }

        // Step 5: Lock and validate seats (CRITICAL SECTION with pessimistic locking)
        List<ShowSeat> seatsToBook = showSeatRepository.findByIdInAndShowIdWithLock(
                request.getSeatIds(), request.getShowId());

        validateSelectedSeats(seatsToBook, request.getSeatIds());

        // Step 6: Calculate pricing
        BigDecimal totalAmount = calculateTotalAmount(seatsToBook, show.getBasePrice());
        BigDecimal discountAmount = calculateDiscount(totalAmount, request.getCouponCode());
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        // Step 7: Generate booking number
        String bookingNumber = generateBookingNumber();

        // Step 8: Create booking entity
        Booking booking = Booking.builder()
                .bookingNumber(bookingNumber)
                .user(user)
                .show(show)
                .numberOfSeats(seatsToBook.size())
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .status(Booking.BookingStatus.PENDING)
                .bookingTime(LocalDateTime.now())
                .build();

        // Step 9: Save booking
        booking = bookingRepository.save(booking);
        final Booking savedBooking = booking;

        // Step 10: Update seat status and link to booking
        seatsToBook.forEach(seat -> {
            seat.setStatus(ShowSeat.SeatStatus.BLOCKED);
            seat.setBooking(savedBooking);
        });
        showSeatRepository.saveAll(seatsToBook);

        // Step 11: Update booking with seats
        booking.setBookedSeats(Set.copyOf(seatsToBook));

        log.info("Booking created successfully: {}", bookingNumber);

        return entityMapper.toBookingResponseDTO(booking);
    }

    /**
     * Confirms a pending booking after successful payment.
     * Changes booking status from PENDING to CONFIRMED.
     * Changes seat status from BLOCKED to BOOKED.
     */
    @Override
    @Transactional
    public BookingResponseDTO confirmBooking(Long bookingId, String transactionId) {
        log.info("Confirming booking {} with transaction {}", bookingId, transactionId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Validate booking state
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new BookingException("Booking cannot be confirmed. Current status: " + booking.getStatus());
        }

        // Create payment record
        Payment payment = Payment.builder()
                .transactionId(transactionId)
                .amount(booking.getFinalAmount())
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD) // Default for now
                .status(Payment.PaymentStatus.SUCCESS)
                .booking(booking)
                .initiatedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // Update booking status
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentTime(LocalDateTime.now());
        booking.setPayment(payment);

        // Update seat status to BOOKED
        List<ShowSeat> seats = showSeatRepository.findByBookingId(bookingId);
        seats.forEach(seat -> seat.setStatus(ShowSeat.SeatStatus.BOOKED));
        showSeatRepository.saveAll(seats);

        // Update show available seats count
        Show show = booking.getShow();
        show.setAvailableSeats(show.getAvailableSeats() - booking.getNumberOfSeats());
        showRepository.save(show);

        booking = bookingRepository.save(booking);

        log.info("Booking {} confirmed successfully", bookingId);

        return entityMapper.toBookingResponseDTO(booking);
    }

    /**
     * Cancels a booking and releases the reserved seats.
     * Handles refund logic if payment was made.
     */
    @Override
    @Transactional
    public BookingResponseDTO cancelBooking(Long bookingId, String reason) {
        log.info("Cancelling booking {} with reason: {}", bookingId, reason);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Validate booking can be cancelled
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled");
        }

        // Release seats
        List<ShowSeat> seats = showSeatRepository.findByBookingId(bookingId);
        seats.forEach(seat -> {
            seat.setStatus(ShowSeat.SeatStatus.AVAILABLE);
            seat.setBooking(null);
        });
        showSeatRepository.saveAll(seats);

        // If booking was confirmed, update show availability and handle refund
        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            Show show = booking.getShow();
            show.setAvailableSeats(show.getAvailableSeats() + booking.getNumberOfSeats());
            showRepository.save(show);

            // Mark payment as refunded
            if (booking.getPayment() != null) {
                Payment payment = booking.getPayment();
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
            }
        }

        // Update booking status
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancellationTime(LocalDateTime.now());
        booking.setCancellationReason(reason);
        booking = bookingRepository.save(booking);

        log.info("Booking {} cancelled successfully", bookingId);

        return entityMapper.toBookingResponseDTO(booking);
    }

    /**
     * Get booking details by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(Long bookingId) {
        log.info("Fetching booking with ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        return entityMapper.toBookingResponseDTO(booking);
    }

    /**
     * Get booking by booking number.
     */
    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingByNumber(String bookingNumber) {
        log.info("Fetching booking with number: {}", bookingNumber);

        Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "bookingNumber", bookingNumber));

        return entityMapper.toBookingResponseDTO(booking);
    }

    /**
     * Get all bookings for a user.
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsByUser(Long userId) {
        log.info("Fetching bookings for user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        List<Booking> bookings = bookingRepository.findActiveBookingsByUser(userId);

        return bookings.stream()
                .map(entityMapper::toBookingResponseDTO)
                .collect(Collectors.toList());
    }

    // ============ PRIVATE HELPER METHODS ============

    /**
     * Validates the booking request parameters.
     */
    private void validateBookingRequest(BookingRequestDTO request) {
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new BookingException("At least one seat must be selected");
        }

        if (request.getSeatIds().size() > MAX_SEATS_PER_BOOKING) {
            throw new BookingException("Maximum " + MAX_SEATS_PER_BOOKING + " seats can be booked at once");
        }

        // Check for duplicate seat IDs
        if (request.getSeatIds().size() != request.getSeatIds().stream().distinct().count()) {
            throw new BookingException("Duplicate seats in selection");
        }
    }

    /**
     * Validates that the show is available for booking.
     */
    private void validateShowIsBookable(Show show) {
        if (!show.getIsActive()) {
            throw new BookingException("This show is no longer available for booking");
        }

        LocalDateTime showDateTime = LocalDateTime.of(show.getShowDate(), show.getStartTime());
        if (showDateTime.isBefore(LocalDateTime.now())) {
            throw new BookingException("Cannot book for a show that has already started");
        }
    }

    /**
     * Validates that selected seats are available.
     */
    private void validateSelectedSeats(List<ShowSeat> foundSeats, List<Long> requestedSeatIds) {
        // Check if all seats were found
        if (foundSeats.size() != requestedSeatIds.size()) {
            throw new ResourceNotFoundException("Some selected seats were not found");
        }

        // Check if all seats are available
        List<ShowSeat> unavailableSeats = foundSeats.stream()
                .filter(seat -> seat.getStatus() != ShowSeat.SeatStatus.AVAILABLE)
                .collect(Collectors.toList());

        if (!unavailableSeats.isEmpty()) {
            String unavailableSeatNames = unavailableSeats.stream()
                    .map(ss -> ss.getSeat().getSeatIdentifier())
                    .collect(Collectors.joining(", "));
            throw new SeatNotAvailableException("The following seats are no longer available: " + unavailableSeatNames);
        }
    }

    /**
     * Calculates total amount based on seat categories and base price.
     */
    private BigDecimal calculateTotalAmount(List<ShowSeat> seats, BigDecimal basePrice) {
        BigDecimal total = BigDecimal.ZERO;

        for (ShowSeat showSeat : seats) {
            BigDecimal seatPrice = calculateSeatPrice(basePrice, showSeat.getSeat().getCategory());
            showSeat.setPrice(seatPrice); // Update seat price
            total = total.add(seatPrice);
        }

        return total;
    }

    /**
     * Calculates individual seat price based on category.
     */
    private BigDecimal calculateSeatPrice(BigDecimal basePrice, Seat.SeatCategory category) {
        return switch (category) {
            case PREMIUM -> basePrice.multiply(PREMIUM_MULTIPLIER);
            case VIP -> basePrice.multiply(VIP_MULTIPLIER);
            case RECLINER -> basePrice.multiply(RECLINER_MULTIPLIER);
            default -> basePrice;
        };
    }

    /**
     * Calculates discount amount based on coupon code.
     * This is a simplified implementation - in production, this would
     * integrate with a coupon/promotion service.
     */
    private BigDecimal calculateDiscount(BigDecimal totalAmount, String couponCode) {
        if (couponCode == null || couponCode.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Simple coupon logic - in production, query coupon service
        return switch (couponCode.toUpperCase()) {
            case "FLAT100" -> new BigDecimal("100.00");
            case "PERCENT10" -> totalAmount.multiply(new BigDecimal("0.10"));
            case "PERCENT20" -> totalAmount.multiply(new BigDecimal("0.20"));
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * Generates a unique booking number.
     * Format: BK-YYYYMMDD-XXXXX (e.g., BK-20260329-A1B2C)
     */
    private String generateBookingNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return "BK-" + datePrefix + "-" + uniqueSuffix;
    }
}

