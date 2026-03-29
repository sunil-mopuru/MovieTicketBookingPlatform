package com.online.movie.ticket.booking.platform.controller;

import com.online.movie.ticket.booking.platform.dto.*;
import com.online.movie.ticket.booking.platform.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Booking operations.
 *
 * WRITE SCENARIO APIs:
 * - Create booking (reserve seats)
 * - Confirm booking (after payment)
 * - Cancel booking
 * - View booking details
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    /**
     * Create a new booking (reserve seats).
     * This is the main WRITE scenario endpoint.
     *
     * POST /api/v1/bookings
     *
     * Request Body:
     * {
     *   "userId": 1,
     *   "showId": 1,
     *   "seatIds": [1, 2, 3],
     *   "couponCode": "FLAT100"
     * }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponseDTO>> createBooking(
            @Valid @RequestBody BookingRequestDTO request) {

        log.info("Creating booking for user {} for show {} with {} seats",
                request.getUserId(), request.getShowId(), request.getSeatIds().size());

        BookingResponseDTO booking = bookingService.createBooking(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(booking, "Booking created successfully. Please complete payment within 10 minutes."));
    }

    /**
     * Confirm booking after payment.
     *
     * PUT /api/v1/bookings/{bookingId}/confirm
     *
     * Request Param: transactionId - Payment transaction ID
     */
    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> confirmBooking(
            @PathVariable Long bookingId,
            @RequestParam String transactionId) {

        log.info("Confirming booking {} with transaction {}", bookingId, transactionId);

        BookingResponseDTO booking = bookingService.confirmBooking(bookingId, transactionId);

        return ResponseEntity.ok(ApiResponse.success(booking, "Booking confirmed successfully!"));
    }

    /**
     * Cancel a booking.
     *
     * PUT /api/v1/bookings/{bookingId}/cancel
     *
     * Request Param: reason - Cancellation reason
     */
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> cancelBooking(
            @PathVariable Long bookingId,
            @RequestParam(required = false, defaultValue = "User requested cancellation") String reason) {

        log.info("Cancelling booking {} with reason: {}", bookingId, reason);

        BookingResponseDTO booking = bookingService.cancelBooking(bookingId, reason);

        return ResponseEntity.ok(ApiResponse.success(booking, "Booking cancelled successfully. Refund will be processed within 5-7 business days."));
    }

    /**
     * Get booking by ID.
     *
     * GET /api/v1/bookings/{bookingId}
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> getBookingById(@PathVariable Long bookingId) {
        log.info("Getting booking with ID: {}", bookingId);

        BookingResponseDTO booking = bookingService.getBookingById(bookingId);

        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    /**
     * Get booking by booking number.
     *
     * GET /api/v1/bookings/number/{bookingNumber}
     */
    @GetMapping("/number/{bookingNumber}")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> getBookingByNumber(
            @PathVariable String bookingNumber) {

        log.info("Getting booking with number: {}", bookingNumber);

        BookingResponseDTO booking = bookingService.getBookingByNumber(bookingNumber);

        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    /**
     * Get all bookings for a user.
     *
     * GET /api/v1/bookings/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getBookingsByUser(
            @PathVariable Long userId) {

        log.info("Getting bookings for user: {}", userId);

        List<BookingResponseDTO> bookings = bookingService.getBookingsByUser(userId);

        return ResponseEntity.ok(ApiResponse.success(bookings, "User bookings retrieved successfully"));
    }
}

