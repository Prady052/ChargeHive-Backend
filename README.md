# ChargeHive Backend

A comprehensive microservices-based backend system for electric vehicle charging station management, built with Spring Boot and Spring Cloud.

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.0-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-yellow)
![Microservices](https://img.shields.io/badge/Architecture-Microservices-purple)
![JWT](https://img.shields.io/badge/Security-JWT-red)

</div>

## 🏗️ Architecture Overview

ChargeHive follows a **microservices architecture** pattern with the following components:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │    │ Discovery Server│
│   (Port 5173)   │◄──►│   (Port 8080)   │◄──►│   (Port 8761)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                ┌─────────────────────────────────────┐
                │                                     │
        ┌───────▼──────┐    ┌───────▼──────┐          │
        │Admin Service │    │Auth Service  │          │
        │(Port 8082)   │    │(Port 8083)   │          │
        └──────────────┘    └──────────────┘          │
                │                     │               │
        ┌───────▼────────┐   ┌─────────▼──────┐       │
        │Station Service │   │Booking Service │       │
        │(Port 8086)     │   │(Port 8085)     │       │
        └────────────────┘   └────────────────┘       │
                │                     │               │
                └─────────────────────┴───────────────┘
                                │
                                ▼
                        ┌─────────────────┐
                        │   MySQL Database│
                        │   (Port 3306)   │
                        └─────────────────┘
```

## 🚀 Services

### 1. **Discovery Server** (Port: 8761)

- **Purpose**: Service registry and discovery using Netflix Eureka
- **Technology**: Spring Cloud Netflix Eureka Server
- **Features**:
  - Service registration and discovery
  - Health monitoring
  - Load balancing support

### 2. **API Gateway** (Port: 8080)

- **Purpose**: Single entry point for all client requests
- **Technology**: Spring Cloud Gateway
- **Features**:
  - Route management
  - CORS configuration
  - JWT authentication
  - Load balancing
  - Request/Response transformation

### 3. **Auth Service** (Port: 8083)

- **Purpose**: User authentication and authorization
- **Technology**: Spring Boot + Spring Security + JWT
- **Features**:
  - User registration and login
  - JWT token generation and validation
  - Password encryption (BCrypt)
  - Role-based access control
  - User management

### 4. **Admin Service** (Port: 8082)

- **Purpose**: Administrative operations and monitoring
- **Technology**: Spring Boot + Spring Data JPA
- **Features**:
  - User management
  - Station approval workflow
  - Audit logging
  - System metrics
  - Role assignment

### 5. **Station Service** (Port: 8086)

- **Purpose**: Charging station management
- **Technology**: Spring Boot + Spring Data JPA + Hibernate Spatial
- **Features**:
  - Station CRUD operations
  - Port management
  - Geographic location support
  - Owner-based access control
  - Status management

### 6. **Booking Service** (Port: 8085)

- **Purpose**: Charging session booking management
- **Technology**: Spring Boot + Spring Data JPA
- **Features**:
  - Booking creation and management
  - Session tracking
  - Earnings calculation
  - Status management
  - Scheduling support

## 🛠️ Technology Stack

### Core Technologies

- **Java**: 21 (varies by service)
- **Spring Boot**: 3.5.4
- **Spring Cloud**: 2025.0.0
- **Spring Data JPA**: Data persistence
- **Spring Security**: Authentication & authorization
- **Spring Cloud Gateway**: API Gateway

### Database

- **MySQL**: Primary database
- **Hibernate**: ORM framework

### Security

- **JWT**: JSON Web Tokens for authentication
- **BCrypt**: Password hashing
- **Spring Security**: Security framework

### Additional Libraries

- **ModelMapper**: Object mapping
- **Lombok**: Code generation
- **SpringDoc OpenAPI**: API documentation
- **Spring Boot Actuator**: Monitoring and metrics

## 📋 Prerequisites

Before running the application, ensure you have:

- **Java**: JDK 17 or 21 (check individual service requirements)
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Git**: For cloning the repository

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Prady052/ChargeHive-Backend.git chargehive-backend
cd chargehive-backend
```

### 2. Database Setup

Create the required MySQL databases:

```sql
-- Create databases for each service
CREATE DATABASE adminDb;
CREATE DATABASE userDb;
CREATE DATABASE stationDb;
CREATE DATABASE bookingDb;

-- Create user (optional)
CREATE USER 'chargehive'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON *.* TO 'chargehive'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configuration

Each service has its own configuration file. Update the database credentials in:

- `admin-service/src/main/resources/application.properties`
- `auth-service/src/main/resources/application.properties`
- `station-service/src/main/resources/application.properties`
- `booking-service/src/main/resources/application.properties`

**Note**: Some services use `application-secret.properties` for sensitive data.

### 4. Build the Project

```bash
# Build all services
mvn clean install -DskipTests

# Or build individual services
cd admin-service && mvn clean install
cd ../auth-service && mvn clean install
cd ../station-service && mvn clean install
cd ../booking-service && mvn clean install
cd ../api-gateway && mvn clean install
cd ../discovery-server && mvn clean install
```

### 5. Start Services

Start services in the following order:

```bash
# 1. Start Discovery Server
cd discovery-server
mvn spring-boot:run

# 2. Start API Gateway (in new terminal)
cd api-gateway
mvn spring-boot:run

# 3. Start Auth Service (in new terminal)
cd auth-service
mvn spring-boot:run

# 4. Start Admin Service (in new terminal)
cd admin-service
mvn spring-boot:run

# 5. Start Station Service (in new terminal)
cd station-service
mvn spring-boot:run

# 6. Start Booking Service (in new terminal)
cd booking-service
mvn spring-boot:run
```

## 🌐 Service Endpoints

### Discovery Server

- **Eureka Dashboard**: http://localhost:8761

### API Gateway

- **Base URL**: http://localhost:8080
- **Admin Routes**: `/api/admin/**`
- **Auth Routes**: `/api/auth/**`
- **Station Routes**: `/api/stations/**`
- **Booking Routes**: `/api/bookings/**`

### Individual Services

- **Admin Service**: http://localhost:8082
- **Auth Service**: http://localhost:8083
- **Station Service**: http://localhost:8086
- **Booking Service**: http://localhost:8085

## 🔐 Authentication

The system uses JWT-based authentication:

1. **Register**: `POST /api/auth/register`
2. **Login**: `POST /api/auth/login`
3. **Include Token**: Add `Authorization: Bearer <token>` header
4. **User ID Header**: Include `X-User-Id: <userId>` for user-specific operations

## 📊 Key Features

### User Management

- User registration and authentication
- Role-based access control
- Profile management
- Password change functionality

### Station Management

- Charging station registration
- Port management (multiple charging ports per station)
- Geographic location support
- Status approval workflow
- Owner-based access control

### Booking System

- Session booking and management
- Real-time status tracking
- Earnings calculation
- Admin oversight and reporting

### Admin Dashboard

- User management
- Station approval workflow
- System metrics and monitoring
- Audit logging

## 📝 API Documentation

Each service includes Swagger/OpenAPI documentation:

- **Admin Service**: http://localhost:8082/swagger-ui.html
- **Auth Service**: http://localhost:8083/swagger-ui.html
- **Station Service**: http://localhost:8086/swagger-ui.html
- **Booking Service**: http://localhost:8085/swagger-ui.html

## 🔧 Configuration

### Environment Variables

Set the following environment variables or update `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/<database_name>
spring.datasource.username=<username>
spring.datasource.password=<password>

# JWT
jwt.expiration.time=86400000

# Service Discovery
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

### Port Configuration

Default ports can be changed in each service's `application.properties`:

- Discovery Server: 8761
- API Gateway: 8080
- Admin Service: 8082
- Auth Service: 8083
- Booking Service: 8085
- Station Service: 8086

## 🚨 Troubleshooting

### Common Issues

1. **Port Already in Use**

   ```bash
   # Find process using port
   netstat -ano | findstr :8080
   # Kill process
   taskkill /PID <process_id> /F
   ```

2. **Database Connection Issues**

   - Verify MySQL is running
   - Check database credentials
   - Ensure databases exist

3. **Service Discovery Issues**

   - Start Discovery Server first
   - Check Eureka dashboard at http://localhost:8761
   - Verify service registration

4. **JWT Issues**
   - Check token expiration
   - Verify token format in headers
   - Ensure proper user ID header

## 📈 Monitoring

### Health Checks

- **Actuator Endpoints**: Available on each service
- **Eureka Dashboard**: Service status and health
- **Custom Metrics**: Admin service provides business metrics

### Logging

Each service includes comprehensive logging:

- Request/Response logging
- Error tracking
- Performance monitoring

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.
