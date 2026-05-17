# Nomadas Backend

Backend de la aplicación web para la agencia de viajes Nomadas
(proyecto final, bootcamp Factoría F5).

> Este repositorio se encuentra en **FASE 1: limpieza**. Toda la
> implementación del briefing oficial (entidades User, Hotel,
> Driver, Bus, Trip, Booking, dashboard de dirección, reservas con
> acompañantes, cálculo de tarifas por edad, email de confirmación)
> se reconstruye en **FASE 2** sobre esta base limpia.

## Stack

- Java
- Spring Boot
- Maven
- MySQL
- Swagger / OpenAPI
- Cloudinary
- JavaMailSender

## Comandos

```bash
./mvnw spring-boot:run
./mvnw clean test
```

En Windows:

```bash
./mvnw.cmd spring-boot:run
./mvnw.cmd clean test
```

## Variables de entorno

Copiar `.env.example` a `.env` y completar los valores que apliquen
al entorno local (DB, Cloudinary, mail, JWT, credencial de demo).

## Ramas

- `dev` — rama de integración (única en uso).
- `main` — bloqueada para entregas estables.
