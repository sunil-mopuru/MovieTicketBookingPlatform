package com.online.movie.ticket.booking.platform.config;

import com.online.movie.ticket.booking.platform.entity.*;
import com.online.movie.ticket.booking.platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data initializer for development/testing purposes.
 * Loads sample data into the database on application startup.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("!prod") // Don't run in production
public class DataInitializer {

    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("Initializing sample data...");

            // Create sample users
            User user1 = createUser("John", "Doe", "john.doe@email.com", "9876543210");
            User user2 = createUser("Jane", "Smith", "jane.smith@email.com", "9876543211");

            // Create sample movies
            Movie movie1 = createMovie("The Dark Knight", "A gripping superhero thriller",
                    152, "Action", "English", "PG-13");
            Movie movie2 = createMovie("Inception", "A mind-bending sci-fi thriller",
                    148, "Sci-Fi", "English", "PG-13");
            Movie movie3 = createMovie("3 Idiots", "A heartwarming comedy-drama",
                    170, "Comedy", "Hindi", "PG");
            Movie movie4 = createMovie("RRR", "An epic action drama",
                    187, "Action", "Telugu", "PG-13");

            // Create theaters and screens
            Theater theater1 = createTheater("PVR Cinemas", "MG Road", "Bangalore", "Karnataka", "560001");
            Theater theater2 = createTheater("INOX Movies", "Phoenix Mall", "Bangalore", "Karnataka", "560004");
            Theater theater3 = createTheater("Cinepolis", "Orion Mall", "Bangalore", "Karnataka", "560010");

            Screen screen1a = createScreen(theater1, 1, 100, 10, 10, Screen.ScreenType.STANDARD);
            Screen screen1b = createScreen(theater1, 2, 80, 8, 10, Screen.ScreenType.IMAX);
            Screen screen2a = createScreen(theater2, 1, 120, 12, 10, Screen.ScreenType.DOLBY_ATMOS);
            Screen screen3a = createScreen(theater3, 1, 90, 9, 10, Screen.ScreenType.THREE_D);

            // Create seats for screens
            createSeatsForScreen(screen1a);
            createSeatsForScreen(screen1b);
            createSeatsForScreen(screen2a);
            createSeatsForScreen(screen3a);

            // Create shows for today and tomorrow
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            // Shows for The Dark Knight
            Show show1 = createShow(movie1, screen1a, today, LocalTime.of(10, 0), new BigDecimal("250.00"));
            Show show2 = createShow(movie1, screen1a, today, LocalTime.of(14, 0), new BigDecimal("300.00"));
            Show show3 = createShow(movie1, screen2a, today, LocalTime.of(18, 0), new BigDecimal("350.00"));
            Show show4 = createShow(movie1, screen1a, tomorrow, LocalTime.of(10, 0), new BigDecimal("250.00"));

            // Shows for Inception
            Show show5 = createShow(movie2, screen1b, today, LocalTime.of(11, 0), new BigDecimal("400.00"));
            Show show6 = createShow(movie2, screen3a, today, LocalTime.of(15, 0), new BigDecimal("350.00"));

            // Shows for 3 Idiots
            Show show7 = createShow(movie3, screen2a, today, LocalTime.of(12, 0), new BigDecimal("280.00"));

            // Shows for RRR
            Show show8 = createShow(movie4, screen3a, today, LocalTime.of(19, 0), new BigDecimal("320.00"));

            // Create show seats
            createShowSeats(show1);
            createShowSeats(show2);
            createShowSeats(show3);
            createShowSeats(show4);
            createShowSeats(show5);
            createShowSeats(show6);
            createShowSeats(show7);
            createShowSeats(show8);

            log.info("Sample data initialization completed!");
            log.info("Created {} users, {} movies, {} theaters, {} shows",
                    userRepository.count(), movieRepository.count(),
                    theaterRepository.count(), showRepository.count());
        };
    }

    private User createUser(String firstName, String lastName, String email, String phone) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phoneNumber(phone)
                .passwordHash("$2a$10$encrypted") // Placeholder
                .role(User.UserRole.CUSTOMER)
                .build();
        return userRepository.save(user);
    }

    private Movie createMovie(String title, String description, int duration,
                              String genre, String language, String rating) {
        Movie movie = Movie.builder()
                .title(title)
                .description(description)
                .durationMinutes(duration)
                .genre(genre)
                .language(language)
                .rating(rating)
                .releaseDate(LocalDate.now().minusDays(30))
                .isActive(true)
                .build();
        return movieRepository.save(movie);
    }

    private Theater createTheater(String name, String address, String city, String state, String pincode) {
        Theater theater = Theater.builder()
                .name(name)
                .address(address)
                .city(city)
                .state(state)
                .pincode(pincode)
                .isActive(true)
                .build();
        return theaterRepository.save(theater);
    }

    private Screen createScreen(Theater theater, int screenNumber, int totalSeats,
                                int rows, int seatsPerRow, Screen.ScreenType type) {
        Screen screen = Screen.builder()
                .theater(theater)
                .screenNumber(screenNumber)
                .totalSeats(totalSeats)
                .totalRows(rows)
                .seatsPerRow(seatsPerRow)
                .screenType(type)
                .isActive(true)
                .build();
        return screenRepository.save(screen);
    }

    private void createSeatsForScreen(Screen screen) {
        List<Seat> seats = new ArrayList<>();
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"};

        int rowCount = screen.getTotalRows();
        int seatsPerRow = screen.getSeatsPerRow();

        for (int r = 0; r < rowCount; r++) {
            for (int s = 1; s <= seatsPerRow; s++) {
                Seat.SeatCategory category;
                if (r < 2) {
                    category = Seat.SeatCategory.REGULAR;
                } else if (r < rowCount - 2) {
                    category = Seat.SeatCategory.PREMIUM;
                } else {
                    category = Seat.SeatCategory.VIP;
                }

                Seat seat = Seat.builder()
                        .screen(screen)
                        .rowLabel(rows[r])
                        .seatNumber(s)
                        .category(category)
                        .isAvailable(true)
                        .build();
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);
    }

    private Show createShow(Movie movie, Screen screen, LocalDate date, LocalTime startTime, BigDecimal price) {
        LocalTime endTime = startTime.plusMinutes(movie.getDurationMinutes() + 15); // 15 min buffer

        Show show = Show.builder()
                .movie(movie)
                .screen(screen)
                .showDate(date)
                .startTime(startTime)
                .endTime(endTime)
                .basePrice(price)
                .availableSeats(screen.getTotalSeats())
                .isActive(true)
                .build();
        return showRepository.save(show);
    }

    private void createShowSeats(Show show) {
        List<Seat> screenSeats = seatRepository.findByScreenIdOrderByRowLabelAscSeatNumberAsc(
                show.getScreen().getId());

        List<ShowSeat> showSeats = new ArrayList<>();
        for (Seat seat : screenSeats) {
            BigDecimal seatPrice = calculateSeatPrice(show.getBasePrice(), seat.getCategory());

            ShowSeat showSeat = ShowSeat.builder()
                    .show(show)
                    .seat(seat)
                    .status(ShowSeat.SeatStatus.AVAILABLE)
                    .price(seatPrice)
                    .build();
            showSeats.add(showSeat);
        }
        showSeatRepository.saveAll(showSeats);
    }

    private BigDecimal calculateSeatPrice(BigDecimal basePrice, Seat.SeatCategory category) {
        return switch (category) {
            case PREMIUM -> basePrice.multiply(new BigDecimal("1.5"));
            case VIP -> basePrice.multiply(new BigDecimal("2.0"));
            case RECLINER -> basePrice.multiply(new BigDecimal("2.5"));
            default -> basePrice;
        };
    }
}

