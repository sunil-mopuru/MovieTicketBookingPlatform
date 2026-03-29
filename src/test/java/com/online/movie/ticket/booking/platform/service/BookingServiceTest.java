package com.online.movie.ticket.booking.platform.service;

import com.online.movie.ticket.booking.platform.dto.*;
import com.online.movie.ticket.booking.platform.entity.*;
import com.online.movie.ticket.booking.platform.exception.SeatNotAvailableException;
import com.online.movie.ticket.booking.platform.mapper.EntityMapper;
import com.online.movie.ticket.booking.platform.repository.*;
import com.online.movie.ticket.booking.platform.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookingService (WRITE Scenario).
 * Tests the core booking functionality including seat reservation and concurrency handling.
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User testUser;
    private Show testShow;
    private Movie testMovie;
    private Theater testTheater;
    private Screen testScreen;
    private List<ShowSeat> testSeats;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .build();

        testTheater = Theater.builder()
                .id(1L)
                .name("PVR Cinemas")
                .city("Bangalore")
                .address("MG Road")
                .build();

        testScreen = Screen.builder()
                .id(1L)
                .theater(testTheater)
                .screenNumber(1)
                .totalSeats(100)
                .build();

        testMovie = Movie.builder()
                .id(1L)
                .title("The Dark Knight")
                .durationMinutes(152)
                .build();

        testShow = Show.builder()
                .id(1L)
                .movie(testMovie)
                .screen(testScreen)
                .showDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 30))
                .basePrice(new BigDecimal("300.00"))
                .availableSeats(100)
                .isActive(true)
                .build();

        // Create test seats
        Seat seat1 = Seat.builder()
                .id(1L)
                .screen(testScreen)
                .rowLabel("A")
                .seatNumber(1)
                .category(Seat.SeatCategory.REGULAR)
                .build();

        Seat seat2 = Seat.builder()
                .id(2L)
                .screen(testScreen)
                .rowLabel("A")
                .seatNumber(2)
                .category(Seat.SeatCategory.REGULAR)
                .build();

        ShowSeat showSeat1 = ShowSeat.builder()
                .id(1L)
                .show(testShow)
                .seat(seat1)
                .status(ShowSeat.SeatStatus.AVAILABLE)
                .price(new BigDecimal("300.00"))
                .build();

        ShowSeat showSeat2 = ShowSeat.builder()
                .id(2L)
                .show(testShow)
                .seat(seat2)
                .status(ShowSeat.SeatStatus.AVAILABLE)
                .price(new BigDecimal("300.00"))
                .build();

        testSeats = Arrays.asList(showSeat1, showSeat2);
    }

    @Test
    @DisplayName("Should create booking successfully when seats are available")
    void createBooking_Success() {
        // Arrange
        BookingRequestDTO request = BookingRequestDTO.builder()
                .userId(1L)
                .showId(1L)
                .seatIds(Arrays.asList(1L, 2L))
                .build();

        Booking savedBooking = Booking.builder()
                .id(1L)
                .bookingNumber("BK-20260329-ABCDE")
                .user(testUser)
                .show(testShow)
                .numberOfSeats(2)
                .totalAmount(new BigDecimal("600.00"))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("600.00"))
                .status(Booking.BookingStatus.PENDING)
                .build();

        BookingResponseDTO expectedResponse = BookingResponseDTO.builder()
                .bookingId(1L)
                .bookingNumber("BK-20260329-ABCDE")
                .status(Booking.BookingStatus.PENDING)
                .numberOfSeats(2)
                .totalAmount(new BigDecimal("600.00"))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
        when(bookingRepository.hasExistingPendingBooking(1L, 1L)).thenReturn(false);
        when(showSeatRepository.findByIdInAndShowIdWithLock(anyList(), eq(1L))).thenReturn(testSeats);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(showSeatRepository.saveAll(anyList())).thenReturn(testSeats);
        when(entityMapper.toBookingResponseDTO(any(Booking.class))).thenReturn(expectedResponse);

        // Act
        BookingResponseDTO result = bookingService.createBooking(request);

        // Assert
        assertNotNull(result);
        assertEquals("BK-20260329-ABCDE", result.getBookingNumber());
        assertEquals(Booking.BookingStatus.PENDING, result.getStatus());
        assertEquals(2, result.getNumberOfSeats());

        verify(showSeatRepository).findByIdInAndShowIdWithLock(anyList(), eq(1L));
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when seats are already booked")
    void createBooking_SeatsNotAvailable() {
        // Arrange
        BookingRequestDTO request = BookingRequestDTO.builder()
                .userId(1L)
                .showId(1L)
                .seatIds(Arrays.asList(1L, 2L))
                .build();

        // Make one seat already booked
        testSeats.get(0).setStatus(ShowSeat.SeatStatus.BOOKED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
        when(bookingRepository.hasExistingPendingBooking(1L, 1L)).thenReturn(false);
        when(showSeatRepository.findByIdInAndShowIdWithLock(anyList(), eq(1L))).thenReturn(testSeats);

        // Act & Assert
        assertThrows(SeatNotAvailableException.class, () -> {
            bookingService.createBooking(request);
        });
    }

    @Test
    @DisplayName("Should confirm booking successfully")
    void confirmBooking_Success() {
        // Arrange
        Booking pendingBooking = Booking.builder()
                .id(1L)
                .bookingNumber("BK-20260329-ABCDE")
                .user(testUser)
                .show(testShow)
                .numberOfSeats(2)
                .totalAmount(new BigDecimal("600.00"))
                .finalAmount(new BigDecimal("600.00"))
                .status(Booking.BookingStatus.PENDING)
                .build();

        BookingResponseDTO expectedResponse = BookingResponseDTO.builder()
                .bookingId(1L)
                .bookingNumber("BK-20260329-ABCDE")
                .status(Booking.BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());
        when(showSeatRepository.findByBookingId(1L)).thenReturn(testSeats);
        when(showSeatRepository.saveAll(anyList())).thenReturn(testSeats);
        when(showRepository.save(any(Show.class))).thenReturn(testShow);
        when(bookingRepository.save(any(Booking.class))).thenReturn(pendingBooking);
        when(entityMapper.toBookingResponseDTO(any(Booking.class))).thenReturn(expectedResponse);

        // Act
        BookingResponseDTO result = bookingService.confirmBooking(1L, "TXN123456");

        // Assert
        assertNotNull(result);
        assertEquals(Booking.BookingStatus.CONFIRMED, result.getStatus());

        verify(paymentRepository).save(any(Payment.class));
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should cancel booking and release seats")
    void cancelBooking_Success() {
        // Arrange
        Booking confirmedBooking = Booking.builder()
                .id(1L)
                .bookingNumber("BK-20260329-ABCDE")
                .user(testUser)
                .show(testShow)
                .numberOfSeats(2)
                .status(Booking.BookingStatus.CONFIRMED)
                .build();

        testSeats.forEach(s -> s.setStatus(ShowSeat.SeatStatus.BOOKED));

        BookingResponseDTO expectedResponse = BookingResponseDTO.builder()
                .bookingId(1L)
                .status(Booking.BookingStatus.CANCELLED)
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(confirmedBooking));
        when(showSeatRepository.findByBookingId(1L)).thenReturn(testSeats);
        when(showSeatRepository.saveAll(anyList())).thenReturn(testSeats);
        when(showRepository.save(any(Show.class))).thenReturn(testShow);
        when(bookingRepository.save(any(Booking.class))).thenReturn(confirmedBooking);
        when(entityMapper.toBookingResponseDTO(any(Booking.class))).thenReturn(expectedResponse);

        // Act
        BookingResponseDTO result = bookingService.cancelBooking(1L, "Changed plans");

        // Assert
        assertNotNull(result);
        assertEquals(Booking.BookingStatus.CANCELLED, result.getStatus());

        // Verify seats are released
        verify(showSeatRepository).saveAll(argThat(seats -> {
            List<ShowSeat> seatList = (List<ShowSeat>) seats;
            return seatList.stream().allMatch(s -> s.getStatus() == ShowSeat.SeatStatus.AVAILABLE);
        }));
    }
}

