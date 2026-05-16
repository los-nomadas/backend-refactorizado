package com.parque.driver.repository;

import com.parque.driver.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    boolean existsByDni(String dni);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByEmail(String email);

    Optional<Driver> findByDni(String dni);

    Optional<Driver> findByLicenseNumber(String licenseNumber);

    Optional<Driver> findByEmail(String email);
}
