# ⚙️ NextCoder Backend — Spring Boot & Distributed Judge Service

> **Primary Documentation**: For full architecture diagrams, ER models, system design, and complete setup instructions, please refer to the main repository [README.md](../README.md).

## Quick Start (Backend)

### Prerequisites
* Java JDK 21
* Maven 3.9+
* Docker (for sandbox judge execution)
* MySQL 8.0+
* Redis 7.0+
* RabbitMQ 3.12+

### Running Locally

1. Configure environment variables in `.env` or application properties.
2. Build the project:
   ```bash
   ./mvnw clean package -DskipTests
   ```
3. Run the application:
   ```bash
   java -jar target/dsa-0.0.1-SNAPSHOT.jar
   ```

The backend API server will run on port `8083`.
