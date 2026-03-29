package com.online.movie.ticket.booking.platform.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * DTO for Movie details in API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDTO {
    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private String genre;
    private String language;
    private LocalDate releaseDate;
    private String rating;
    private String posterUrl;
    private String trailerUrl;
}

