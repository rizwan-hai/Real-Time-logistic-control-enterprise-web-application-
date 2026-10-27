# Logistics Control Tower

This project is a Spring Boot 3 + Java 21 backend for a real-time logistics control tower and delivery exception management system.

## Features implemented

- API prefix: `/api/v1`
- Authentication with JWT, password encoding, account lifecycle controls, and role-aware access
- Role model: `ADMIN`, `LOGISTICS_MANAGER`, `DISPATCHER`, `DRIVER`, `SECURITY_AUDITOR`, `VIEWER`
- Shipment CRUD, assignment workflow, status transitions, filtering, pagination, and CSV export
- Delivery exception lifecycle and overdue escalation scheduler
- Customer, driver, and vehicle management
- Dashboard summarization and audit logs
- Notifications and authenticated STOMP websocket topic at `/ws`
- Flyway MySQL schema initialization

## Project structure

- `src/main/java/com/logistics/controltower/...` — application code organized by domain
- `src/main/resources/application.yml` — runtime configuration
- `src/main/resources/db/migration/V1__initial_schema.sql` — database schema
- `src/test/java/...` — tests

## Required environment

Windows setup (PowerShell):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Copy-Item .env.example .env
# Edit .env before startup
```

## Start the app

```powershell
cd backend
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:SPRING_PROFILES_ACTIVE = 'dev'
mvn spring-boot:run
```

## Swagger

After startup, visit:

- http://localhost:8080/swagger-ui.html
- http://localhost:8080/v3/api-docs

## Environment variables

See `.env.example` for full placeholders. Minimum required values:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS`

## Notes

- MySQL 8 must be running and reachable.
- A production-grade deployment would still need stronger operational controls, including centralized secrets management and tamper-evident log retention.
