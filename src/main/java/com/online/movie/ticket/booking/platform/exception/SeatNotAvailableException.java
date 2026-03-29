package com.online.movie.ticket.booking.platform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when seats are not available for booking.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class SeatNotAvailableException extends RuntimeException {

    public SeatNotAvailableException(String message) {
        super(message);
    }

    public SeatNotAvailableException(int requestedSeats, int availableSeats) {
        super(String.format("Requested %d seats but only %d are available", requestedSeats, availableSeats));
    }
}

