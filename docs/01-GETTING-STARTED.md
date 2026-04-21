# Getting Started with LMS Backend

## Prerequisites
- Java 17+
- PostgreSQL
- Maven

## Local Setup
1. **Create the Database:**
   Ensure PostgreSQL is running and create a database named `lms-project`.
   ```sql
   CREATE DATABASE "lms-project";
   ```

2. **Configure Database Credentials:**
   The application uses environment variables for credentials. You can set them in your terminal or replace the defaults in `src/main/resources/application.yml`.
   ```yaml
   spring:
     datasource:
       username: postgres
       password: ${DB_PASSWORD:***REMOVED***}
   ```

3. **Install Dependencies and Run:**
   ```bash
   ./mvnw spring-boot:run
   ```

## Initial Data
The application automatically seeds:
- **Admin:** `admin@lms.com` (password: `admin123`)
- **Student:** `student@lms.com` (password: `student123`)
- A sample book ("Harry Potter") for initial testing.

## Useful Commands
- **Run tests:** `./mvnw test`
- **Clean and package:** `./mvnw clean package`
