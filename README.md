# AggregateHub - Multi-Database User Aggregation Service

This Spring Boot application provides a service for aggregating user data from multiple databases into a single REST endpoint. It's designed to be flexible with an infinite number of database connections, supporting both PostgreSQL and MySQL databases.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Adding New Databases](#adding-new-databases)
- [API Documentation](#api-documentation)
- [Running the Application](#running-the-application)
- [Docker Compose](#docker-compose)
- [Contributing](#contributing)

## Prerequisites
- Java 21 or later
- Docker (for Docker Compose setup)
- Maven (for building the project)

## Getting Started
Clone this repository to your local machine:

```bash
git clone https://your-git-url.com/AggregateHub.git UPDATE
cd AggregateHub
```

## Project Structure
- `src/main/java/com/zemnitskiy/aggregatehub/` - Contains the main Java source code.
   - `controller/` - REST controllers for handling HTTP requests.
   - `dao/` - Data Access Objects for database operations.
   - `model/` - Entity classes representing the user model.
   - `naming/` - Custom naming strategies for database mappings.
   - `repository/` - Database strategy implementations for different database types.
   - `service/` - Business logic services.
   - `config/` - Configuration classes for multi-database setup.

## Adding New Databases
To add support for a new database or additional connections, follow these steps:

1. **Update `app.properties` or create a new configuration file:**

   ```properties
   aggregate-hub.data-sources[4].name=new-db
   aggregate-hub.data-sources[4].strategy=oracle # or another type if implemented
   aggregate-hub.data-sources[4].url=jdbc:oracle:thin:@localhost:1521:xe
   aggregate-hub.data-sources[4].user=newuser
   aggregate-hub.data-sources[4].password=newpass
   aggregate-hub.data-sources[4].table=oracle_users
   aggregate-hub.data-sources[4].mapping.id=oracle_id
   aggregate-hub.data-sources[4].mapping.username=oracle_username
   aggregate-hub.data-sources[4].mapping.name=oracle_name
   aggregate-hub.data-sources[4].mapping.surname=oracle_surname
   ```

2. **Implement a new `DatabaseStrategy` for the new database type if not already supported:**

   ```java
   @Component("oracle")
   public class OracleStrategy implements DatabaseStrategy {
       // Implement methods for Oracle-specific configurations
   }
   ```

3. **Ensure the new database driver is included in your project's dependencies.**

## API Documentation
The REST API is documented using OpenAPI (Swagger). After starting the application, you can access the API documentation at:

```
http://localhost:8080/swagger-ui.html
```

### Endpoint:
- **GET /users** - Fetches users from all databases. Optional parameters for filtering:
   - `id`
   - `username`
   - `name`
   - `surname`

## Running the Application

### Locally with Maven:
```bash
mvn spring-boot:run
```

### Docker Compose
To run the application along with its database dependencies using Docker:

```bash
docker-compose up --build
```

This command will start all services defined in `docker-compose.yml`, including the Spring Boot application and the databases.

**Note:** The Docker Compose configuration includes PostgreSQL and MySQL databases. Make sure to adjust the ports or database settings if they conflict with local services.

## Contributing
- Fork the repository
- Create your feature branch (`git checkout -b feature/YourFeature`)
- Commit your changes (`git commit -m 'Add some feature'`)
- Push to the branch (`git push origin feature/YourFeature`)
- Create a new Pull Request

---
Feel free to reach out with any questions or contributions!