# Online Movie Ticket Booking Platform - Solution Documentation

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [System Architecture](#system-architecture)
3. [Data Model](#data-model)
4. [Implemented Scenarios](#implemented-scenarios)
5. [Design Principles](#design-principles)
6. [Non-Functional Requirements](#non-functional-requirements)
7. [Platform Provisioning](#platform-provisioning)
8. [API Documentation](#api-documentation)
9. [Areas Not Covered](#areas-not-covered)

---

## 1. Executive Summary

This solution implements an **Online Movie Ticket Booking Platform** using Spring Boot 3.2.x. The implementation includes:

- **Read Scenario**: Movie Search & Browse with Shows (fully implemented)
- **Write Scenario**: Ticket Booking with Seat Reservation (fully implemented)

### Technologies Used
- **Framework**: Spring Boot 3.2.3
- **Language**: Java 17
- **Database**: H2 (Development) / MySQL (Production)
- **ORM**: Spring Data JPA with Hibernate
- **Build Tool**: Maven
- **Additional**: Lombok, Spring Validation, Spring Cache

---

## 2. System Architecture

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              PRESENTATION LAYER                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐              │
│  │  Movie Controller │  │ Booking Controller│  │  Show Controller │              │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘              │
└───────────┼────────────────────┼────────────────────┼───────────────────────┘
            │                    │                    │
            ▼                    ▼                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              SERVICE LAYER                                   │
│  ┌─────────────────┐  ┌─────────────────┐                                   │
│  │  MovieService   │  │  BookingService │                                   │
│  │  (READ Scenario)│  │  (WRITE Scenario)│                                   │
│  └────────┬────────┘  └────────┬────────┘                                   │
│           │                    │                                            │
│  ┌────────┴────────────────────┴────────┐                                   │
│  │           Entity Mapper              │                                   │
│  └────────┬────────────────────┬────────┘                                   │
└───────────┼────────────────────┼────────────────────────────────────────────┘
            │                    │
            ▼                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           REPOSITORY LAYER                                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │  Movie   │ │ Theater  │ │  Show    │ │ Booking  │ │ ShowSeat │           │
│  │Repository│ │Repository│ │Repository│ │Repository│ │Repository│           │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘           │
└───────┼────────────┼────────────┼────────────┼────────────┼─────────────────┘
        │            │            │            │            │
        ▼            ▼            ▼            ▼            ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              DATABASE LAYER                                  │
│                        (H2 / MySQL / PostgreSQL)                            │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Package Structure

```
com.online.movie.ticket.booking.platform/
├── controller/          # REST API Controllers
│   ├── MovieController.java
│   ├── ShowController.java
│   └── BookingController.java
├── service/             # Business Logic Layer
│   ├── MovieService.java
│   ├── BookingService.java
│   └── impl/
│       ├── MovieServiceImpl.java
│       └── BookingServiceImpl.java
├── repository/          # Data Access Layer
│   ├── MovieRepository.java
│   ├── TheaterRepository.java
│   ├── ShowRepository.java
│   ├── BookingRepository.java
│   └── ...
├── entity/              # JPA Entities
│   ├── Movie.java
│   ├── Theater.java
│   ├── Screen.java
│   ├── Seat.java
│   ├── Show.java
│   ├── ShowSeat.java
│   ├── User.java
│   ├── Booking.java
│   └── Payment.java
├── dto/                 # Data Transfer Objects
│   ├── MovieDTO.java
│   ├── BookingRequestDTO.java
│   ├── BookingResponseDTO.java
│   └── ApiResponse.java
├── mapper/              # Entity-DTO Mappers
│   └── EntityMapper.java
├── exception/           # Custom Exceptions
│   ├── ResourceNotFoundException.java
│   ├── SeatNotAvailableException.java
│   ├── BookingException.java
│   └── GlobalExceptionHandler.java
└── config/              # Configuration Classes
    ├── CacheConfig.java
    └── DataInitializer.java
```

---

## 3. Data Model

### 3.1 Entity Relationship Diagram

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│    USER     │     │   THEATER   │     │    MOVIE    │
├─────────────┤     ├─────────────┤     ├─────────────┤
│ id (PK)     │     │ id (PK)     │     │ id (PK)     │
│ firstName   │     │ name        │     │ title       │
│ lastName    │     │ address     │     │ description │
│ email (UK)  │     │ city        │     │ duration    │
│ phoneNumber │     │ state       │     │ genre       │
│ passwordHash│     │ pincode     │     │ language    │
│ role        │     │ isActive    │     │ rating      │
│ isActive    │     │ version     │     │ isActive    │
│ version     │     └──────┬──────┘     │ version     │
└──────┬──────┘            │            └──────┬──────┘
       │                   │                   │
       │            ┌──────┴──────┐            │
       │            │   SCREEN    │            │
       │            ├─────────────┤            │
       │            │ id (PK)     │            │
       │            │ theater_id  │◄───────────┘
       │            │ screenNumber│                    
       │            │ totalSeats  │            
       │            │ screenType  │            
       │            │ version     │            
       │            └──────┬──────┘            
       │                   │                   
       │            ┌──────┴──────┐            
       │            │    SEAT     │            
       │            ├─────────────┤            
       │            │ id (PK)     │            
       │            │ screen_id   │            
       │            │ rowLabel    │            
       │            │ seatNumber  │            
       │            │ category    │            
       │            └──────┬──────┘            
       │                   │                   
       │  ┌────────────────┼────────────────┐  
       │  │                │                │  
       │  │         ┌──────┴──────┐         │  
       │  │         │    SHOW     │◄────────┼──┐
       │  │         ├─────────────┤         │  │
       │  │         │ id (PK)     │         │  │
       │  │         │ movie_id    │─────────┼──┘
       │  │         │ screen_id   │         │
       │  │         │ showDate    │         │
       │  │         │ startTime   │         │
       │  │         │ basePrice   │         │
       │  │         │ version     │         │
       │  │         └──────┬──────┘         │
       │  │                │                │
       │  │         ┌──────┴──────┐         │
       │  │         │  SHOW_SEAT  │         │
       │  │         ├─────────────┤         │
       │  │         │ id (PK)     │         │
       │  │         │ show_id     │         │
       │  │         │ seat_id     │◄────────┘
       │  │         │ booking_id  │
       │  │         │ status      │
       │  │         │ price       │
       │  │         │ version     │
       │  │         └──────┬──────┘
       │  │                │
       │  │         ┌──────┴──────┐
       │  └────────►│   BOOKING   │
       │            ├─────────────┤
       └───────────►│ id (PK)     │
                    │ bookingNumber│
                    │ user_id     │
                    │ show_id     │
                    │ totalAmount │
                    │ status      │
                    │ version     │
                    └──────┬──────┘
                           │
                    ┌──────┴──────┐
                    │   PAYMENT   │
                    ├─────────────┤
                    │ id (PK)     │
                    │ booking_id  │
                    │ transactionId│
                    │ amount      │
                    │ status      │
                    │ version     │
                    └─────────────┘
```

### 3.2 Key Entity Descriptions

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| **Movie** | Stores movie information | title, duration, genre, language |
| **Theater** | Cinema complex details | name, address, city |
| **Screen** | Individual screening room | screenNumber, totalSeats, screenType |
| **Seat** | Physical seat in a screen | rowLabel, seatNumber, category |
| **Show** | Movie screening at specific time | showDate, startTime, basePrice |
| **ShowSeat** | Seat availability per show | status (AVAILABLE/BLOCKED/BOOKED), price |
| **Booking** | Reservation record | bookingNumber, status, totalAmount |
| **Payment** | Payment transaction | transactionId, paymentMethod, status |
| **User** | Customer information | email, phoneNumber, role |

---

## 4. Implemented Scenarios

### 4.1 READ Scenario: Movie Search & Browse

**Implementation**: `MovieServiceImpl.java`

**Flow**:
1. User searches for movies by title/genre/language/city/date
2. System returns matching movies with available shows
3. User selects a movie to view shows grouped by theater
4. User selects a show to view seat availability

**API Endpoints**:
```
GET /api/v1/movies/search?title=&genre=&city=&date=
GET /api/v1/movies/{movieId}
GET /api/v1/movies/{movieId}/shows?city=Bangalore&date=2026-03-29
GET /api/v1/shows/{showId}/seats
```

**Key Features**:
- Dynamic filtering with multiple optional parameters
- Shows grouped by theater for better UX
- Real-time seat availability count
- Caching for reference data (genres, languages, cities)

### 4.2 WRITE Scenario: Ticket Booking

**Implementation**: `BookingServiceImpl.java`

**Flow**:
1. User selects seats for a show
2. System validates seat availability
3. System locks seats (pessimistic locking)
4. System calculates pricing with seat categories
5. System creates booking with PENDING status
6. User completes payment
7. System confirms booking and marks seats as BOOKED

**API Endpoints**:
```
POST /api/v1/bookings              # Create booking
PUT /api/v1/bookings/{id}/confirm  # Confirm after payment
PUT /api/v1/bookings/{id}/cancel   # Cancel booking
GET /api/v1/bookings/{id}          # Get booking details
```

**Concurrency Handling**:
- **Pessimistic Locking**: Used during seat selection to prevent double-booking
- **Optimistic Locking**: Version fields on all entities for concurrent updates
- **SERIALIZABLE Isolation**: For critical booking transactions
- **Atomic Operations**: All-or-nothing seat reservation

**Pricing Logic**:
- Base price from show
- Multipliers by seat category:
  - REGULAR: 1.0x
  - PREMIUM: 1.5x
  - VIP: 2.0x
  - RECLINER: 2.5x
- Coupon discounts supported

---

## 5. Design Principles

### 5.1 SOLID Principles Applied

| Principle | Application |
|-----------|-------------|
| **Single Responsibility** | Each service handles one domain (MovieService, BookingService) |
| **Open/Closed** | Services use interfaces; new implementations can be added |
| **Liskov Substitution** | DTOs can be extended without breaking contracts |
| **Interface Segregation** | Separate interfaces for read/write operations |
| **Dependency Inversion** | Services depend on repository interfaces |

### 5.2 Design Patterns Used

1. **Repository Pattern**: Data access abstraction via Spring Data JPA
2. **DTO Pattern**: Separate objects for API contracts
3. **Builder Pattern**: Used for entity/DTO construction (via Lombok)
4. **Template Method**: Transaction management via annotations
5. **Strategy Pattern**: Price calculation by seat category

### 5.3 Code Quality Practices

- **Validation**: Input validation using Jakarta Bean Validation
- **Exception Handling**: Global exception handler with consistent responses
- **Logging**: Structured logging with SLF4J
- **Documentation**: JavaDoc comments on public methods
- **Immutability**: DTOs are effectively immutable

---

## 6. Non-Functional Requirements

### 6.1 Performance

| Requirement | Implementation |
|-------------|----------------|
| **Response Time** | < 200ms for read operations |
| **Throughput** | Designed for 1000+ concurrent users |
| **Caching** | In-memory cache for static data |
| **Database Indexing** | Indexes on frequently queried columns |
| **Pagination** | All list endpoints support pagination |

**Database Indexes Created**:
```sql
idx_movie_title, idx_movie_genre, idx_movie_release_date
idx_theater_city, idx_theater_name
idx_show_date, idx_show_movie, idx_show_screen
idx_show_seat_status, idx_show_seat_show
idx_booking_number, idx_booking_user, idx_booking_status
```

### 6.2 Scalability

```
┌─────────────────────────────────────────────────────────────────┐
│                      LOAD BALANCER (Nginx/ALB)                  │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
   ┌─────────┐           ┌─────────┐           ┌─────────┐
   │  App    │           │  App    │           │  App    │
   │Instance1│           │Instance2│           │Instance3│
   └────┬────┘           └────┬────┘           └────┬────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
   ┌─────────┐           ┌─────────┐           ┌─────────┐
   │  Redis  │           │  MySQL  │           │  Redis  │
   │ (Cache) │           │ (Master)│           │(Session)│
   └─────────┘           └────┬────┘           └─────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
                    ▼                   ▼
               ┌─────────┐         ┌─────────┐
               │  MySQL  │         │  MySQL  │
               │ (Read1) │         │ (Read2) │
               └─────────┘         └─────────┘
```

**Strategies**:
- Horizontal scaling with stateless application
- Read replicas for database scaling
- Distributed caching with Redis
- Session externalization

### 6.3 Reliability & Availability

| Aspect | Strategy |
|--------|----------|
| **High Availability** | Multiple application instances behind LB |
| **Data Consistency** | ACID transactions with optimistic/pessimistic locking |
| **Fault Tolerance** | Circuit breaker pattern for external services |
| **Data Durability** | Database replication and backups |
| **Graceful Degradation** | Fallbacks for non-critical features |

### 6.4 Security

| Threat | Mitigation |
|--------|------------|
| **SQL Injection** | Parameterized queries via JPA |
| **Input Validation** | Bean validation on all DTOs |
| **Authentication** | JWT-based auth (to be implemented) |
| **Authorization** | Role-based access control |
| **Data Protection** | HTTPS, encrypted passwords |
| **Rate Limiting** | API rate limiting (to be configured) |

### 6.5 Monitoring & Observability

**Recommended Stack**:
- **Metrics**: Micrometer + Prometheus
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Tracing**: Spring Cloud Sleuth + Zipkin
- **Alerting**: Prometheus AlertManager + PagerDuty

**Key Metrics to Track**:
- Booking success/failure rate
- Seat availability in real-time
- API response times (p50, p95, p99)
- Database connection pool utilization
- Cache hit/miss ratio

---

## 7. Platform Provisioning

### 7.1 Development Environment

```yaml
# docker-compose.yml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_DATASOURCE_URL=jdbc:h2:mem:moviedb
    
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: moviedb
    ports:
      - "3306:3306"
```

### 7.2 Production Deployment (AWS)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                AWS CLOUD                                     │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │                              VPC                                         ││
│  │  ┌─────────────────────────────────────────────────────────────────┐    ││
│  │  │                    PUBLIC SUBNET                                 │    ││
│  │  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐          │    ││
│  │  │  │   Route53   │────│    ALB      │────│  CloudFront │          │    ││
│  │  │  └─────────────┘    └──────┬──────┘    └─────────────┘          │    ││
│  │  └────────────────────────────┼────────────────────────────────────┘    ││
│  │                               │                                          ││
│  │  ┌─────────────────────────────────────────────────────────────────┐    ││
│  │  │                    PRIVATE SUBNET                                │    ││
│  │  │  ┌───────────────────────────────────────────────────────────┐  │    ││
│  │  │  │                    ECS FARGATE                            │  │    ││
│  │  │  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐      │  │    ││
│  │  │  │  │Container│  │Container│  │Container│  │Container│      │  │    ││
│  │  │  │  │   1     │  │   2     │  │   3     │  │   4     │      │  │    ││
│  │  │  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘      │  │    ││
│  │  │  └───────────────────────────────────────────────────────────┘  │    ││
│  │  │                               │                                  │    ││
│  │  │  ┌─────────────┐    ┌────────┴────────┐    ┌─────────────┐     │    ││
│  │  │  │ ElastiCache │    │   Aurora MySQL  │    │   Secrets   │     │    ││
│  │  │  │   (Redis)   │    │  (Multi-AZ)     │    │   Manager   │     │    ││
│  │  │  └─────────────┘    └─────────────────┘    └─────────────┘     │    ││
│  │  └─────────────────────────────────────────────────────────────────┘    ││
│  └─────────────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────┘
```

### 7.3 Infrastructure as Code (Terraform)

```hcl
# main.tf (simplified)
module "vpc" {
  source = "terraform-aws-modules/vpc/aws"
  name   = "movie-booking-vpc"
  cidr   = "10.0.0.0/16"
}

module "ecs" {
  source       = "terraform-aws-modules/ecs/aws"
  cluster_name = "movie-booking-cluster"
}

module "rds" {
  source     = "terraform-aws-modules/rds-aurora/aws"
  name       = "movie-booking-db"
  engine     = "aurora-mysql"
  instances  = 2
}

module "elasticache" {
  source         = "terraform-aws-modules/elasticache/aws"
  cluster_id     = "movie-booking-cache"
  engine         = "redis"
  node_type      = "cache.t3.medium"
}
```

### 7.4 CI/CD Pipeline

```yaml
# .github/workflows/main.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Build with Maven
        run: mvn clean package -DskipTests
      - name: Run Tests
        run: mvn test
      - name: Build Docker Image
        run: docker build -t movie-booking:${{ github.sha }} .
      - name: Push to ECR
        run: |
          aws ecr get-login-password | docker login --username AWS --password-stdin $ECR_REGISTRY
          docker push $ECR_REGISTRY/movie-booking:${{ github.sha }}
      - name: Deploy to ECS
        run: |
          aws ecs update-service --cluster movie-booking --service app --force-new-deployment
```

---

## 8. API Documentation

### 8.1 Movie APIs (READ Scenario)

#### Search Movies
```http
GET /api/v1/movies/search?title=Dark&city=Bangalore&date=2026-03-29

Response:
{
  "success": true,
  "message": "Movies retrieved successfully",
  "data": [
    {
      "id": 1,
      "title": "The Dark Knight",
      "genre": "Action",
      "language": "English",
      "durationMinutes": 152
    }
  ],
  "timestamp": 1711699200000
}
```

#### Get Movie with Shows
```http
GET /api/v1/movies/1/shows?city=Bangalore&date=2026-03-29

Response:
{
  "success": true,
  "data": {
    "movie": { "id": 1, "title": "The Dark Knight", ... },
    "theaterShows": [
      {
        "theater": { "id": 1, "name": "PVR Cinemas", "address": "MG Road" },
        "shows": [
          { "id": 1, "startTime": "10:00", "availableSeats": 95, "basePrice": 250.00 },
          { "id": 2, "startTime": "14:00", "availableSeats": 100, "basePrice": 300.00 }
        ]
      }
    ]
  }
}
```

### 8.2 Booking APIs (WRITE Scenario)

#### Create Booking
```http
POST /api/v1/bookings
Content-Type: application/json

{
  "userId": 1,
  "showId": 1,
  "seatIds": [1, 2, 3],
  "couponCode": "FLAT100"
}

Response:
{
  "success": true,
  "message": "Booking created successfully. Please complete payment within 10 minutes.",
  "data": {
    "bookingId": 1,
    "bookingNumber": "BK-20260329-A1B2C",
    "status": "PENDING",
    "movieTitle": "The Dark Knight",
    "theaterName": "PVR Cinemas",
    "showDate": "2026-03-29",
    "showTime": "10:00",
    "seatNumbers": ["A-1", "A-2", "A-3"],
    "totalAmount": 750.00,
    "discountAmount": 100.00,
    "finalAmount": 650.00
  }
}
```

#### Confirm Booking
```http
PUT /api/v1/bookings/1/confirm?transactionId=TXN123456789

Response:
{
  "success": true,
  "message": "Booking confirmed successfully!",
  "data": {
    "bookingId": 1,
    "bookingNumber": "BK-20260329-A1B2C",
    "status": "CONFIRMED",
    ...
  }
}
```

---

## 9. Areas Not Covered

The following areas were not implemented in this version but are noted for future development:

| Area | Reason | Notes |
|------|--------|-------|
| **Authentication/Authorization** | Time constraints | Recommended: Spring Security + JWT |
| **Payment Gateway Integration** | External dependency | Mock payment flow implemented |
| **Email/SMS Notifications** | External dependency | Recommended: AWS SES/SNS |
| **Seat Selection UI** | UI not required | API supports seat selection |
| **Admin Dashboard** | Out of scope | CRUD APIs can be added |
| **Analytics & Reporting** | Out of scope | Recommended: Apache Superset |
| **Microservices Split** | Monolith sufficient for MVP | Can be split later |
| **Event Sourcing** | Complexity | Consider for audit requirements |
| **Full-text Search** | Not implemented | Recommended: Elasticsearch |
| **Multi-tenancy** | Not required | Can be added if needed |

---

## 10. How to Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Steps

1. **Clone and Build**
```bash
cd D:\SpringBoot\TicketBookingPlatForm
mvn clean install
```

2. **Run Application**
```bash
mvn spring-boot:run
```

3. **Access APIs**
- Base URL: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console

4. **Sample API Calls**
```bash
# Get all movies
curl http://localhost:8080/api/v1/movies/now-showing

# Search movies
curl "http://localhost:8080/api/v1/movies/search?city=Bangalore"

# Get movie shows
curl "http://localhost:8080/api/v1/movies/1/shows?city=Bangalore&date=2026-03-29"

# Get seat availability
curl http://localhost:8080/api/v1/shows/1/seats

# Create booking
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"showId":1,"seatIds":[1,2,3]}'
```

---

## Author
Solution prepared for Online Movie Ticket Booking Platform Case Study

**Date**: March 29, 2026

