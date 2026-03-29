package com.online.movie.ticket.booking.platform.dto;

import com.online.movie.ticket.booking.platform.entity.Screen;
import lombok.*;

/**
 * DTO for Screen details in API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenDTO {
    private Long id;
    private Integer screenNumber;
    private Integer totalSeats;
    private Screen.ScreenType screenType;
}

