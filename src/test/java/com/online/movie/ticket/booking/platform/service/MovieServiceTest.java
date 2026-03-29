package com.online.movie.ticket.booking.platform.service;

import com.online.movie.ticket.booking.platform.dto.*;
import com.online.movie.ticket.booking.platform.entity.*;
import com.online.movie.ticket.booking.platform.mapper.EntityMapper;
import com.online.movie.ticket.booking.platform.repository.*;
import com.online.movie.ticket.booking.platform.service.impl.MovieServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MovieService (READ Scenario).
 * Tests movie search and browse functionality.
 */
@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @Mock
    private TheaterRepository theaterRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private MovieServiceImpl movieService;

    private Movie testMovie;
    private Theater testTheater;
    private Screen testScreen;
    private Show testShow;

    @BeforeEach
    void setUp() {
        // Setup test data
        testTheater = Theater.builder()
                .id(1L)
                .name("PVR Cinemas")
                .city("Bangalore")
                .address("MG Road")
                .isActive(true)
                .build();

        testScreen = Screen.builder()
                .id(1L)
                .theater(testTheater)
                .screenNumber(1)
                .totalSeats(100)
                .screenType(Screen.ScreenType.STANDARD)
                .isActive(true)
                .build();

        testMovie = Movie.builder()
                .id(1L)
                .title("The Dark Knight")
                .description("A gripping superhero thriller")
                .durationMinutes(152)
                .genre("Action")
                .language("English")
                .releaseDate(LocalDate.of(2008, 7, 18))
                .rating("PG-13")
                .isActive(true)
                .build();

        testShow = Show.builder()
                .id(1L)
                .movie(testMovie)
                .screen(testScreen)
                .showDate(LocalDate.now())
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 30))
                .basePrice(new BigDecimal("300.00"))
                .availableSeats(95)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should search movies with filters successfully")
    void searchMovies_Success() {
        // Arrange
        MovieSearchRequestDTO request = MovieSearchRequestDTO.builder()
                .title("Dark")
                .genre("Action")
                .city("Bangalore")
                .date(LocalDate.now())
                .page(0)
                .size(10)
                .build();

        Page<Movie> moviePage = new PageImpl<>(Collections.singletonList(testMovie));
        MovieDTO expectedDTO = MovieDTO.builder()
                .id(1L)
                .title("The Dark Knight")
                .genre("Action")
                .build();

        when(movieRepository.searchMovies(
                eq("Dark"), eq("Action"), isNull(), eq("Bangalore"), eq(LocalDate.now()), any(Pageable.class)))
                .thenReturn(moviePage);
        when(entityMapper.toMovieDTO(testMovie)).thenReturn(expectedDTO);

        // Act
        List<MovieDTO> result = movieService.searchMovies(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("The Dark Knight", result.get(0).getTitle());

        verify(movieRepository).searchMovies(anyString(), anyString(), isNull(), anyString(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get movie with shows grouped by theater")
    void getMovieWithShows_Success() {
        // Arrange
        Long movieId = 1L;
        String city = "Bangalore";
        LocalDate date = LocalDate.now();

        MovieDTO movieDTO = MovieDTO.builder()
                .id(1L)
                .title("The Dark Knight")
                .build();

        TheaterDTO theaterDTO = TheaterDTO.builder()
                .id(1L)
                .name("PVR Cinemas")
                .city("Bangalore")
                .build();

        ShowDTO showDTO = ShowDTO.builder()
                .id(1L)
                .startTime(LocalTime.of(18, 0))
                .availableSeats(95)
                .basePrice(new BigDecimal("300.00"))
                .build();

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(testMovie));
        when(showRepository.findShowsByMovieCityAndDate(movieId, city, date))
                .thenReturn(Collections.singletonList(testShow));
        when(entityMapper.toMovieDTO(testMovie)).thenReturn(movieDTO);
        when(entityMapper.toTheaterDTO(testTheater)).thenReturn(theaterDTO);
        when(entityMapper.toShowDTO(testShow)).thenReturn(showDTO);
        when(showSeatRepository.countAvailableSeatsForShow(1L)).thenReturn(95L);

        // Act
        MovieSearchResponseDTO result = movieService.getMovieWithShows(movieId, city, date);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMovie());
        assertEquals("The Dark Knight", result.getMovie().getTitle());
        assertNotNull(result.getTheaterShows());
        assertFalse(result.getTheaterShows().isEmpty());

        verify(showRepository).findShowsByMovieCityAndDate(movieId, city, date);
    }

    @Test
    @DisplayName("Should get seats for show with availability")
    void getSeatsForShow_Success() {
        // Arrange
        Long showId = 1L;

        Seat seat1 = Seat.builder()
                .id(1L)
                .rowLabel("A")
                .seatNumber(1)
                .category(Seat.SeatCategory.REGULAR)
                .build();

        ShowSeat showSeat1 = ShowSeat.builder()
                .id(1L)
                .show(testShow)
                .seat(seat1)
                .status(ShowSeat.SeatStatus.AVAILABLE)
                .price(new BigDecimal("300.00"))
                .build();

        SeatDTO seatDTO = SeatDTO.builder()
                .seatId(1L)
                .showSeatId(1L)
                .rowLabel("A")
                .seatNumber(1)
                .seatIdentifier("A-1")
                .status(ShowSeat.SeatStatus.AVAILABLE)
                .price(new BigDecimal("300.00"))
                .build();

        when(showSeatRepository.findByShowIdWithSeat(showId))
                .thenReturn(Collections.singletonList(showSeat1));
        when(entityMapper.toSeatDTO(showSeat1)).thenReturn(seatDTO);

        // Act
        List<SeatDTO> result = movieService.getSeatsForShow(showId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A-1", result.get(0).getSeatIdentifier());
        assertEquals(ShowSeat.SeatStatus.AVAILABLE, result.get(0).getStatus());

        verify(showSeatRepository).findByShowIdWithSeat(showId);
    }

    @Test
    @DisplayName("Should return all genres")
    void getAllGenres_Success() {
        // Arrange
        List<String> expectedGenres = Arrays.asList("Action", "Comedy", "Drama", "Sci-Fi");
        when(movieRepository.findAllGenres()).thenReturn(expectedGenres);

        // Act
        List<String> result = movieService.getAllGenres();

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains("Action"));
        assertTrue(result.contains("Comedy"));

        verify(movieRepository).findAllGenres();
    }

    @Test
    @DisplayName("Should return all cities with theaters")
    void getAllCities_Success() {
        // Arrange
        List<String> expectedCities = Arrays.asList("Bangalore", "Mumbai", "Delhi");
        when(theaterRepository.findAllCities()).thenReturn(expectedCities);

        // Act
        List<String> result = movieService.getAllCities();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("Bangalore"));

        verify(theaterRepository).findAllCities();
    }
}

