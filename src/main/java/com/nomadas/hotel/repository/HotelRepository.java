package com.nomadas.hotel.repository;

import com.nomadas.hotel.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    Optional<Hotel> findByName(String name);
}

