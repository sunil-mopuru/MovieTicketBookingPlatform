package com.online.movie.ticket.booking.platform.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for Show details including movie and theater information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowDTO {
    private Long id;
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal basePrice;
    private Integer availableSeats;
    private MovieDTO movie;
    private TheaterDTO theater;
    private ScreenDTO screen;
}

