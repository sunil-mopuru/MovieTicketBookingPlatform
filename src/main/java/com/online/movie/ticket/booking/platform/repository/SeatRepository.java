package com.online.movie.ticket.booking.platform.repository;

import com.online.movie.ticket.booking.platform.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Seat entity operations.
 */
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    /**
     * Find all seats in a screen
     */
    List<Seat> findByScreenIdAndIsAvailableTrue(Long screenId);

    /**
     * Find all seats in a screen ordered by row and seat number
     */
    List<Seat> findByScreenIdOrderByRowLabelAscSeatNumberAsc(Long screenId);

    /**
     * Find a specific seat by screen, row, and number
     */
    Optional<Seat> findByScreenIdAndRowLabelAndSeatNumber(Long screenId, String rowLabel, Integer seatNumber);

    /**
     * Count available seats in a screen
     */
    long countByScreenIdAndIsAvailableTrue(Long screenId);
}

