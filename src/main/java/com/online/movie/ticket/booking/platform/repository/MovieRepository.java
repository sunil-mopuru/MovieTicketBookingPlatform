package com.online.movie.ticket.booking.platform.repository;

import com.online.movie.ticket.booking.platform.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Movie entity operations.
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * Find all active movies
     */
    List<Movie> findByIsActiveTrue();

    /**
     * Find movies by title containing (case-insensitive search)
     */
    Page<Movie> findByTitleContainingIgnoreCaseAndIsActiveTrue(String title, Pageable pageable);

    /**
     * Find movies by genre
     */
    List<Movie> findByGenreIgnoreCaseAndIsActiveTrue(String genre);

    /**
     * Find movies by language
     */
    List<Movie> findByLanguageIgnoreCaseAndIsActiveTrue(String language);

    /**
     * Find movies currently showing in a city on a specific date
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
           "JOIN m.shows s " +
           "JOIN s.screen sc " +
           "JOIN sc.theater t " +
           "WHERE t.city = :city " +
           "AND s.showDate = :date " +
           "AND m.isActive = true " +
           "AND s.isActive = true " +
           "AND t.isActive = true")
    List<Movie> findMoviesShowingInCityOnDate(@Param("city") String city, @Param("date") LocalDate date);

    /**
     * Advanced search with multiple optional filters
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
           "JOIN m.shows s " +
           "JOIN s.screen sc " +
           "JOIN sc.theater t " +
           "WHERE (:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:genre IS NULL OR LOWER(m.genre) = LOWER(:genre)) " +
           "AND (:language IS NULL OR LOWER(m.language) = LOWER(:language)) " +
           "AND (:city IS NULL OR LOWER(t.city) = LOWER(:city)) " +
           "AND (:date IS NULL OR s.showDate = :date) " +
           "AND m.isActive = true " +
           "AND s.isActive = true " +
           "AND t.isActive = true")
    Page<Movie> searchMovies(@Param("title") String title,
                             @Param("genre") String genre,
                             @Param("language") String language,
                             @Param("city") String city,
                             @Param("date") LocalDate date,
                             Pageable pageable);

    /**
     * Get all unique genres
     */
    @Query("SELECT DISTINCT m.genre FROM Movie m WHERE m.isActive = true ORDER BY m.genre")
    List<String> findAllGenres();

    /**
     * Get all unique languages
     */
    @Query("SELECT DISTINCT m.language FROM Movie m WHERE m.isActive = true ORDER BY m.language")
    List<String> findAllLanguages();
}

