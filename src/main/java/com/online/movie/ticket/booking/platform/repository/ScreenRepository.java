package com.online.movie.ticket.booking.platform.repository;

import com.online.movie.ticket.booking.platform.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Screen entity operations.
 */
@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {

    /**
     * Find all screens in a theater
     */
    List<Screen> findByTheaterIdAndIsActiveTrue(Long theaterId);

    /**
     * Find a specific screen by theater and screen number
     */
    Optional<Screen> findByTheaterIdAndScreenNumberAndIsActiveTrue(Long theaterId, Integer screenNumber);

    /**
     * Count active screens in a theater
     */
    long countByTheaterIdAndIsActiveTrue(Long theaterId);
}

