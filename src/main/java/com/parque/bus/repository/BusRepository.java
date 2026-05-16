package com.parque.bus.repository;

import com.parque.bus.model.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus, Long> {
    boolean existsByPlateNumber(String plateNumber);

    boolean existsByDriverId(Long driverId);

    Optional<Bus> findByPlateNumber(String plateNumber);

    Optional<Bus> findByDriverId(Long driverId);
}
