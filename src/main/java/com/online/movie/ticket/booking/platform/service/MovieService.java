package com.online.movie.ticket.booking.platform.service;

import com.online.movie.ticket.booking.platform.dto.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for movie-related operations (READ SCENARIO).
 * Provides methods for searching and browsing movies with shows.
 */
public interface MovieService {

    /**
     * Search movies with optional filters
     *
     * @param request Search criteria including title, genre, language, city, and date
     * @return Paginated list of movies matching the criteria
     */
    List<MovieDTO> searchMovies(MovieSearchRequestDTO request);

    /**
     * Get movie details by ID
     *
     * @param movieId Movie ID
     * @return Movie details
     */
    MovieDTO getMovieById(Long movieId);

    /**
     * Get movie details with all shows for a specific date and city
     *
     * @param movieId Movie ID
     * @param city City name
     * @param date Show date
     * @return Movie with theater shows grouped by theater
     */
    MovieSearchResponseDTO getMovieWithShows(Long movieId, String city, LocalDate date);

    /**
     * Get all shows for a movie in a specific theater on a date
     *
     * @param movieId Movie ID
     * @param theaterId Theater ID
     * @param date Show date
     * @return List of shows
     */
    List<ShowDTO> getShowsForMovieInTheater(Long movieId, Long theaterId, LocalDate date);

    /**
     * Get seat availability for a specific show
     *
     * @param showId Show ID
     * @return List of seats with availability status
     */
    List<SeatDTO> getSeatsForShow(Long showId);

    /**
     * Get all available genres
     */
    List<String> getAllGenres();

    /**
     * Get all available languages
     */
    List<String> getAllLanguages();

    /**
     * Get all cities where movies are showing
     */
    List<String> getAllCities();

    /**
     * Get currently running movies
     */
    List<MovieDTO> getCurrentlyRunningMovies();
}

