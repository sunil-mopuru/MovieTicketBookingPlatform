package com.online.movie.ticket.booking.platform.repository;

import com.online.movie.ticket.booking.platform.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Payment entity operations.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Find payment by booking ID
     */
    Optional<Payment> findByBookingId(Long bookingId);

    /**
     * Check if payment exists for a booking
     */
    boolean existsByBookingId(Long bookingId);
}

