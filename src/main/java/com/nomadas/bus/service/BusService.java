package com.nomadas.bus.service;

import com.nomadas.bus.dto.BusCreateRequest;
import com.nomadas.bus.dto.BusResponse;
import com.nomadas.bus.dto.BusUpdateRequest;

import java.util.List;

public interface BusService {
    List<BusResponse> getAll();

    BusResponse getById(Long id);

    BusResponse create(BusCreateRequest request);

    BusResponse update(Long id, BusUpdateRequest request);

    void delete(Long id);
}
