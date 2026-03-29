package com.online.movie.ticket.booking.platform.repository;

import com.online.movie.ticket.booking.platform.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Show entity operations.
 */
@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    /**
     * Find all shows for a movie on a specific date
     */
    @Query("SELECT s FROM Show s " +
           "WHERE s.movie.id = :movieId " +
           "AND s.showDate = :date " +
           "AND s.isActive = true " +
           "ORDER BY s.startTime")
    List<Show> findShowsByMovieAndDate(@Param("movieId") Long movieId, @Param("date") LocalDate date);

    /**
     * Find all shows for a movie in a specific theater on a date
     */
    @Query("SELECT s FROM Show s " +
           "WHERE s.movie.id = :movieId " +
           "AND s.screen.theater.id = :theaterId " +
           "AND s.showDate = :date " +
           "AND s.isActive = true " +
           "ORDER BY s.startTime")
    List<Show> findShowsByMovieTheaterAndDate(@Param("movieId") Long movieId,
                                               @Param("theaterId") Long theaterId,
                                               @Param("date") LocalDate date);

    /**
     * Find all shows for a movie in a city on a date
     */
    @Query("SELECT s FROM Show s " +
           "JOIN s.screen sc " +
           "JOIN sc.theater t " +
           "WHERE s.movie.id = :movieId " +
           "AND LOWER(t.city) = LOWER(:city) " +
           "AND s.showDate = :date " +
           "AND s.isActive = true " +
           "AND t.isActive = true " +
           "ORDER BY t.name, s.startTime")
    List<Show> findShowsByMovieCityAndDate(@Param("movieId") Long movieId,
                                            @Param("city") String city,
                                            @Param("date") LocalDate date);

    /**
     * Find shows in a screen on a date (for scheduling conflicts check)
     */
    @Query("SELECT s FROM Show s " +
           "WHERE s.screen.id = :screenId " +
           "AND s.showDate = :date " +
           "AND s.isActive = true " +
           "ORDER BY s.startTime")
    List<Show> findShowsByScreenAndDate(@Param("screenId") Long screenId, @Param("date") LocalDate date);

    /**
     * Check for overlapping shows in a screen
     */
    @Query("SELECT COUNT(s) > 0 FROM Show s " +
           "WHERE s.screen.id = :screenId " +
           "AND s.showDate = :date " +
           "AND s.isActive = true " +
           "AND ((s.startTime <= :startTime AND s.endTime > :startTime) " +
           "OR (s.startTime < :endTime AND s.endTime >= :endTime) " +
           "OR (s.startTime >= :startTime AND s.endTime <= :endTime))")
    boolean hasOverlappingShows(@Param("screenId") Long screenId,
                                @Param("date") LocalDate date,
                                @Param("startTime") java.time.LocalTime startTime,
                                @Param("endTime") java.time.LocalTime endTime);
}

