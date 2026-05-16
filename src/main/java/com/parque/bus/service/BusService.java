package com.parque.bus.service;

import com.parque.bus.dto.BusCreateRequest;
import com.parque.bus.dto.BusResponse;
import com.parque.bus.dto.BusUpdateRequest;

import java.util.List;

public interface BusService {
    List<BusResponse> getAll();

    BusResponse getById(Long id);

    BusResponse create(BusCreateRequest request);

    BusResponse update(Long id, BusUpdateRequest request);

    void delete(Long id);
}
