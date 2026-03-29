package com.online.movie.ticket.booking.platform.dto;

import com.online.movie.ticket.booking.platform.entity.Seat;
import com.online.movie.ticket.booking.platform.entity.ShowSeat;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for Seat availability information for a show.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {
    private Long seatId;
    private Long showSeatId;
    private String rowLabel;
    private Integer seatNumber;
    private String seatIdentifier;
    private Seat.SeatCategory category;
    private ShowSeat.SeatStatus status;
    private BigDecimal price;
}

