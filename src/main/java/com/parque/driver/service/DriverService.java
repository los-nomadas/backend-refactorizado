package com.parque.driver.service;

import com.parque.driver.dto.DriverCreateRequest;
import com.parque.driver.dto.DriverResponse;
import com.parque.driver.dto.DriverUpdateRequest;

import java.util.List;

public interface DriverService {
    List<DriverResponse> getAll();

    DriverResponse getById(Long id);

    DriverResponse create(DriverCreateRequest request);

    DriverResponse update(Long id, DriverUpdateRequest request);

    void delete(Long id);
}
