package com.online.movie.ticket.booking.platform.service.impl;

import com.online.movie.ticket.booking.platform.dto.*;
import com.online.movie.ticket.booking.platform.entity.*;
import com.online.movie.ticket.booking.platform.exception.ResourceNotFoundException;
import com.online.movie.ticket.booking.platform.mapper.EntityMapper;
import com.online.movie.ticket.booking.platform.repository.*;
import com.online.movie.ticket.booking.platform.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of MovieService providing movie search and browsing functionality.
 *
 * READ SCENARIO IMPLEMENTATION:
 * - Search movies with multiple filters (title, genre, language, city, date)
 * - Get movie details with shows grouped by theater
 * - Get seat availability for a show
 *
 * Design Patterns Used:
 * - Repository Pattern: Data access abstraction
 * - DTO Pattern: Data transfer objects for API responses
 * - Caching: Frequently accessed data is cached
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // Optimized for read operations
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final TheaterRepository theaterRepository;
    private final EntityMapper entityMapper;

    /**
     * Search movies with multiple optional filters.
     * Uses Spring Data JPA dynamic queries for flexible searching.
     */
    @Override
    public List<MovieDTO> searchMovies(MovieSearchRequestDTO request) {
        log.info("Searching movies with criteria: {}", request);

        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        Pageable pageable = PageRequest.of(page, size);

        Page<Movie> movies = movieRepository.searchMovies(
                request.getTitle(),
                request.getGenre(),
                request.getLanguage(),
                request.getCity(),
                request.getDate(),
                pageable
        );

        log.info("Found {} movies matching criteria", movies.getTotalElements());

        return movies.getContent().stream()
                .map(entityMapper::toMovieDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get movie by ID with validation.
     */
    @Override
    @Cacheable(value = "movies", key = "#movieId")
    public MovieDTO getMovieById(Long movieId) {
        log.info("Fetching movie with ID: {}", movieId);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", movieId));

        return entityMapper.toMovieDTO(movie);
    }

    /**
     * Get movie details with shows grouped by theater for a specific city and date.
     * This is the main READ scenario implementation.
     *
     * Algorithm:
     * 1. Fetch movie details
     * 2. Find all shows for the movie in the specified city on the date
     * 3. Group shows by theater
     * 4. Build response with proper DTOs
     */
    @Override
    public MovieSearchResponseDTO getMovieWithShows(Long movieId, String city, LocalDate date) {
        log.info("Fetching movie {} with shows in {} on {}", movieId, city, date);

        // Validate movie exists
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", movieId));

        // Get all shows for this movie in the city on the date
        List<Show> shows = showRepository.findShowsByMovieCityAndDate(movieId, city, date);

        if (shows.isEmpty()) {
            log.info("No shows found for movie {} in {} on {}", movieId, city, date);
        }

        // Group shows by theater
        Map<Theater, List<Show>> showsByTheater = shows.stream()
                .collect(Collectors.groupingBy(
                        show -> show.getScreen().getTheater(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Build theater shows DTOs
        List<MovieSearchResponseDTO.TheaterShowsDTO> theaterShowsList = showsByTheater.entrySet().stream()
                .map(entry -> MovieSearchResponseDTO.TheaterShowsDTO.builder()
                        .theater(entityMapper.toTheaterDTO(entry.getKey()))
                        .shows(entry.getValue().stream()
                                .map(this::toShowDTOWithAvailability)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        log.info("Found shows in {} theaters for movie {}", theaterShowsList.size(), movieId);

        return MovieSearchResponseDTO.builder()
                .movie(entityMapper.toMovieDTO(movie))
                .theaterShows(theaterShowsList)
                .build();
    }

    /**
     * Get all shows for a movie in a specific theater on a date.
     */
    @Override
    public List<ShowDTO> getShowsForMovieInTheater(Long movieId, Long theaterId, LocalDate date) {
        log.info("Fetching shows for movie {} in theater {} on {}", movieId, theaterId, date);

        // Validate movie and theater exist
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie", "id", movieId);
        }
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("Theater", "id", theaterId);
        }

        List<Show> shows = showRepository.findShowsByMovieTheaterAndDate(movieId, theaterId, date);

        return shows.stream()
                .map(this::toShowDTOWithAvailability)
                .collect(Collectors.toList());
    }

    /**
     * Get seat availability for a specific show.
     * Returns all seats with their current booking status.
     */
    @Override
    public List<SeatDTO> getSeatsForShow(Long showId) {
        log.info("Fetching seats for show: {}", showId);

        List<ShowSeat> showSeats = showSeatRepository.findByShowIdWithSeat(showId);

        if (showSeats.isEmpty()) {
            throw new ResourceNotFoundException("Show", "id", showId);
        }

        return showSeats.stream()
                .map(entityMapper::toSeatDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all available genres (cached for performance).
     */
    @Override
    @Cacheable(value = "genres")
    public List<String> getAllGenres() {
        log.info("Fetching all genres");
        return movieRepository.findAllGenres();
    }

    /**
     * Get all available languages (cached for performance).
     */
    @Override
    @Cacheable(value = "languages")
    public List<String> getAllLanguages() {
        log.info("Fetching all languages");
        return movieRepository.findAllLanguages();
    }

    /**
     * Get all cities where theaters are located (cached for performance).
     */
    @Override
    @Cacheable(value = "cities")
    public List<String> getAllCities() {
        log.info("Fetching all cities");
        return theaterRepository.findAllCities();
    }

    /**
     * Get currently running movies (movies with active shows).
     */
    @Override
    public List<MovieDTO> getCurrentlyRunningMovies() {
        log.info("Fetching currently running movies");

        List<Movie> movies = movieRepository.findByIsActiveTrue();

        return movies.stream()
                .map(entityMapper::toMovieDTO)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to convert Show to ShowDTO with live availability count.
     */
    private ShowDTO toShowDTOWithAvailability(Show show) {
        ShowDTO dto = entityMapper.toShowDTO(show);

        // Get live availability count
        long availableSeats = showSeatRepository.countAvailableSeatsForShow(show.getId());
        dto.setAvailableSeats((int) availableSeats);

        return dto;
    }
}

