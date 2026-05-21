# Los Nómadas Backend

# 🇪🇸 Español

<div align="center">

![Java](https://img.shields.io/badge/Java-25-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen?logo=springboot)
![Maven](https://img.shields.io/badge/Maven-wrapper-red?logo=apachemaven)
![MySQL](https://img.shields.io/badge/MySQL-8.4-blue?logo=mysql)
![Flyway](https://img.shields.io/badge/Flyway-migraciones-red?logo=flyway)
![JWT](https://img.shields.io/badge/JWT-jjwt%200.12.3-orange)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203-green?logo=swagger)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![License](https://img.shields.io/badge/Licencia-MIT-lightgrey)

</div>

API REST de la **agencia de viajes Nómadas**, proyecto final del bootcamp Factoría F5. Gestiona usuarios, hoteles, autobuses, conductores, viajes y reservas, con autenticación JWT, notificaciones por email, subida de imágenes a Cloudinary y documentación OpenAPI integrada.

---

## Tabla de contenidos

- [Tecnologías](#tecnologías)
- [Requisitos previos](#requisitos-previos)
- [Instalación](#instalación)
- [Variables de entorno](#variables-de-entorno)
- [Base de datos con Docker](#base-de-datos-con-docker)
- [Ejecutar la aplicación](#ejecutar-la-aplicación)
- [Swagger UI](#swagger-ui)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Endpoints principales](#endpoints-principales)
- [Autenticación JWT](#autenticación-jwt)
- [Datos de demo](#datos-de-demo)
- [Tests](#tests)
- [Contrato API](#contrato-api)

---

## Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Java (Microsoft OpenJDK) | 25 | Lenguaje principal |
| Spring Boot | 4.0.6 | Framework web |
| Spring Security | (con Boot) | Seguridad y autenticación |
| Spring Data JPA | (con Boot) | Persistencia |
| Spring Mail | (con Boot) | Notificaciones por email |
| Maven Wrapper | — | Build y dependencias |
| MySQL | 8.4 | Base de datos (dev/prod) |
| H2 | — | Base de datos en memoria (tests) |
| Flyway | (con Boot) | Migraciones de esquema |
| jjwt | 0.12.3 | Generación y validación de JWT |
| SpringDoc OpenAPI | 3.0.2 | Documentación Swagger UI |
| ZXing | 3.5.3 | Generación de códigos QR |
| Cloudinary | — | Subida y almacenamiento de imágenes |
| Lombok | — | Reducción de boilerplate |
| Docker Compose | — | Entorno de base de datos local |

---

## Requisitos previos

- **Java 25** (Microsoft OpenJDK recomendado)
- **Maven** (o usar el wrapper incluido `./mvnw` — no requiere instalación)
- **Docker** y **Docker Compose** (para levantar MySQL localmente)

### Instalación de Java 25 en Windows

```powershell
winget install --id Microsoft.OpenJDK.25 --silent --accept-package-agreements --accept-source-agreements
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-25.0.3.9-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

### Verificar instalación

```bash
java -version
./mvnw -version
docker compose version
```

---

## Instalación

```bash
# 1. Clonar el repositorio
git clone <url-del-repositorio>
cd backend-refactorizado-dev

# 2. Copiar el archivo de variables de entorno
cp .env.example .env

# 3. Editar .env con tus valores (ver sección siguiente)
```

---

## Variables de entorno

Copia `.env.example` a `.env` y rellena los valores necesarios.

```env
# Perfil activo de Spring
SPRING_PROFILES_ACTIVE=dev

# Puerto del servidor
SERVER_PORT=8080

# Base de datos MySQL
DB_URL=jdbc:mysql://localhost:3306/nomadas_travel?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Madrid
DB_USERNAME=nomadas_user
DB_PASSWORD=nomadas_password

# Cloudinary (opcional — si no se configura, la subida de imágenes queda deshabilitada)
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=

# Email SMTP (opcional — si no se configura, los emails se degradan a no-op)
MAIL_HOST=
MAIL_PORT=587
MAIL_USER=
MAIL_PASS=

# JWT
JWT_SECRET=replace-with-a-long-random-secret-for-nomadas-travel-agency
JWT_EXPIRATION=86400000

# Datos de demo (primer arranque)
APP_DEMO_DATA_ENABLED=true
APP_DEMO_ADMIN_USERNAME=admin
APP_DEMO_ADMIN_EMAIL=admin@nomadas.local
APP_DEMO_ADMIN_PASSWORD=admin12345
```

> **Mínimo para arrancar en desarrollo:** `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` y las variables de demo. Cloudinary y Mail son opcionales.

---

## Base de datos con Docker

El repositorio incluye un `docker-compose.yml` que levanta MySQL 8.4 con las credenciales del `.env`:

```bash
# Levantar MySQL en segundo plano
docker compose up -d

# Verificar que el contenedor está listo
docker compose ps

# Detener el contenedor
docker compose down
```

El contenedor `nomadas-mysql` crea automáticamente la base de datos `nomadas_travel`, incluye healthcheck y un volumen persistente `mysql_data`.

---

## Ejecutar la aplicación

```bash
# Compilar el proyecto
./mvnw clean compile

# Arrancar contra MySQL local
./mvnw spring-boot:run
```

En **Windows** usa `mvnw.cmd` en lugar de `./mvnw`:

```cmd
mvnw.cmd spring-boot:run
```

La aplicación arrancará en `http://localhost:8080`.

Al primer arranque con `APP_DEMO_DATA_ENABLED=true`, Flyway ejecutará las migraciones y `DemoDataConfig` sembrará los datos iniciales.

---

## Swagger UI

Una vez levantada la aplicación, la documentación interactiva está disponible en:

| URL | Descripción |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Interfaz Swagger UI (pública) |
| `http://localhost:8080/v3/api-docs` | Especificación OpenAPI en JSON |

Desde Swagger UI puedes explorar todos los endpoints, ver los esquemas de request/response y probar las llamadas autenticadas con JWT.

---

## Estructura del proyecto

```
src/main/java/com/nomadas/
├── NomadasBackendApplication.java   ← Punto de entrada
│
├── auth/                            ← Autenticación interna
│   ├── controller/AuthController.java
│   ├── model/InternalRole.java
│   ├── repository/InternalCredentialRepository.java
│   └── service/InternalAuthServiceImpl.java
│
├── user/                            ← Gestión de usuarios
├── hotel/                           ← Gestión de hoteles
├── bus/                             ← Gestión de autobuses
├── driver/                          ← Gestión de conductores
│
├── trip/                            ← Gestión de viajes
│   └── model/ (BoardType, TripStatus)
│
├── booking/                         ← Reservas
│   ├── service/pricing/             ← Lógica de precios (AgeBracket, BookingPricingPolicy)
│   └── service/notification/        ← Notificaciones email
│
├── dashboard/                       ← Estadísticas y métricas
├── cloudinary/                      ← Subida de imágenes
│
├── security/                        ← Seguridad JWT
│   ├── JwtProvider.java
│   └── filter/JwtAuthenticationFilter.java
│
├── config/                          ← Configuración global
│   ├── SecurityConfig.java
│   ├── MailConfig.java
│   ├── OpenApiConfig.java
│   └── DemoDataConfig.java
│
├── exception/                       ← Manejo global de errores
│   └── GlobalExceptionHandler.java
│
└── validation/                      ← Validadores personalizados (@AllowedValues)
```

---

## Endpoints principales

La URL base de todos los endpoints es `http://localhost:8080/api`.

### Autenticación

| Método | Ruta | Público | Descripción |
|---|---|---|---|
| POST | `/auth/login` | ✅ Sí | Obtener token JWT |

### Usuarios

| Método | Ruta | Público | Descripción |
|---|---|---|---|
| GET | `/users` | 🔒 No | Listar todos los usuarios |
| GET | `/users/{id}` | 🔒 No | Obtener usuario por ID |
| POST | `/users` | ✅ Sí | Crear nuevo usuario |
| PUT | `/users/{id}` | 🔒 No | Actualizar usuario |
| DELETE | `/users/{id}` | 🔒 No | Eliminar usuario |

### Hoteles

| Método | Ruta | Público | Descripción |
|---|---|---|---|
| GET | `/hotels` | ✅ Sí | Listar todos los hoteles |
| GET | `/hotels/{id}` | ✅ Sí | Obtener hotel por ID |
| POST | `/hotels` | 🔒 No | Crear hotel |
| PUT | `/hotels/{id}` | 🔒 No | Actualizar hotel |
| DELETE | `/hotels/{id}` | 🔒 No | Eliminar hotel |

### Autobuses, Conductores, Viajes

Todos siguen el patrón CRUD completo (`GET`, `GET/{id}`, `POST`, `PUT/{id}`, `DELETE/{id}`) bajo `/buses`, `/drivers` y `/trips` respectivamente. Los `GET` de `/trips` son públicos.

### Reservas

| Método | Ruta | Público | Descripción |
|---|---|---|---|
| GET | `/bookings` | 🔒 No | Listar reservas (soporta `?userId={id}`) |
| GET | `/bookings/{id}` | 🔒 No | Obtener reserva por ID |
| POST | `/bookings` | 🔒 No | Crear reserva (aplica reglas de negocio + pricing) |
| DELETE | `/bookings/{id}` | 🔒 No | Cancelar reserva (restaura plazas) |

### Dashboard

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/dashboard/trips-by-year?year=YYYY` | Total de viajes en un año |
| GET | `/dashboard/current-year-revenue` | Ingresos del año en curso |
| GET | `/dashboard/top-trips?year=YYYY` | Top 3 viajes por ingresos |

### Imágenes

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/images` | Subir imagen a Cloudinary (`multipart/form-data`, campo `file`) |

---

## Autenticación JWT

### Obtener el token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin12345"}'
```

Respuesta:

```json
{
  "token": "<jwt>",
  "type": "Bearer",
  "credentialId": 1,
  "username": "admin",
  "email": "admin@nomadas.local",
  "role": "ADMIN",
  "expiresAt": "2026-05-23T10:30:00"
}
```

### Usar el token en peticiones protegidas

```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <jwt>"
```

El token caduca según `JWT_EXPIRATION` (valor en milisegundos, por defecto 86400000 = 24 horas). Al expirar, el servidor devuelve `401 Unauthorized`.

### Formato de errores

Todos los errores controlados siguen el mismo envelope:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token expired",
  "path": "/api/users",
  "timestamp": "2026-05-22T10:30:00"
}
```

---

## Datos de demo

Con `APP_DEMO_DATA_ENABLED=true`, al primer arranque `DemoDataConfig` siembra automáticamente un usuario administrador con las credenciales configuradas en las variables de entorno:

| Campo | Valor por defecto |
|---|---|
| Usuario | `admin` |
| Email | `admin@nomadas.local` |
| Contraseña | `admin12345` |

> Cambia estas credenciales en producción.

---

## Tests

```bash
# Tests unitarios y de servicio (surefire)
./mvnw test

# Tests + integración (failsafe, incluye FlywayBaselineIT)
./mvnw verify
```

Los tests de integración (`*IT.java`) utilizan H2 en memoria con el perfil `test` y se ejecutan por separado con `maven-failsafe-plugin`. Los tests unitarios (`*Test.java`) corren con `maven-surefire-plugin`.

---

## Contrato API

La documentación completa de todos los endpoints, DTOs, validaciones y reglas de negocio está en:

📄 [`docs/API_CONTRACT.md`](docs/API_CONTRACT.md)

O consulta directamente el **Swagger UI** en `http://localhost:8080/swagger-ui.html` tras arrancar la aplicación.

---

## Ramas

| Rama | Propósito |
|---|---|
| `dev` | Rama de integración (desarrollo activo) |
| `main` | Versiones estables |

---

---

# 🇬🇧 English

<div align="center">

![Java](https://img.shields.io/badge/Java-25-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen?logo=springboot)
![Maven](https://img.shields.io/badge/Maven-wrapper-red?logo=apachemaven)
![MySQL](https://img.shields.io/badge/MySQL-8.4-blue?logo=mysql)
![Flyway](https://img.shields.io/badge/Flyway-migrations-red?logo=flyway)
![JWT](https://img.shields.io/badge/JWT-jjwt%200.12.3-orange)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203-green?logo=swagger)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

</div>

REST API for the **Nómadas travel agency**, final project of the Factoría F5 bootcamp. It manages users, hotels, buses, drivers, trips and bookings, with JWT authentication, email notifications, image uploads to Cloudinary and integrated OpenAPI documentation.

---

## Table of contents

- [Tech stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Environment variables](#environment-variables)
- [Database with Docker](#database-with-docker)
- [Running the application](#running-the-application)
- [Swagger UI](#swagger-ui-1)
- [Project structure](#project-structure)
- [Main endpoints](#main-endpoints)
- [JWT authentication](#jwt-authentication)
- [Demo data](#demo-data)
- [Tests](#tests-1)
- [API contract](#api-contract)

---

## Tech stack

| Technology | Version | Purpose |
|---|---|---|
| Java (Microsoft OpenJDK) | 25 | Main language |
| Spring Boot | 4.0.6 | Web framework |
| Spring Security | (with Boot) | Security & authentication |
| Spring Data JPA | (with Boot) | Persistence |
| Spring Mail | (with Boot) | Email notifications |
| Maven Wrapper | — | Build & dependency management |
| MySQL | 8.4 | Database (dev/prod) |
| H2 | — | In-memory database (tests) |
| Flyway | (with Boot) | Schema migrations |
| jjwt | 0.12.3 | JWT generation and validation |
| SpringDoc OpenAPI | 3.0.2 | Swagger UI documentation |
| ZXing | 3.5.3 | QR code generation |
| Cloudinary | — | Image upload and storage |
| Lombok | — | Boilerplate reduction |
| Docker Compose | — | Local database environment |

---

## Prerequisites

- **Java 25** (Microsoft OpenJDK recommended)
- **Maven** (or use the included wrapper `./mvnw` — no installation required)
- **Docker** and **Docker Compose** (to run MySQL locally)

### Installing Java 25 on Windows

```powershell
winget install --id Microsoft.OpenJDK.25 --silent --accept-package-agreements --accept-source-agreements
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-25.0.3.9-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

### Verify installation

```bash
java -version
./mvnw -version
docker compose version
```

---

## Installation

```bash
# 1. Clone the repository
git clone <repository-url>
cd backend-refactorizado-dev

# 2. Copy the environment variables file
cp .env.example .env

# 3. Edit .env with your values (see next section)
```

---

## Environment variables

Copy `.env.example` to `.env` and fill in the required values.

```env
# Active Spring profile
SPRING_PROFILES_ACTIVE=dev

# Server port
SERVER_PORT=8080

# MySQL database
DB_URL=jdbc:mysql://localhost:3306/nomadas_travel?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Madrid
DB_USERNAME=nomadas_user
DB_PASSWORD=nomadas_password

# Cloudinary (optional — if not set, image uploads are disabled)
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=

# SMTP email (optional — if not set, emails degrade to no-op)
MAIL_HOST=
MAIL_PORT=587
MAIL_USER=
MAIL_PASS=

# JWT
JWT_SECRET=replace-with-a-long-random-secret-for-nomadas-travel-agency
JWT_EXPIRATION=86400000

# Demo data (first startup)
APP_DEMO_DATA_ENABLED=true
APP_DEMO_ADMIN_USERNAME=admin
APP_DEMO_ADMIN_EMAIL=admin@nomadas.local
APP_DEMO_ADMIN_PASSWORD=admin12345
```

> **Minimum required to start in development:** `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` and the demo variables. Cloudinary and Mail are optional.

---

## Database with Docker

The repository includes a `docker-compose.yml` that starts MySQL 8.4 with the credentials from `.env`:

```bash
# Start MySQL in the background
docker compose up -d

# Check the container is ready
docker compose ps

# Stop the container
docker compose down
```

The `nomadas-mysql` container automatically creates the `nomadas_travel` database, includes a healthcheck and a persistent `mysql_data` volume.

---

## Running the application

```bash
# Compile the project
./mvnw clean compile

# Start against local MySQL
./mvnw spring-boot:run
```

On **Windows** use `mvnw.cmd` instead of `./mvnw`:

```cmd
mvnw.cmd spring-boot:run
```

The application will start at `http://localhost:8080`.

On the first startup with `APP_DEMO_DATA_ENABLED=true`, Flyway will run the migrations and `DemoDataConfig` will seed the initial data.

---

## Swagger UI

Once the application is running, interactive documentation is available at:

| URL | Description |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Swagger UI interface (public) |
| `http://localhost:8080/v3/api-docs` | OpenAPI specification in JSON |

From Swagger UI you can explore all endpoints, view request/response schemas and test authenticated calls with JWT.

---

## Project structure

```
src/main/java/com/nomadas/
├── NomadasBackendApplication.java   ← Entry point
│
├── auth/                            ← Internal authentication
│   ├── controller/AuthController.java
│   ├── model/InternalRole.java
│   ├── repository/InternalCredentialRepository.java
│   └── service/InternalAuthServiceImpl.java
│
├── user/                            ← User management
├── hotel/                           ← Hotel management
├── bus/                             ← Bus management
├── driver/                          ← Driver management
│
├── trip/                            ← Trip management
│   └── model/ (BoardType, TripStatus)
│
├── booking/                         ← Bookings
│   ├── service/pricing/             ← Pricing logic (AgeBracket, BookingPricingPolicy)
│   └── service/notification/        ← Email notifications
│
├── dashboard/                       ← Statistics and metrics
├── cloudinary/                      ← Image uploads
│
├── security/                        ← JWT security
│   ├── JwtProvider.java
│   └── filter/JwtAuthenticationFilter.java
│
├── config/                          ← Global configuration
│   ├── SecurityConfig.java
│   ├── MailConfig.java
│   ├── OpenApiConfig.java
│   └── DemoDataConfig.java
│
├── exception/                       ← Global error handling
│   └── GlobalExceptionHandler.java
│
└── validation/                      ← Custom validators (@AllowedValues)
```

---

## Main endpoints

The base URL for all endpoints is `http://localhost:8080/api`.

### Authentication

| Method | Path | Public | Description |
|---|---|---|---|
| POST | `/auth/login` | ✅ Yes | Obtain JWT token |

### Users

| Method | Path | Public | Description |
|---|---|---|---|
| GET | `/users` | 🔒 No | List all users |
| GET | `/users/{id}` | 🔒 No | Get user by ID |
| POST | `/users` | ✅ Yes | Create new user |
| PUT | `/users/{id}` | 🔒 No | Update user |
| DELETE | `/users/{id}` | 🔒 No | Delete user |

### Hotels

| Method | Path | Public | Description |
|---|---|---|---|
| GET | `/hotels` | ✅ Yes | List all hotels |
| GET | `/hotels/{id}` | ✅ Yes | Get hotel by ID |
| POST | `/hotels` | 🔒 No | Create hotel |
| PUT | `/hotels/{id}` | 🔒 No | Update hotel |
| DELETE | `/hotels/{id}` | 🔒 No | Delete hotel |

### Buses, Drivers, Trips

All follow the full CRUD pattern (`GET`, `GET/{id}`, `POST`, `PUT/{id}`, `DELETE/{id}`) under `/buses`, `/drivers` and `/trips` respectively. `GET` endpoints for `/trips` are public.

### Bookings

| Method | Path | Public | Description |
|---|---|---|---|
| GET | `/bookings` | 🔒 No | List bookings (supports `?userId={id}`) |
| GET | `/bookings/{id}` | 🔒 No | Get booking by ID |
| POST | `/bookings` | 🔒 No | Create booking (applies business rules + pricing) |
| DELETE | `/bookings/{id}` | 🔒 No | Cancel booking (restores seats) |

### Dashboard

| Method | Path | Description |
|---|---|---|
| GET | `/dashboard/trips-by-year?year=YYYY` | Total trips in a given year |
| GET | `/dashboard/current-year-revenue` | Revenue for the current year |
| GET | `/dashboard/top-trips?year=YYYY` | Top 3 trips by revenue |

### Images

| Method | Path | Description |
|---|---|---|
| POST | `/images` | Upload image to Cloudinary (`multipart/form-data`, field `file`) |

---

## JWT authentication

### Obtaining the token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin12345"}'
```

Response:

```json
{
  "token": "<jwt>",
  "type": "Bearer",
  "credentialId": 1,
  "username": "admin",
  "email": "admin@nomadas.local",
  "role": "ADMIN",
  "expiresAt": "2026-05-23T10:30:00"
}
```

### Using the token in protected requests

```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <jwt>"
```

The token expires according to `JWT_EXPIRATION` (value in milliseconds, default 86400000 = 24 hours). When it expires, the server returns `401 Unauthorized`.

### Error format

All controlled errors follow the same envelope:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token expired",
  "path": "/api/users",
  "timestamp": "2026-05-22T10:30:00"
}
```

| Code | Cause |
|---|---|
| 400 | DTO validation error (required fields, format) |
| 401 | Missing or expired JWT on a protected route |
| 404 | Resource not found by ID |
| 409 | Business rule violation (capacity, minor without adult, etc.) |
| 500 | Unexpected server error (logged, no internal traces exposed) |

---

## Demo data

With `APP_DEMO_DATA_ENABLED=true`, on the first startup `DemoDataConfig` automatically seeds an administrator user with the credentials set in the environment variables:

| Field | Default value |
|---|---|
| Username | `admin` |
| Email | `admin@nomadas.local` |
| Password | `admin12345` |

> Change these credentials in production.

---

## Tests

```bash
# Unit and service tests (surefire)
./mvnw test

# Tests + integration tests (failsafe, includes FlywayBaselineIT)
./mvnw verify
```

Integration tests (`*IT.java`) use H2 in-memory with the `test` profile and are run separately with `maven-failsafe-plugin`. Unit tests (`*Test.java`) run with `maven-surefire-plugin`.

---

## API contract

Full documentation of all endpoints, DTOs, validations and business rules is available at:

📄 [`docs/API_CONTRACT.md`](docs/API_CONTRACT.md)

Or consult the **Swagger UI** directly at `http://localhost:8080/swagger-ui.html` after starting the application.

---

## Branches

| Branch | Purpose |
|---|---|
| `dev` | Integration branch (active development) |
| `main` | Stable releases |
