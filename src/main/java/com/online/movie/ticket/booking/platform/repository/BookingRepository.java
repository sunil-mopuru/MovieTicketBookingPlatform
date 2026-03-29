package com.online.movie.ticket.booking.platform.repository;

import com.online.movie.ticket.booking.platform.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Booking entity operations.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find booking by booking number
     */
    Optional<Booking> findByBookingNumber(String bookingNumber);

    /**
     * Find all bookings by user
     */
    Page<Booking> findByUserIdOrderByBookingTimeDesc(Long userId, Pageable pageable);

    /**
     * Find active bookings by user (not cancelled or expired)
     */
    @Query("SELECT b FROM Booking b " +
           "WHERE b.user.id = :userId " +
           "AND b.status NOT IN ('CANCELLED', 'EXPIRED') " +
           "ORDER BY b.bookingTime DESC")
    List<Booking> findActiveBookingsByUser(@Param("userId") Long userId);

    /**
     * Find pending bookings that have expired (for cleanup job)
     */
    @Query("SELECT b FROM Booking b " +
           "WHERE b.status = 'PENDING' " +
           "AND b.bookingTime < :expiryTime")
    List<Booking> findExpiredPendingBookings(@Param("expiryTime") LocalDateTime expiryTime);

    /**
     * Find bookings for a specific show
     */
    List<Booking> findByShowIdAndStatusNot(Long showId, Booking.BookingStatus status);

    /**
     * Check if user has existing pending booking for a show
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.user.id = :userId " +
           "AND b.show.id = :showId " +
           "AND b.status = 'PENDING'")
    boolean hasExistingPendingBooking(@Param("userId") Long userId, @Param("showId") Long showId);
}

