package com.nomadas.driver;

import com.nomadas.driver.dto.DriverCreateRequest;
import com.nomadas.driver.dto.DriverResponse;
import com.nomadas.driver.dto.DriverUpdateRequest;
import com.nomadas.driver.repository.DriverRepository;
import com.nomadas.driver.service.DriverService;
import com.nomadas.exception.ConflictException;
import com.nomadas.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DriverServiceTest {

    @Autowired
    private DriverService driverService;

    @Autowired
    private DriverRepository driverRepository;

    @BeforeEach
    void setUp() {
        driverRepository.deleteAll();
    }

    @Test
    void create_shouldCreateDriver() {
        DriverCreateRequest request = new DriverCreateRequest(
                "Manuel",
                "Lopez",
                "98765432B",
                "A-12345678",
                "612345678",
                "manuel@example.com",
                true
        );

        DriverResponse created = driverService.create(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.firstName()).isEqualTo("Manuel");
        assertThat(created.dni()).isEqualTo("98765432B");
        assertThat(created.licenseNumber()).isEqualTo("A-12345678");
        assertThat(created.available()).isTrue();
    }

    @Test
    void create_shouldThrowConflict_whenDniExists() {
        driverService.create(new DriverCreateRequest(
                "Manuel",
                "Lopez",
                "98765432B",
                "A-12345678",
                "612345678",
                "manuel@example.com",
                true
        ));

        assertThatThrownBy(() -> driverService.create(new DriverCreateRequest(
                "Ana",
                "Garcia",
                "98765432B",
                "B-87654321",
                "612345679",
                "ana.driver@example.com",
                true
        ))).isInstanceOf(ConflictException.class).hasMessage("DNI already exists");
    }

    @Test
    void update_shouldThrowNotFound_whenDriverDoesNotExist() {
        DriverUpdateRequest request = new DriverUpdateRequest(
                "Manuel",
                "Lopez",
                "98765432B",
                "A-12345678",
                "612345678",
                "manuel@example.com",
                true
        );

        assertThatThrownBy(() -> driverService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Driver not found");
    }
}
