package com.nomadas.driver.service;

import com.nomadas.driver.dto.DriverCreateRequest;
import com.nomadas.driver.dto.DriverResponse;
import com.nomadas.driver.dto.DriverUpdateRequest;

import java.util.List;

public interface DriverService {
    List<DriverResponse> getAll();

    DriverResponse getById(Long id);

    DriverResponse create(DriverCreateRequest request);

    DriverResponse update(Long id, DriverUpdateRequest request);

    void delete(Long id);
}
