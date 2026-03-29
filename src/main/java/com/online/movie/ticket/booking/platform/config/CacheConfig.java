package com.online.movie.ticket.booking.platform.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for the application.
 * Uses in-memory caching for frequently accessed data.
 *
 * In production, this would be replaced with Redis
 * for distributed caching across multiple instances.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "movies",       // Movie details cache
                "genres",       // Genre list cache
                "languages",    // Language list cache
                "cities"        // City list cache
        );
    }
}

