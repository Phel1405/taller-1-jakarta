# Jakarta EE Distributed Application

This project contains a distributed Jakarta EE application composed of two
independently deployable services:

- **Logic Service** — Contains the application and business logic.
- **Data Service** — Handles data access and communication with the database.
- **gRPC** — Used for communication between the Logic Service and Data Service.
- **Docker Compose** — Used to run both services together in separate
  containers.

Each service runs in its own JVM and WildFly instance.

## Project Structure

```text
.
├── pom.xml
├── docker-compose.yml
│
├── logic-service/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│
└── data-service/
    ├── pom.xml
    ├── Dockerfile
    └── src/
```

## Requirements

The following software is required:

- [Java SE 21](https://adoptium.net)
- [Docker](https://docs.docker.com/get-docker/)
- Docker Compose

The Maven Wrapper is included with each service, so a separate Maven
installation is not required.

On Linux/macOS, you may need to make the Maven Wrapper executable:

```bash
chmod +x logic-service/mvnw
chmod +x data-service/mvnw
```

## Building the Services

The Dockerfiles expect the WAR files to already exist in each service's
`target/` directory. Therefore, the services must be built with Maven before
building the Docker images.

### 1. Build the Logic Service

From the project root:

```bash
cd logic-service
./mvnw clean package
cd ..
```

This generates:

```text
logic-service/target/jakartaee-logic-tier.war
```

### 2. Build the Data Service

From the project root:

```bash
cd data-service
./mvnw clean package
cd ..
```

This generates:

```text
data-service/target/jakartaee-data-tier.war
```

## Building the Docker Images

Once both WAR files have been generated, build the Docker images from the
project root:

```bash
docker compose build
```

Docker uses the Dockerfile in each service directory:

```text
logic-service/Dockerfile
data-service/Dockerfile
```

The resulting architecture is:

```text
┌─────────────────────────┐
│     Logic Service       │
│                         │
│  WildFly + Java 21      │
│  logic-tier.war         │
└────────────┬────────────┘
             │
             │ gRPC
             ▼
┌─────────────────────────┐
│      Data Service       │
│                         │
│  WildFly + Java 21      │
│  data-tier.war          │
└────────────┬────────────┘
             │
             │ JDBC / JPA
             ▼
┌─────────────────────────┐
│        Database         │
└─────────────────────────┘
```

## Running the Application

After building both WAR files and Docker images, start the complete application
with:

```bash
docker compose up
```

To rebuild the images and start the application in one command:

```bash
docker compose up --build
```

To run the containers in the background:

```bash
docker compose up -d
```

To stop the application:

```bash
docker compose down
```

## Development Workflow

The recommended workflow is:

```text
1. Modify the code
       │
       ▼
2. Build Logic Service
   ./mvnw clean package
       │
       ▼
3. Build Data Service
   ./mvnw clean package
       │
       ▼
4. Build Docker images
   docker compose build
       │
       ▼
5. Start the services
   docker compose up
```

From the project root, the complete sequence is:

```bash
cd logic-service
./mvnw clean package
cd ../data-service
./mvnw clean package
cd ..
docker compose up --build
```

## Running the Services Individually

For development and debugging, each service can also be started independently
using its Maven Wrapper.

For example, to start the Logic Service:

```bash
cd logic-service
./mvnw wildfly:dev
```

And to start the Data Service:

```bash
cd data-service
./mvnw wildfly:dev
```

However, when testing the complete distributed architecture, Docker Compose
should be used so that the services run in separate containers and communicate
through the Docker network.

## Stopping and Cleaning

Stop the containers with:

```bash
docker compose down
```

Maven's `target/` directories contain generated build artifacts and should not
be committed to Git. They can safely be removed and regenerated:

```bash
rm -rf logic-service/target
rm -rf data-service/target
```

Or simply use:

```bash
./mvnw clean
```

inside each service.

## Architecture

The application is intentionally separated into independently deployable tiers:

```text
         Client
           │
           │ HTTP
           ▼
┌─────────────────────┐
│    Logic Service    │
│                     │
│ Jakarta EE          │
│ WildFly / JVM       │
└──────────┬──────────┘
           │
           │ gRPC
           ▼
┌─────────────────────┐
│    Data Service     │
│                     │
│ Jakarta EE          │
│ WildFly / JVM       │
└──────────┬──────────┘
           │
           │ JPA / JDBC
           ▼
       Database
```

The Logic Service does not directly access the database. Communication with the
Data Service occurs through the gRPC interface, allowing the two services to run
in separate JVMs, containers, or physical machines.
