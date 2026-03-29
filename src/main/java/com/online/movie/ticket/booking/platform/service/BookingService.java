package com.online.movie.ticket.booking.platform.service;

import com.online.movie.ticket.booking.platform.dto.BookingRequestDTO;
import com.online.movie.ticket.booking.platform.dto.BookingResponseDTO;

import java.util.List;

/**
 * Service interface for booking-related operations (WRITE SCENARIO).
 * Provides methods for creating, confirming, and cancelling bookings.
 */
public interface BookingService {

    /**
     * Create a new booking (reserves seats temporarily).
     *
     * @param request Booking request with user, show, and seat information
     * @return Booking confirmation with details
     */
    BookingResponseDTO createBooking(BookingRequestDTO request);

    /**
     * Confirm a pending booking after payment.
     *
     * @param bookingId Booking ID
     * @param transactionId Payment transaction ID
     * @return Updated booking details
     */
    BookingResponseDTO confirmBooking(Long bookingId, String transactionId);

    /**
     * Cancel a booking.
     *
     * @param bookingId Booking ID
     * @param reason Cancellation reason
     * @return Updated booking details
     */
    BookingResponseDTO cancelBooking(Long bookingId, String reason);

    /**
     * Get booking details by ID.
     *
     * @param bookingId Booking ID
     * @return Booking details
     */
    BookingResponseDTO getBookingById(Long bookingId);

    /**
     * Get booking by booking number.
     *
     * @param bookingNumber Unique booking number
     * @return Booking details
     */
    BookingResponseDTO getBookingByNumber(String bookingNumber);

    /**
     * Get all bookings for a user.
     *
     * @param userId User ID
     * @return List of user's bookings
     */
    List<BookingResponseDTO> getBookingsByUser(Long userId);
}

