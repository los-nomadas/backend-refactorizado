# Nomadas Backend

API REST de la agencia de viajes Nomadas (proyecto final, bootcamp
Factoría F5).

## Stack

- Java 25 (Microsoft OpenJDK build)
- Spring Boot 4.0.6
- Maven
- MySQL (perfiles `dev` y `prod`) / H2 in-memory (perfiles `test` y `e2e`)
- Flyway (V1 baseline en `db/migration/{mysql,h2}`)
- Swagger / OpenAPI (springdoc)
- Cloudinary
- JavaMailSender (best-effort para email de confirmación)
- JWT con `JJWT`

## Estructura

```
src/main/java/com/nomadas
├── NomadasBackendApplication.java
├── auth/        JWT login (POST /api/auth/login)
├── user/        CRUD /api/users
├── hotel/       CRUD /api/hotels (GET público)
├── driver/      CRUD /api/drivers
├── bus/         CRUD /api/buses (driver unicidad)
├── trip/        CRUD /api/trips (GET público, /offers, validaciones)
├── booking/     POST /api/bookings con reglas de negocio + pricing + email
├── dashboard/   /api/dashboard/{trips-by-year,current-year-revenue,top-trips}
├── cloudinary/  /api/images (subida de imágenes)
├── config/      Security, CORS, OpenAPI, MailConfig, DemoData
├── exception/   GlobalExceptionHandler con envelope estándar
├── security/    JwtProvider + filter
└── validation/  Validador @AllowedValues
```

## Comandos

```bash
# Compilar
./mvnw clean compile

# Tests unitarios + de servicio (surefire)
./mvnw test

# Tests + integración (failsafe, incluye FlywayBaselineIT)
./mvnw verify

# Arrancar la app contra MySQL local del docker-compose
./mvnw spring-boot:run
```

En Windows también `./mvnw.cmd ...`.

### Java 25

El proyecto exige JDK 25. Si no lo tienes:

```powershell
winget install --id Microsoft.OpenJDK.25 --silent --accept-package-agreements --accept-source-agreements
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-25.0.3.9-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

## Variables de entorno

Copia `.env.example` a `.env`. Lo mínimo para arrancar en dev:

```
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:mysql://localhost:3306/nomadas_travel?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Madrid
DB_USERNAME=nomadas_user
DB_PASSWORD=nomadas_password
APP_DEMO_ADMIN_USERNAME=admin
APP_DEMO_ADMIN_EMAIL=admin@nomadas.local
APP_DEMO_ADMIN_PASSWORD=admin12345
JWT_SECRET=<un secreto largo y aleatorio>
```

Cloudinary y mail son opcionales (los servicios degradan a no-op si
las variables no están configuradas).

## Levantar MySQL local

```bash
docker compose up -d
```

Crea el contenedor `nomadas-mysql` con la base `nomadas_travel` y las
credenciales del `.env`.

## Contrato API

Ver [`docs/API_CONTRACT.md`](docs/API_CONTRACT.md) o el Swagger UI en
`http://localhost:8080/swagger-ui.html` (público) tras arrancar la app.

## Demo credentials

`DemoDataConfig` siembra un único usuario admin la primera vez que
arranca la base de datos: `admin / admin12345`.

## Ramas

- `dev` — integración (única en uso).
- `main` — versiones estables.
