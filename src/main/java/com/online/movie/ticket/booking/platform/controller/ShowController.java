package com.online.movie.ticket.booking.platform.controller;

import com.online.movie.ticket.booking.platform.dto.*;
import com.online.movie.ticket.booking.platform.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Show-related operations.
 * Provides endpoints for viewing show details and seat availability.
 */
@RestController
@RequestMapping("/api/v1/shows")
@RequiredArgsConstructor
@Slf4j
public class ShowController {

    private final MovieService movieService;

    /**
     * Get seat layout and availability for a show.
     *
     * GET /api/v1/shows/{showId}/seats
     */
    @GetMapping("/{showId}/seats")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> getSeatsForShow(@PathVariable Long showId) {
        log.info("Getting seats for show: {}", showId);

        List<SeatDTO> seats = movieService.getSeatsForShow(showId);

        return ResponseEntity.ok(ApiResponse.success(seats, "Seats retrieved successfully"));
    }
}

