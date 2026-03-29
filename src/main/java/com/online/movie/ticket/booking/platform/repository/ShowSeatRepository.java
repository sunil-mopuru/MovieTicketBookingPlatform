package com.online.movie.ticket.booking.platform.repository;

import com.online.movie.ticket.booking.platform.entity.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;

/**
 * Repository interface for ShowSeat entity operations.
 * Includes pessimistic locking for concurrent booking prevention.
 */
@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    /**
     * Find all seats for a show ordered by row and seat number
     */
    @Query("SELECT ss FROM ShowSeat ss " +
           "JOIN FETCH ss.seat s " +
           "WHERE ss.show.id = :showId " +
           "ORDER BY s.rowLabel, s.seatNumber")
    List<ShowSeat> findByShowIdWithSeat(@Param("showId") Long showId);

    /**
     * Find available seats for a show
     */
    @Query("SELECT ss FROM ShowSeat ss " +
           "JOIN FETCH ss.seat s " +
           "WHERE ss.show.id = :showId " +
           "AND ss.status = 'AVAILABLE' " +
           "ORDER BY s.rowLabel, s.seatNumber")
    List<ShowSeat> findAvailableSeatsForShow(@Param("showId") Long showId);

    /**
     * Find specific seats with pessimistic lock for booking
     * This prevents concurrent booking of the same seats
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ss FROM ShowSeat ss " +
           "WHERE ss.id IN :seatIds " +
           "AND ss.show.id = :showId")
    List<ShowSeat> findByIdInAndShowIdWithLock(@Param("seatIds") List<Long> seatIds, @Param("showId") Long showId);

    /**
     * Count available seats for a show
     */
    @Query("SELECT COUNT(ss) FROM ShowSeat ss " +
           "WHERE ss.show.id = :showId " +
           "AND ss.status = 'AVAILABLE'")
    long countAvailableSeatsForShow(@Param("showId") Long showId);

    /**
     * Check if all seats are available
     */
    @Query("SELECT COUNT(ss) = :expectedCount FROM ShowSeat ss " +
           "WHERE ss.id IN :seatIds " +
           "AND ss.status = 'AVAILABLE'")
    boolean areAllSeatsAvailable(@Param("seatIds") List<Long> seatIds, @Param("expectedCount") long expectedCount);

    /**
     * Update seat status to BLOCKED (for temporary hold during booking)
     */
    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = 'BLOCKED' " +
           "WHERE ss.id IN :seatIds " +
           "AND ss.status = 'AVAILABLE'")
    int blockSeats(@Param("seatIds") List<Long> seatIds);

    /**
     * Update seat status to AVAILABLE (release blocked seats)
     */
    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = 'AVAILABLE', ss.booking = null " +
           "WHERE ss.id IN :seatIds " +
           "AND ss.status = 'BLOCKED'")
    int releaseSeats(@Param("seatIds") List<Long> seatIds);

    /**
     * Find seats by booking ID
     */
    List<ShowSeat> findByBookingId(Long bookingId);
}

