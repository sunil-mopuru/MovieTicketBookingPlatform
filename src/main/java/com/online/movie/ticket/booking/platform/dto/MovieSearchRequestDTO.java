package com.online.movie.ticket.booking.platform.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * Request DTO for searching movies with various filters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieSearchRequestDTO {
    private String title;
    private String genre;
    private String language;
    private String city;
    private LocalDate date;
    private Integer page;
    private Integer size;
}

