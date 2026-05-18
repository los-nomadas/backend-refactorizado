CREATE TABLE internal_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    last_login_at DATETIME(6)
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    dni VARCHAR(9) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    birth_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6)
);

CREATE TABLE hotels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    location VARCHAR(255) NOT NULL,
    total_rooms INT NOT NULL,
    available_rooms INT NOT NULL,
    total_places INT NOT NULL,
    available_places INT NOT NULL,
    half_board_price DECIMAL(10, 2) NOT NULL,
    full_board_price DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(1000) NOT NULL
);

CREATE TABLE drivers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    dni VARCHAR(9) NOT NULL UNIQUE,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(255) NOT NULL UNIQUE,
    available BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE buses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plate_number VARCHAR(20) NOT NULL UNIQUE,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    driver_id BIGINT NOT NULL,
    CONSTRAINT fk_buses_driver FOREIGN KEY (driver_id) REFERENCES drivers (id)
);

CREATE TABLE trips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    destination VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    departure_date DATE NOT NULL,
    return_date DATE NOT NULL,
    hotel_id BIGINT NOT NULL,
    bus_id BIGINT NOT NULL,
    board_type VARCHAR(20) NOT NULL,
    price_adult DECIMAL(10, 2) NOT NULL,
    price_child DECIMAL(10, 2) NOT NULL,
    price_senior DECIMAL(10, 2) NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    is_offer BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    CONSTRAINT fk_trips_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id),
    CONSTRAINT fk_trips_bus FOREIGN KEY (bus_id) REFERENCES buses (id)
);

CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    trip_id BIGINT NOT NULL,
    board_type VARCHAR(20) NOT NULL,
    group_type VARCHAR(20) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    group_discount DECIMAL(10, 2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_bookings_trip FOREIGN KEY (trip_id) REFERENCES trips (id)
);

CREATE TABLE companions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    booking_id BIGINT NOT NULL,
    CONSTRAINT fk_companions_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE
);

CREATE INDEX idx_trips_departure_date ON trips (departure_date);
CREATE INDEX idx_bookings_user ON bookings (user_id);
CREATE INDEX idx_bookings_trip ON bookings (trip_id);
CREATE INDEX idx_companions_booking ON companions (booking_id);
