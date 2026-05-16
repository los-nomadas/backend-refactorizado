package com.parque.driver.service;

import com.parque.driver.dto.DriverCreateRequest;
import com.parque.driver.dto.DriverResponse;
import com.parque.driver.dto.DriverUpdateRequest;
import com.parque.driver.model.Driver;
import com.parque.driver.repository.DriverRepository;
import com.parque.exception.ConflictException;
import com.parque.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;

    public DriverServiceImpl(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> getAll() {
        return driverRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getById(Long id) {
        Driver driver = driverRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        return toResponse(driver);
    }

    @Override
    public DriverResponse create(DriverCreateRequest request) {
        validateUniqueFields(request.dni(), request.licenseNumber(), request.email(), null);
        Driver driver = Driver.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dni(request.dni())
                .licenseNumber(request.licenseNumber())
                .phone(request.phone())
                .email(request.email())
                .available(request.available() == null || request.available())
                .build();
        return toResponse(driverRepository.save(driver));
    }

    @Override
    public DriverResponse update(Long id, DriverUpdateRequest request) {
        Driver driver = driverRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        validateUniqueFields(request.dni(), request.licenseNumber(), request.email(), id);
        driver.setFirstName(request.firstName());
        driver.setLastName(request.lastName());
        driver.setDni(request.dni());
        driver.setLicenseNumber(request.licenseNumber());
        driver.setPhone(request.phone());
        driver.setEmail(request.email());
        driver.setAvailable(request.available() == null || request.available());
        return toResponse(driverRepository.save(driver));
    }

    @Override
    public void delete(Long id) {
        if (!driverRepository.existsById(id)) {
            throw new ResourceNotFoundException("Driver not found");
        }
        driverRepository.deleteById(id);
    }

    private void validateUniqueFields(String dni, String licenseNumber, String email, Long currentId) {
        driverRepository.findByDni(dni)
                .filter(driver -> !driver.getId().equals(currentId))
                .ifPresent(driver -> {
                    throw new ConflictException("DNI already exists");
                });
        driverRepository.findByLicenseNumber(licenseNumber)
                .filter(driver -> !driver.getId().equals(currentId))
                .ifPresent(driver -> {
                    throw new ConflictException("License number already exists");
                });
        driverRepository.findByEmail(email)
                .filter(driver -> !driver.getId().equals(currentId))
                .ifPresent(driver -> {
                    throw new ConflictException("Email already exists");
                });
    }

    private DriverResponse toResponse(Driver driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getDni(),
                driver.getLicenseNumber(),
                driver.getPhone(),
                driver.getEmail(),
                driver.getAvailable()
        );
    }
}
