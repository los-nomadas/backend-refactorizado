package com.nomadas.bus;

import com.nomadas.bus.dto.BusCreateRequest;
import com.nomadas.bus.dto.BusResponse;
import com.nomadas.bus.dto.BusUpdateRequest;
import com.nomadas.bus.repository.BusRepository;
import com.nomadas.bus.service.BusService;
import com.nomadas.driver.dto.DriverCreateRequest;
import com.nomadas.driver.dto.DriverResponse;
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
class BusServiceTest {

    @Autowired
    private BusService busService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private DriverRepository driverRepository;

    @BeforeEach
    void setUp() {
        busRepository.deleteAll();
        driverRepository.deleteAll();
    }

    @Test
    void create_shouldCreateBus() {
        DriverResponse driver = saveDriver("98765432B", "A-12345678", "manuel@example.com");
        BusCreateRequest request = new BusCreateRequest("1234-ABC", 55, 55, driver.id());

        BusResponse created = busService.create(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.plateNumber()).isEqualTo("1234-ABC");
        assertThat(created.driverId()).isEqualTo(driver.id());
        assertThat(created.driverFullName()).isEqualTo("Manuel Lopez");
    }

    @Test
    void create_shouldThrowNotFound_whenDriverDoesNotExist() {
        BusCreateRequest request = new BusCreateRequest("1234-ABC", 55, 55, 999L);

        assertThatThrownBy(() -> busService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Driver not found");
    }

    @Test
    void create_shouldThrowConflict_whenDriverAlreadyAssigned() {
        DriverResponse driver = saveDriver("98765432B", "A-12345678", "manuel@example.com");
        busService.create(new BusCreateRequest("1234-ABC", 55, 55, driver.id()));

        assertThatThrownBy(() -> busService.create(new BusCreateRequest("5678-DEF", 50, 50, driver.id())))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Driver is already assigned to another bus in this period");
    }

    @Test
    void update_shouldThrowNotFound_whenBusDoesNotExist() {
        DriverResponse driver = saveDriver("98765432B", "A-12345678", "manuel@example.com");
        BusUpdateRequest request = new BusUpdateRequest("1234-ABC", 55, 55, driver.id());

        assertThatThrownBy(() -> busService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Bus not found");
    }

    private DriverResponse saveDriver(String dni, String licenseNumber, String email) {
        return driverService.create(new DriverCreateRequest(
                "Manuel",
                "Lopez",
                dni,
                licenseNumber,
                "612345678",
                email,
                true
        ));
    }
}
