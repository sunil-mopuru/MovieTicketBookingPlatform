package com.online.movie.ticket.booking.platform.repository;

import com.online.movie.ticket.booking.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Check if email exists
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Find user by phone number
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
}

