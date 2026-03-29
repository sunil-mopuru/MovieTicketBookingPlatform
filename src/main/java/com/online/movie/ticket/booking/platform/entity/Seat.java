package com.online.movie.ticket.booking.platform.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a Seat within a Screen.
 * Contains seat details including row, number, and category for pricing.
 */
@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"screen_id", "rowLabel", "seatNumber"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5)
    private String rowLabel; // A, B, C, etc.

    @Column(nullable = false)
    private Integer seatNumber; // 1, 2, 3, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SeatCategory category = SeatCategory.REGULAR;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    public enum SeatCategory {
        REGULAR, PREMIUM, VIP, RECLINER
    }

    /**
     * Returns the seat identifier (e.g., "A-1", "B-15")
     */
    public String getSeatIdentifier() {
        return rowLabel + "-" + seatNumber;
    }
}

