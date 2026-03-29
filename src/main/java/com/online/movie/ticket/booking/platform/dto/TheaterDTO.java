package com.online.movie.ticket.booking.platform.dto;

import lombok.*;

/**
 * DTO for Theater details in API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TheaterDTO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String contactNumber;
}

