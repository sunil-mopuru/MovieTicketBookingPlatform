# 🎬 Online Movie Ticket Booking Platform

A Spring Boot application for booking movie tickets online with seat reservation and concurrency handling.

---

## 📋 Table of Contents
- [Features Implemented](#features-implemented)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Data Model](#data-model)
- [API Endpoints](#api-endpoints)
- [How to Run](#how-to-run)
- [How to Test](#how-to-test)
- [Documentation](#documentation)

---

## ✅ Features Implemented

### READ Scenario: Movie Search & Browse
- Search movies by title, genre, language, city, and date
- View movie details with shows grouped by theater
- View seat availability for a specific show
- Get reference data (genres, languages, cities)

### WRITE Scenario: Ticket Booking
- Create booking with seat reservation
- Pessimistic locking to prevent double-booking
- Dynamic pricing by seat category (Regular, Premium, VIP, Recliner)
- Coupon/discount support
- Confirm booking after payment
- Cancel booking with seat release

---

## 🛠️ Technology Stack

| Technology | Purpose |
|------------|---------|
| Spring Boot 3.2.3 | Application Framework |
| Java 17 | Programming Language |
| Spring Data JPA | Database Access |
| H2 Database | In-Memory Database (Dev) |
| Lombok | Boilerplate Reduction |
| Maven | Build Tool |

---

## 📁 Project Structure

```
src/main/java/com/online/movie/ticket/booking/platform/
├── controller/          # REST Controllers
│   ├── MovieController.java
│   ├── BookingController.java
│   └── ShowController.java
├── service/             # Business Logic
│   ├── MovieService.java
│   ├── BookingService.java
│   └── impl/
│       ├── MovieServiceImpl.java
│       └── BookingServiceImpl.java
├── repository/          # Data Access (9 repositories)
├── entity/              # JPA Entities (9 entities)
├── dto/                 # Data Transfer Objects (10 DTOs)
├── mapper/              # Entity-DTO Mapper
├── exception/           # Exception Handling
└── config/              # Configuration
```

---

## 📊 Data Model

### Entities (9)
| Entity | Description |
|--------|-------------|
| Movie | Movie information (title, genre, language, duration) |
| Theater | Cinema complex (name, address, city) |
| Screen | Auditorium (screen number, type, capacity) |
| Seat | Physical seat (row, number, category) |
| Show | Movie screening (date, time, price) |
| ShowSeat | Seat availability per show (status, price) |
| User | Customer information |
| Booking | Reservation details (status, amount) |
| Payment | Payment transaction |

### Key Relationships
- Theater → has many → Screens
- Screen → has many → Seats
- Screen → has many → Shows
- Movie → has many → Shows
- Show → has many → ShowSeats
- User → has many → Bookings
- Booking → has one → Payment

---

## 🔌 API Endpoints

### Movie APIs (READ)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/movies/now-showing` | Get all running movies |
| GET | `/api/v1/movies/search` | Search with filters |
| GET | `/api/v1/movies/{id}` | Get movie by ID |
| GET | `/api/v1/movies/{id}/shows?city=&date=` | Get shows by city & date |
| GET | `/api/v1/movies/genres` | Get all genres |
| GET | `/api/v1/movies/languages` | Get all languages |
| GET | `/api/v1/movies/cities` | Get all cities |

### Show APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/shows/{id}/seats` | Get seat availability |

### Booking APIs (WRITE)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/bookings` | Create booking |
| PUT | `/api/v1/bookings/{id}/confirm` | Confirm booking |
| PUT | `/api/v1/bookings/{id}/cancel` | Cancel booking |
| GET | `/api/v1/bookings/{id}` | Get booking by ID |
| GET | `/api/v1/bookings/user/{userId}` | Get user's bookings |

---

## 🚀 How to Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Steps
```bash
# Clone the repository
git clone <repository-url>
cd TicketBookingPlatForm

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### Access
- **Application**: http://localhost:8080
- **H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:moviedb`
  - Username: `sa`
  - Password: *(empty)*

---

## 🧪 How to Test

### Sample Test Flow

#### 1. Get Movies
```
GET http://localhost:8080/api/v1/movies/now-showing
```

#### 2. Get Shows for a Movie
```
GET http://localhost:8080/api/v1/movies/1/shows?city=Bangalore&date=2026-03-29
```

#### 3. Get Seat Availability
```
GET http://localhost:8080/api/v1/shows/1/seats
```

#### 4. Create Booking
```
POST http://localhost:8080/api/v1/bookings
Content-Type: application/json

{
    "userId": 1,
    "showId": 1,
    "seatIds": [1, 2, 3]
}
```

#### 5. Confirm Booking
```
PUT http://localhost:8080/api/v1/bookings/1/confirm?transactionId=TXN123456
```

### Pre-loaded Test Data
- **Users**: 2 (John Doe, Jane Smith)
- **Movies**: 4 (The Dark Knight, Inception, 3 Idiots, RRR)
- **Theaters**: 3 (PVR, INOX, Cinepolis - all in Bangalore)
- **Shows**: 8 (various timings)
- **Seats**: 100 per screen

---

## 📄 Documentation

| File | Description |
|------|-------------|
| `SOLUTION_DOCUMENTATION.md` | Complete solution details |
| `TESTING_GUIDE.md` | Detailed testing instructions |
| `PRESENTATION.md` | Presentation content |
| `PRESENTATION.html` | Visual presentation (open in browser) |
| `UML_DIAGRAMS.md` | All UML diagrams in text format |
| `diagrams/*.puml` | PlantUML diagram files |

### UML Diagrams Included
- Class Diagram (9 entities)
- Component Diagram (4-layer architecture)
- Sequence Diagram - READ (Movie search flow)
- Sequence Diagram - WRITE (Booking flow with locking)
- State Diagram - Booking (PENDING → CONFIRMED/CANCELLED)
- State Diagram - Seat (AVAILABLE → BLOCKED → BOOKED)
- Activity Diagram (Complete booking flow)
- High-Level Architecture

---

## 🔒 Key Technical Highlights

### Concurrency Handling
- **Pessimistic Locking**: `@Lock(PESSIMISTIC_WRITE)` on seat selection
- **Transaction Isolation**: `SERIALIZABLE` for booking operations
- **Optimistic Locking**: `@Version` on entities

### Design Patterns
- Repository Pattern
- DTO Pattern
- Builder Pattern (Lombok)
- Strategy Pattern (Pricing)

### SOLID Principles
- Single Responsibility (separate services)
- Interface Segregation (read/write separation)
- Dependency Inversion (repository interfaces)

---

## 👤 Author
Online Movie Ticket Booking Platform - Technical Assignment

