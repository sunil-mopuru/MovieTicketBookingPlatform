package com.online.movie.ticket.booking.platform.dto;

import lombok.*;

import java.util.List;

/**
 * Response DTO for movie search results with shows grouped by theater.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieSearchResponseDTO {
    private MovieDTO movie;
    private List<TheaterShowsDTO> theaterShows;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TheaterShowsDTO {
        private TheaterDTO theater;
        private List<ShowDTO> shows;
    }
}

