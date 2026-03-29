package com.online.movie.ticket.booking.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a Screen within a Theater.
 * Contains screen details including capacity and seat configuration.
 */
@Entity
@Table(name = "screens", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"theater_id", "screenNumber"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer screenNumber;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer totalRows;

    @Column(nullable = false)
    private Integer seatsPerRow;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ScreenType screenType = ScreenType.STANDARD;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Seat> seats = new HashSet<>();

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Show> shows = new HashSet<>();

    @Version
    private Long version;

    public enum ScreenType {
        STANDARD, IMAX, DOLBY_ATMOS, THREE_D, FOUR_DX
    }
}

