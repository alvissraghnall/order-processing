# Microservices Application

gRPC Microservices application built with Angular frontend and Java Spring Boot backend services.

## Architecture

This project consists of:
- **Frontend**: Angular web application
- **Inventory Service**: Java Spring Boot microservice for inventory management
- **Order Service**: Java Spring Boot microservice for order processing
- **Proto Common**: Shared protocol buffer definitions and common utilities

## Prerequisites

Before running the application, ensure you have the following installed:

- **Docker & Docker Compose**: For containerized deployment
- **Node.js** (v20+) and **pnpm**: For frontend development
- **Java 17+** and **Maven**: For backend service development
- **Git**: For version control

## Quick Start

### Using Docker Compose (Recommended)

The easiest way to run the entire application is using Docker Compose:

```bash
git clone https://github.com/alvissraghnall/order-processing order-processing-system
cd order-processing-system

# Start all services
docker-compose up -d

# View logs (optional)
docker-compose logs -f
```

This will start:
- Frontend application (port 4200)
- Inventory service (gRPC server running on port 8000)
- Order service (port 8080)

### Manual Setup

If you prefer to run services individually:

#### 1. Build Proto Common (First)

```bash
cd proto-common
./mvnw clean install
```

#### 2. Start Backend Services

**Inventory Service:**
```bash
cd inventory-service
./mvnw spring-boot:run
```

**Order Service:**
```bash
cd order-service
./mvnw spring-boot:run
```

#### 3. Start Frontend

```bash
cd frontend
pnpm install
pnpm start
```

## Development

### Frontend Development

```bash
cd frontend
pnpm install              # Install dependencies
pnpm start               # Start development server
pnpm build               # Build for production
pnpm test                # Run tests
```

### Backend Development

For each service (inventory-service, order-service):

```bash
./mvnw clean compile     # Compile
./mvnw spring-boot:run   # Run the service
./mvnw test              # Run tests
./mvnw clean package     # Build JAR
```

### Proto Common

When making changes to shared protocols:

```bash
cd proto-common
./mvnw clean install    # Build and install to local Maven repository
```

Then rebuild dependent services.

## Configuration

- **Docker Compose**: Edit `docker-compose.yml` for service configuration
- **Frontend**: Configuration files in `frontend/src/environments/`
- **Backend Services**: `application.yml` in each service's `src/main/resources/`

## Ports

Default port assignments (check `docker-compose.yml` for actual configuration):
- Frontend: `http://localhost:4200`
- Inventory Service: `static:/localhost:8000`
- Order Service: `http://localhost:8080`

## Stopping the Application

```bash
# Stop Docker Compose services
docker-compose down

# Stop with cleanup
docker-compose down -v --remove-orphans
```
