# Booking Service

The Booking Service is a microservice responsible for managing EV charging station bookings in the ChargeHive system. It provides comprehensive booking management functionality including creation, updates, cancellations, and status tracking.

## Features

### Core Functionality
- **Create Bookings**: Book charging ports for specific time slots
- **Update Bookings**: Modify booking details (time, duration, status)
- **Cancel Bookings**: Cancel bookings before they start
- **Complete Bookings**: Mark bookings as completed after charging
- **Port Availability**: Check if ports are available for booking
- **Booking Statistics**: Get statistics for users and stations

### Business Rules
- Minimum booking duration: 30 minutes
- Maximum booking duration: 24 hours
- Cannot book ports that are already occupied
- Cannot cancel bookings that have already started
- Cannot update completed or cancelled bookings
- Automatic cost calculation based on duration and port power

### Pricing Model
- Base rate: $2.50 per hour
- Power-based rate: $0.10 per kW per hour
- Total cost = Base cost + (Port power × Power rate × Hours)

## API Endpoints

### Booking Management

#### Create Booking
```
POST /api/bookings
Content-Type: application/json

{
  "userId": 1,
  "stationId": 1,
  "portId": 1,
  "startTime": "2024-01-15T10:00:00",
  "endTime": "2024-01-15T12:00:00",
  "duration": 120
}
```

#### Get Booking by ID
```
GET /api/bookings/{bookingId}
```

#### Update Booking
```
PUT /api/bookings/{bookingId}
Content-Type: application/json

{
  "startTime": "2024-01-15T11:00:00",
  "endTime": "2024-01-15T13:00:00",
  "duration": 120,
  "status": "BOOKED"
}
```

#### Cancel Booking
```
PUT /api/bookings/{bookingId}/cancel
```

#### Complete Booking
```
PUT /api/bookings/{bookingId}/complete
```

### Query Operations

#### Get User Bookings
```
GET /api/bookings/user/{userId}
```

#### Get Station Bookings
```
GET /api/bookings/station/{stationId}
```

#### Get All Bookings (Admin)
```
GET /api/bookings
```

#### Get Bookings by Status
```
GET /api/bookings/status/{status}
```

#### Get Active Bookings
```
GET /api/bookings/active
```

#### Get Upcoming User Bookings
```
GET /api/bookings/user/{userId}/upcoming
```

#### Get Completed User Bookings
```
GET /api/bookings/user/{userId}/completed
```

### Date Range Queries

#### Get Bookings in Date Range
```
GET /api/bookings/date-range?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
```

#### Get User Bookings in Date Range
```
GET /api/bookings/user/{userId}/date-range?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
```

### Availability & Statistics

#### Check Port Availability
```
GET /api/bookings/port/{portId}/availability?startTime=2024-01-15T10:00:00&endTime=2024-01-15T12:00:00
```

#### Get User Booking Statistics
```
GET /api/bookings/user/{userId}/statistics
```

#### Get Station Booking Statistics
```
GET /api/bookings/station/{stationId}/statistics
```

#### Health Check
```
GET /api/bookings/health
```

## Data Models

### Booking Entity
```java
@Entity
public class Booking {
    private Long id;
    private Long userId;
    private Long stationId;
    private Long portId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration; // in minutes
    private Double totalCost;
    private Status status;
    private LocalDateTime createdAt;
}
```

### Status Enum
```java
public enum Status {
    BOOKED,
    CANCELLED,
    COMPLETED
}
```

### DTOs

#### BookingRequestDto
```java
public class BookingRequestDto {
    private Long userId;
    private Long stationId;
    private Long portId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
}
```

#### BookingResponseDto
```java
public class BookingResponseDto {
    private Long id;
    private Long userId;
    private Long stationId;
    private Long portId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Double totalCost;
    private Status status;
    private LocalDateTime createdAt;
    private String stationName;
    private String stationAddress;
    private String connectorType;
    private Double maxPowerKw;
}
```

## External Service Integration

The booking service integrates with other microservices:

### Station Service
- Fetches station and port information
- Validates station and port existence
- Gets port specifications for cost calculation

### Auth Service
- Validates user existence
- Fetches user information

## Configuration

### Application Properties
```properties
spring.application.name=booking-service
server.port=8083

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/chargehive_booking
spring.datasource.username=root
spring.datasource.password=root

# External Services
service.station.url=http://localhost:8082
service.auth.url=http://localhost:8081

# Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

## Database Schema

The service creates the following tables:
- `bookings`: Main booking table with all booking information

## Error Handling

The service includes comprehensive error handling:

### Custom Exceptions
- `BookingException`: For booking-related business rule violations
- `ResourceNotFoundException`: For missing resources

### Global Exception Handler
- Handles validation errors
- Provides consistent error responses
- Logs errors appropriately

## Business Logic

### Booking Validation
1. Start time cannot be in the past
2. End time must be after start time
3. Duration must match time range
4. Minimum duration: 30 minutes
5. Maximum duration: 24 hours
6. User and station must exist
7. Port must be available for the time slot

### Cost Calculation
```
Total Cost = (Base Rate × Hours) + (Port Power × Power Rate × Hours)
```

### Status Transitions
- `BOOKED` → `CANCELLED` (before start time)
- `BOOKED` → `COMPLETED` (after charging)
- `CANCELLED` and `COMPLETED` are final states

## Running the Service

### Prerequisites
- Java 17
- MySQL 8.0+
- Maven 3.6+
- Eureka Server running on port 8761

### Build and Run
```bash
cd booking-service
mvn clean install
mvn spring-boot:run
```

### Docker
```bash
docker build -t booking-service .
docker run -p 8083:8083 booking-service
```

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn test -Dtest=*IntegrationTest
```

### API Testing
Use the provided endpoints with tools like Postman or curl.

## Monitoring and Logging

- Application logs are configured for DEBUG level
- SQL queries are logged for debugging
- Health check endpoint available
- Metrics can be added with Spring Boot Actuator

## Security Considerations

- Input validation on all endpoints
- Business rule enforcement
- Proper error handling without exposing sensitive information
- CORS configuration for frontend integration

## Future Enhancements

- Payment integration
- Notification service integration
- Advanced scheduling algorithms
- Real-time availability updates
- Booking analytics and reporting
