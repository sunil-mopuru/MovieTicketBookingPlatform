package com.online.movie.ticket.booking.platform.dto;

import com.online.movie.ticket.booking.platform.entity.Booking;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for booking confirmation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {
    private Long bookingId;
    private String bookingNumber;
    private Booking.BookingStatus status;
    private LocalDateTime bookingTime;

    // Movie & Show Details
    private String movieTitle;
    private String theaterName;
    private String theaterAddress;
    private String screenNumber;
    private String showDate;
    private String showTime;

    // Seat Details
    private Integer numberOfSeats;
    private List<String> seatNumbers;

    // Payment Details
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    // User Details
    private String userName;
    private String userEmail;
}

