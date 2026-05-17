package com.nomadas.hotel.service;

import com.nomadas.hotel.dto.HotelCreateRequest;
import com.nomadas.hotel.dto.HotelResponse;
import com.nomadas.hotel.dto.HotelUpdateRequest;

import java.util.List;

public interface HotelService {
    List<HotelResponse> getAll();

    HotelResponse getById(Long id);

    HotelResponse create(HotelCreateRequest request);

    HotelResponse update(Long id, HotelUpdateRequest request);

    void delete(Long id);
}

