package com.online.movie.ticket.booking.platform.controller;

import com.online.movie.ticket.booking.platform.dto.*;
import com.online.movie.ticket.booking.platform.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Movie-related operations.
 *
 * READ SCENARIO APIs:
 * - Search movies with filters
 * - Get movie details with shows
 * - Get seat availability
 * - Get reference data (genres, languages, cities)
 */
@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Slf4j
public class MovieController {

    private final MovieService movieService;

    /**
     * Search movies with optional filters.
     *
     * GET /api/v1/movies/search?title=&genre=&language=&city=&date=&page=&size=
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MovieDTO>>> searchMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        log.info("Searching movies with title={}, genre={}, language={}, city={}, date={}",
                title, genre, language, city, date);

        MovieSearchRequestDTO request = MovieSearchRequestDTO.builder()
                .title(title)
                .genre(genre)
                .language(language)
                .city(city)
                .date(date)
                .page(page)
                .size(size)
                .build();

        List<MovieDTO> movies = movieService.searchMovies(request);

        return ResponseEntity.ok(ApiResponse.success(movies, "Movies retrieved successfully"));
    }

    /**
     * Get movie by ID.
     *
     * GET /api/v1/movies/{movieId}
     */
    @GetMapping("/{movieId}")
    public ResponseEntity<ApiResponse<MovieDTO>> getMovieById(@PathVariable Long movieId) {
        log.info("Getting movie with ID: {}", movieId);

        MovieDTO movie = movieService.getMovieById(movieId);

        return ResponseEntity.ok(ApiResponse.success(movie));
    }

    /**
     * Get movie details with shows grouped by theater.
     * This is the main READ scenario endpoint.
     *
     * GET /api/v1/movies/{movieId}/shows?city=&date=
     */
    @GetMapping("/{movieId}/shows")
    public ResponseEntity<ApiResponse<MovieSearchResponseDTO>> getMovieWithShows(
            @PathVariable Long movieId,
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Getting shows for movie {} in {} on {}", movieId, city, date);

        MovieSearchResponseDTO response = movieService.getMovieWithShows(movieId, city, date);

        return ResponseEntity.ok(ApiResponse.success(response, "Shows retrieved successfully"));
    }

    /**
     * Get shows for a movie in a specific theater.
     *
     * GET /api/v1/movies/{movieId}/theaters/{theaterId}/shows?date=
     */
    @GetMapping("/{movieId}/theaters/{theaterId}/shows")
    public ResponseEntity<ApiResponse<List<ShowDTO>>> getShowsInTheater(
            @PathVariable Long movieId,
            @PathVariable Long theaterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Getting shows for movie {} in theater {} on {}", movieId, theaterId, date);

        List<ShowDTO> shows = movieService.getShowsForMovieInTheater(movieId, theaterId, date);

        return ResponseEntity.ok(ApiResponse.success(shows, "Shows retrieved successfully"));
    }

    /**
     * Get currently running movies.
     *
     * GET /api/v1/movies/now-showing
     */
    @GetMapping("/now-showing")
    public ResponseEntity<ApiResponse<List<MovieDTO>>> getNowShowingMovies() {
        log.info("Getting currently running movies");

        List<MovieDTO> movies = movieService.getCurrentlyRunningMovies();

        return ResponseEntity.ok(ApiResponse.success(movies, "Now showing movies retrieved successfully"));
    }

    /**
     * Get all available genres.
     *
     * GET /api/v1/movies/genres
     */
    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<String>>> getAllGenres() {
        log.info("Getting all genres");

        List<String> genres = movieService.getAllGenres();

        return ResponseEntity.ok(ApiResponse.success(genres));
    }

    /**
     * Get all available languages.
     *
     * GET /api/v1/movies/languages
     */
    @GetMapping("/languages")
    public ResponseEntity<ApiResponse<List<String>>> getAllLanguages() {
        log.info("Getting all languages");

        List<String> languages = movieService.getAllLanguages();

        return ResponseEntity.ok(ApiResponse.success(languages));
    }

    /**
     * Get all cities with theaters.
     *
     * GET /api/v1/movies/cities
     */
    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<String>>> getAllCities() {
        log.info("Getting all cities");

        List<String> cities = movieService.getAllCities();

        return ResponseEntity.ok(ApiResponse.success(cities));
    }
}

