package com.nomadas.bus.service;

import com.nomadas.bus.dto.BusCreateRequest;
import com.nomadas.bus.dto.BusResponse;
import com.nomadas.bus.dto.BusUpdateRequest;
import com.nomadas.bus.model.Bus;
import com.nomadas.bus.repository.BusRepository;
import com.nomadas.driver.model.Driver;
import com.nomadas.driver.repository.DriverRepository;
import com.nomadas.exception.ConflictException;
import com.nomadas.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BusServiceImpl implements BusService {

    private final BusRepository busRepository;
    private final DriverRepository driverRepository;

    public BusServiceImpl(BusRepository busRepository, DriverRepository driverRepository) {
        this.busRepository = busRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusResponse> getAll() {
        return busRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BusResponse getById(Long id) {
        Bus bus = busRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bus not found"));
        return toResponse(bus);
    }

    @Override
    public BusResponse create(BusCreateRequest request) {
        Driver driver = driverRepository.findById(request.driverId()).orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        validateAvailability(request.availableSeats(), request.totalSeats());
        validateUniquePlate(request.plateNumber(), null);
        validateDriverAssignment(request.driverId(), null);
        Bus bus = Bus.builder()
                .plateNumber(request.plateNumber())
                .totalSeats(request.totalSeats())
                .availableSeats(request.availableSeats())
                .driver(driver)
                .build();
        return toResponse(busRepository.save(bus));
    }

    @Override
    public BusResponse update(Long id, BusUpdateRequest request) {
        Bus bus = busRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bus not found"));
        Driver driver = driverRepository.findById(request.driverId()).orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        validateAvailability(request.availableSeats(), request.totalSeats());
        validateUniquePlate(request.plateNumber(), id);
        validateDriverAssignment(request.driverId(), id);
        bus.setPlateNumber(request.plateNumber());
        bus.setTotalSeats(request.totalSeats());
        bus.setAvailableSeats(request.availableSeats());
        bus.setDriver(driver);
        return toResponse(busRepository.save(bus));
    }

    @Override
    public void delete(Long id) {
        if (!busRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bus not found");
        }
        busRepository.deleteById(id);
    }

    private void validateAvailability(Integer availableSeats, Integer totalSeats) {
        if (availableSeats > totalSeats) {
            throw new ConflictException("Available seats cannot exceed total seats");
        }
    }

    private void validateUniquePlate(String plateNumber, Long currentId) {
        busRepository.findByPlateNumber(plateNumber)
                .filter(bus -> !bus.getId().equals(currentId))
                .ifPresent(bus -> {
                    throw new ConflictException("Plate number already exists");
                });
    }

    private void validateDriverAssignment(Long driverId, Long currentId) {
        busRepository.findByDriverId(driverId)
                .filter(bus -> !bus.getId().equals(currentId))
                .ifPresent(bus -> {
                    throw new ConflictException("Driver is already assigned to another bus in this period");
                });
    }

    private BusResponse toResponse(Bus bus) {
        Driver driver = bus.getDriver();
        return new BusResponse(
                bus.getId(),
                bus.getPlateNumber(),
                bus.getTotalSeats(),
                bus.getAvailableSeats(),
                driver.getId(),
                driver.getFirstName() + " " + driver.getLastName()
        );
    }
}
