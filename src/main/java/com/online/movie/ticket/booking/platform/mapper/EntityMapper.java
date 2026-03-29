package com.online.movie.ticket.booking.platform.mapper;

import com.online.movie.ticket.booking.platform.dto.*;
import com.online.movie.ticket.booking.platform.entity.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper class for converting between Entity and DTO objects.
 * Follows the Single Responsibility Principle for data transformation.
 */
@Component
public class EntityMapper {

    /**
     * Convert Movie entity to MovieDTO
     */
    public MovieDTO toMovieDTO(Movie movie) {
        if (movie == null) return null;

        return MovieDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .durationMinutes(movie.getDurationMinutes())
                .genre(movie.getGenre())
                .language(movie.getLanguage())
                .releaseDate(movie.getReleaseDate())
                .rating(movie.getRating())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .build();
    }

    /**
     * Convert Theater entity to TheaterDTO
     */
    public TheaterDTO toTheaterDTO(Theater theater) {
        if (theater == null) return null;

        return TheaterDTO.builder()
                .id(theater.getId())
                .name(theater.getName())
                .address(theater.getAddress())
                .city(theater.getCity())
                .state(theater.getState())
                .pincode(theater.getPincode())
                .contactNumber(theater.getContactNumber())
                .build();
    }

    /**
     * Convert Screen entity to ScreenDTO
     */
    public ScreenDTO toScreenDTO(Screen screen) {
        if (screen == null) return null;

        return ScreenDTO.builder()
                .id(screen.getId())
                .screenNumber(screen.getScreenNumber())
                .totalSeats(screen.getTotalSeats())
                .screenType(screen.getScreenType())
                .build();
    }

    /**
     * Convert Show entity to ShowDTO with related entities
     */
    public ShowDTO toShowDTO(Show show) {
        if (show == null) return null;

        return ShowDTO.builder()
                .id(show.getId())
                .showDate(show.getShowDate())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .basePrice(show.getBasePrice())
                .availableSeats(show.getAvailableSeats())
                .movie(toMovieDTO(show.getMovie()))
                .theater(show.getScreen() != null ? toTheaterDTO(show.getScreen().getTheater()) : null)
                .screen(toScreenDTO(show.getScreen()))
                .build();
    }

    /**
     * Convert ShowSeat entity to SeatDTO
     */
    public SeatDTO toSeatDTO(ShowSeat showSeat) {
        if (showSeat == null) return null;

        Seat seat = showSeat.getSeat();
        return SeatDTO.builder()
                .seatId(seat.getId())
                .showSeatId(showSeat.getId())
                .rowLabel(seat.getRowLabel())
                .seatNumber(seat.getSeatNumber())
                .seatIdentifier(seat.getSeatIdentifier())
                .category(seat.getCategory())
                .status(showSeat.getStatus())
                .price(showSeat.getPrice())
                .build();
    }

    /**
     * Convert Booking entity to BookingResponseDTO
     */
    public BookingResponseDTO toBookingResponseDTO(Booking booking) {
        if (booking == null) return null;

        Show show = booking.getShow();
        User user = booking.getUser();

        return BookingResponseDTO.builder()
                .bookingId(booking.getId())
                .bookingNumber(booking.getBookingNumber())
                .status(booking.getStatus())
                .bookingTime(booking.getBookingTime())
                .movieTitle(show.getMovie().getTitle())
                .theaterName(show.getScreen().getTheater().getName())
                .theaterAddress(show.getScreen().getTheater().getAddress())
                .screenNumber("Screen " + show.getScreen().getScreenNumber())
                .showDate(show.getShowDate().toString())
                .showTime(show.getStartTime().toString())
                .numberOfSeats(booking.getNumberOfSeats())
                .seatNumbers(booking.getBookedSeats().stream()
                        .map(ss -> ss.getSeat().getSeatIdentifier())
                        .sorted()
                        .collect(Collectors.toList()))
                .totalAmount(booking.getTotalAmount())
                .discountAmount(booking.getDiscountAmount())
                .finalAmount(booking.getFinalAmount())
                .userName(user.getFullName())
                .userEmail(user.getEmail())
                .build();
    }
}

