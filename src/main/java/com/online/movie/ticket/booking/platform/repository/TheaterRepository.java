package com.online.movie.ticket.booking.platform.repository;

import com.online.movie.ticket.booking.platform.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Theater entity operations.
 */
@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long> {

    /**
     * Find all active theaters
     */
    List<Theater> findByIsActiveTrue();

    /**
     * Find theaters by city
     */
    List<Theater> findByCityIgnoreCaseAndIsActiveTrue(String city);

    /**
     * Find all unique cities where theaters are located
     */
    @Query("SELECT DISTINCT t.city FROM Theater t WHERE t.isActive = true ORDER BY t.city")
    List<String> findAllCities();

    /**
     * Find theaters showing a specific movie on a date
     */
    @Query("SELECT DISTINCT t FROM Theater t " +
           "JOIN t.screens sc " +
           "JOIN sc.shows s " +
           "WHERE s.movie.id = :movieId " +
           "AND s.showDate = :date " +
           "AND t.isActive = true " +
           "AND s.isActive = true")
    List<Theater> findTheatersShowingMovieOnDate(@Param("movieId") Long movieId, @Param("date") LocalDate date);

    /**
     * Find theaters showing a movie in a specific city on a date
     */
    @Query("SELECT DISTINCT t FROM Theater t " +
           "JOIN t.screens sc " +
           "JOIN sc.shows s " +
           "WHERE s.movie.id = :movieId " +
           "AND s.showDate = :date " +
           "AND LOWER(t.city) = LOWER(:city) " +
           "AND t.isActive = true " +
           "AND s.isActive = true")
    List<Theater> findTheatersShowingMovieInCityOnDate(@Param("movieId") Long movieId,
                                                        @Param("city") String city,
                                                        @Param("date") LocalDate date);
}

