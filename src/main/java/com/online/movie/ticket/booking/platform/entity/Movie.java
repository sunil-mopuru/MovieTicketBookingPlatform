package com.online.movie.ticket.booking.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a Movie in the system.
 * Contains movie details including title, description, duration, genre, and ratings.
 */
@Entity
@Table(name = "movies", indexes = {
    @Index(name = "idx_movie_title", columnList = "title"),
    @Index(name = "idx_movie_genre", columnList = "genre"),
    @Index(name = "idx_movie_release_date", columnList = "releaseDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false, length = 50)
    private String genre;

    @Column(length = 50)
    private String language;

    private LocalDate releaseDate;

    @Column(length = 10)
    private String rating; // PG, PG-13, R, etc.

    @Column(length = 500)
    private String posterUrl;

    @Column(length = 500)
    private String trailerUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Show> shows = new HashSet<>();

    @Version
    private Long version; // Optimistic locking
}

