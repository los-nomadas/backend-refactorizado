# Nomadas API Contract

Contrato de la API REST de la agencia de viajes Nomadas. Esta es la
referencia que comparten backend y frontend. Si vas a modificar un
endpoint, request, response o nombre de campo JSON, actualízalo aquí
**antes** de tocar el código.

- **Base URL local**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **Autenticación**: JWT Bearer en el header `Authorization` salvo en
  los endpoints listados como públicos.

## Convenciones

- Tipos de fecha: `LocalDate` se serializa como `YYYY-MM-DD`,
  `LocalDateTime` como `YYYY-MM-DDTHH:mm:ss`.
- Importes (`BigDecimal`): se envían como número o string con dos
  decimales. El frontend coerce a `Number` antes de formatear.
- Identificadores numéricos: `Long` (entero positivo).
- Enums: cadenas en mayúsculas (`HALF_BOARD`, `FULL_BOARD`, `IMSERSO`, etc.).

## Formato de error

Todos los errores controlados siguen este envelope:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "The bus is full and cannot be booked",
  "path": "/api/bookings",
  "timestamp": "2026-05-22T10:30:00"
}
```

Códigos esperados:

| Código | Caso                                                                  |
|--------|-----------------------------------------------------------------------|
| 400    | Validación de DTO (campos obligatorios, formato).                     |
| 401    | Falta o caduca el JWT en una ruta protegida.                          |
| 404    | Recurso no encontrado por id.                                         |
| 409    | Violación de regla de negocio (capacidad, menor sin adulto, etc.).    |
| 500    | Error inesperado del servidor (loggeado, no expone trazas internas).  |

---

## Auth

### `POST /api/auth/login` *(público)*

```json
{
  "username": "admin",
  "password": "admin12345"
}
```

`200 OK`:

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

`401 Unauthorized` con envelope estándar si las credenciales no son válidas.

---

## Users — `/api/users`

| Método | Path        | Público | Body            | Respuesta             |
|--------|-------------|---------|-----------------|-----------------------|
| GET    | `/api/users`        | NO  | -                  | `UserResponse[]`      |
| GET    | `/api/users/{id}`   | NO  | -                  | `UserResponse`        |
| POST   | `/api/users`        | SÍ  | `UserCreateRequest`| `201` `UserResponse`  |
| PUT    | `/api/users/{id}`   | NO  | `UserUpdateRequest`| `UserResponse`        |
| DELETE | `/api/users/{id}`   | NO  | -                  | `204 No Content`      |

### `UserCreateRequest`

```json
{
  "firstName": "Carla",
  "lastName": "Vega",
  "dni": "12345678A",
  "email": "carla@example.com",
  "phone": "611222333",
  "birthDate": "1985-03-10"
}
```

Validaciones: `firstName`/`lastName` 1-100, `dni` patrón `^[0-9]{8}[A-Z]$`,
`email` con formato, `phone` `^[0-9]{9,}$`, `birthDate` pasada o presente.

### `UserResponse`

```json
{
  "id": 1,
  "firstName": "Carla",
  "lastName": "Vega",
  "dni": "12345678A",
  "email": "carla@example.com",
  "phone": "611222333",
  "birthDate": "1985-03-10",
  "createdAt": "2026-05-22T10:00:00",
  "updatedAt": "2026-05-22T10:00:00"
}
```

---

## Hotels — `/api/hotels`

| Método | Path             | Público | Body                 | Respuesta              |
|--------|------------------|---------|----------------------|------------------------|
| GET    | `/api/hotels`        | **SÍ** | -                    | `HotelResponse[]`      |
| GET    | `/api/hotels/{id}`   | **SÍ** | -                    | `HotelResponse`        |
| POST   | `/api/hotels`        | NO     | `HotelCreateRequest` | `201` `HotelResponse`  |
| PUT    | `/api/hotels/{id}`   | NO     | `HotelUpdateRequest` | `HotelResponse`        |
| DELETE | `/api/hotels/{id}`   | NO     | -                    | `204 No Content`       |

### `HotelCreateRequest`

```json
{
  "name": "Hotel Sol",
  "description": "Hotel céntrico",
  "location": "Madrid",
  "totalRooms": 40,
  "availableRooms": 40,
  "totalPlaces": 100,
  "availablePlaces": 100,
  "halfBoardPrice": 60.00,
  "fullBoardPrice": 90.00,
  "imageUrl": "https://example.com/hotel.png"
}
```

---

## Drivers — `/api/drivers`

| Método | Path                | Público | Body                  | Respuesta              |
|--------|---------------------|---------|-----------------------|------------------------|
| GET    | `/api/drivers`         | NO  | -                     | `DriverResponse[]`     |
| GET    | `/api/drivers/{id}`    | NO  | -                     | `DriverResponse`       |
| POST   | `/api/drivers`         | NO  | `DriverCreateRequest` | `201` `DriverResponse` |
| PUT    | `/api/drivers/{id}`    | NO  | `DriverUpdateRequest` | `DriverResponse`       |
| DELETE | `/api/drivers/{id}`    | NO  | -                     | `204 No Content`       |

### `DriverCreateRequest`

```json
{
  "firstName": "Marta",
  "lastName": "Ortega",
  "dni": "22222222B",
  "licenseNumber": "L-12345",
  "phone": "611222333",
  "email": "marta@example.com",
  "available": true
}
```

---

## Buses — `/api/buses`

| Método | Path             | Público | Body              | Respuesta            |
|--------|------------------|---------|-------------------|----------------------|
| GET    | `/api/buses`         | NO  | -                  | `BusResponse[]`      |
| GET    | `/api/buses/{id}`    | NO  | -                  | `BusResponse`        |
| POST   | `/api/buses`         | NO  | `BusCreateRequest` | `201` `BusResponse`  |
| PUT    | `/api/buses/{id}`    | NO  | `BusUpdateRequest` | `BusResponse`        |
| DELETE | `/api/buses/{id}`    | NO  | -                  | `204 No Content`     |

### `BusCreateRequest`

```json
{
  "plateNumber": "1234-ABC",
  "totalSeats": 55,
  "availableSeats": 55,
  "driverId": 3
}
```

Regla de negocio: un mismo `driverId` no puede estar asignado a dos
buses activos (`409 Conflict`).

### `BusResponse`

```json
{
  "id": 1,
  "plateNumber": "1234-ABC",
  "totalSeats": 55,
  "availableSeats": 55,
  "driverId": 3,
  "driverFullName": "Marta Ortega"
}
```

---

## Trips — `/api/trips`

| Método | Path                     | Público | Body                | Respuesta             |
|--------|--------------------------|---------|---------------------|-----------------------|
| GET    | `/api/trips`                 | **SÍ** | -                   | `TripResponse[]`      |
| GET    | `/api/trips/offers`          | **SÍ** | -                   | `TripResponse[]` (solo `isOffer=true`) |
| GET    | `/api/trips/{id}`            | **SÍ** | -                   | `TripResponse`        |
| POST   | `/api/trips`                 | NO     | `TripCreateRequest` | `201` `TripResponse`  |
| PUT    | `/api/trips/{id}`            | NO     | `TripUpdateRequest` | `TripResponse`        |
| DELETE | `/api/trips/{id}`            | NO     | -                   | `204 No Content`      |

### `TripCreateRequest`

```json
{
  "destination": "Lisboa",
  "description": "Escapada de fin de semana",
  "departureDate": "2026-06-10",
  "returnDate": "2026-06-13",
  "hotelId": 1,
  "busId": 1,
  "boardType": "FULL_BOARD",
  "priceAdult": 120.00,
  "priceChild": 60.00,
  "priceSenior": 90.00,
  "totalSeats": 30,
  "availableSeats": 30,
  "isOffer": true,
  "imageUrl": "https://example.com/trip.png"
}
```

Validaciones aplicadas por el `TripService`:

- `returnDate >= departureDate` (409 si se invierte).
- `availableSeats <= totalSeats` (409 si excede).
- `totalSeats <= bus.totalSeats` (409 si excede la capacidad del bus).
- `hotelId` y `busId` deben existir (404 en caso contrario).
- `status` se resuelve automáticamente en create:
  - `PAST` si `departureDate < hoy`.
  - `FULL` si `availableSeats == 0`.
  - `AVAILABLE` en otro caso.

### `TripResponse`

```json
{
  "id": 7,
  "destination": "Lisboa",
  "description": "Escapada de fin de semana",
  "departureDate": "2026-06-10",
  "returnDate": "2026-06-13",
  "hotelId": 1,
  "hotelName": "Hotel Sol",
  "busId": 1,
  "busPlateNumber": "1234-ABC",
  "boardType": "FULL_BOARD",
  "priceAdult": 120.00,
  "priceChild": 60.00,
  "priceSenior": 90.00,
  "totalSeats": 30,
  "availableSeats": 30,
  "isOffer": true,
  "status": "AVAILABLE",
  "imageUrl": "https://example.com/trip.png"
}
```

---

## Bookings — `/api/bookings`

| Método | Path                          | Público | Body                  | Respuesta                |
|--------|-------------------------------|---------|-----------------------|--------------------------|
| GET    | `/api/bookings`                   | NO  | -                     | `BookingResponse[]`      |
| GET    | `/api/bookings?userId={id}`       | NO  | -                     | `BookingResponse[]`      |
| GET    | `/api/bookings/{id}`              | NO  | -                     | `BookingResponse`        |
| POST   | `/api/bookings`                   | NO  | `BookingCreateRequest`| `201` `BookingResponse`  |
| DELETE | `/api/bookings/{id}`              | NO  | -                     | `204 No Content`         |

### `BookingCreateRequest`

```json
{
  "userId": 1,
  "tripId": 7,
  "boardType": "HALF_BOARD",
  "groupType": "NONE",
  "companions": [
    {
      "firstName": "Carla",
      "lastName": "Vega",
      "birthDate": "1985-03-10"
    },
    {
      "firstName": "Lucas",
      "lastName": "Vega",
      "birthDate": "2015-07-20"
    }
  ]
}
```

- `companions` no puede estar vacío y cada elemento es obligatorio.
- `groupType` admite `NONE`, `IMSERSO`, `SCHOOL`.
- `boardType` admite `HALF_BOARD`, `FULL_BOARD`.

### Reglas de negocio aplicadas (todas devuelven `409 Conflict`)

| Condición                                                     | Mensaje                                              |
|---------------------------------------------------------------|------------------------------------------------------|
| `trip.status == CANCELLED`                                     | `The trip has been cancelled`                        |
| `trip.departureDate` en el pasado                              | `Cannot book a trip that has already departed`       |
| `trip.availableSeats < companions.size`                        | `Not enough seats available on this trip`            |
| `bus.availableSeats == 0`                                      | `The bus is full and cannot be booked`               |
| `hotel.availablePlaces == 0`                                   | `The hotel is full and cannot be booked`             |
| Algún `companions[*]` es menor (<18) y no hay ningún adulto    | `A minor cannot travel without an adult`             |

Al crear la reserva:

1. Calcula `totalPrice` y `groupDiscount` con `BookingPricingPolicy`:
   - `CHILD < 18` → `priceChild`, `18 <= ADULT < 65` → `priceAdult`,
     `SENIOR >= 65` → `priceSenior`.
   - `IMSERSO` aplica 20% de descuento al total, `SCHOOL` 15%, `NONE` 0%.
2. Decrementa `trip.availableSeats` y `hotel.availablePlaces` por el
   número de acompañantes.
3. Recalcula `TripStatus.FULL` si las plazas llegan a 0.
4. Envía email de confirmación con `JavaMailSender` (best-effort: los
   errores SMTP se loguean pero no propagan).

Al borrar la reserva las plazas y el estado se restauran.

### `BookingResponse`

```json
{
  "id": 12,
  "userId": 1,
  "userFullName": "Carla Vega",
  "tripId": 7,
  "tripDestination": "Lisboa",
  "boardType": "HALF_BOARD",
  "groupType": "NONE",
  "totalPrice": 180.00,
  "groupDiscount": 0.00,
  "companions": [
    {
      "id": 21,
      "firstName": "Carla",
      "lastName": "Vega",
      "birthDate": "1985-03-10"
    }
  ],
  "createdAt": "2026-05-22T10:30:00"
}
```

---

## Dashboard — `/api/dashboard`

| Método | Path                                    | Público | Respuesta                            |
|--------|------------------------------------------|---------|--------------------------------------|
| GET    | `/api/dashboard/trips-by-year?year=YYYY`     | NO  | `TripsByYearResponse`                |
| GET    | `/api/dashboard/current-year-revenue`        | NO  | `CurrentYearRevenueResponse`         |
| GET    | `/api/dashboard/top-trips?year=YYYY`         | NO  | `TopTripResponse[]` (máx. 3)         |

```json
// TripsByYearResponse
{ "year": 2026, "totalTrips": 12 }

// CurrentYearRevenueResponse
{ "year": 2026, "totalRevenue": 12345.67 }

// TopTripResponse
{ "tripId": 7, "destination": "Lisboa", "revenue": 1800.00 }
```

`revenue` y `totalRevenue` agregan `Booking.totalPrice` agrupando por
`trip.departureDate` dentro del año solicitado.

---

## Cloudinary — `/api/images`

| Método | Path             | Público | Body              | Respuesta              |
|--------|------------------|---------|-------------------|------------------------|
| POST   | `/api/images`         | NO  | `multipart/form-data` (campo `file`) | `ImageUploadResponse` |

```json
// ImageUploadResponse
{
  "url": "https://res.cloudinary.com/.../image.png",
  "publicId": "nomadas/abc123"
}
```

---

## CORS

`SecurityConfig.corsConfigurationSource` acepta cualquier puerto local
(`http://localhost:*`, `http://127.0.0.1:*`) para los métodos
`GET, POST, PUT, DELETE, OPTIONS, PATCH` con cualquier header.

## Cambios de contrato

Cualquier cambio en este documento requiere:

1. Actualizar el DTO o controller correspondiente en el backend.
2. Actualizar `src/api/services.js` y/o las páginas afectadas en el
   frontend.
3. Reflejar el cambio en Swagger (es automático vía springdoc, pero
   verificar que aparece correctamente).
4. Actualizar o añadir tests (`ContractTest`, IT del controller o
   tests Vitest del frontend).
