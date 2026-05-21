package com.nomadas.trip.service;

import com.nomadas.trip.dto.TripCreateRequest;
import com.nomadas.trip.dto.TripResponse;
import com.nomadas.trip.dto.TripUpdateRequest;

import java.util.List;

public interface TripService {
    List<TripResponse> getAll();

    List<TripResponse> getOffers();

    TripResponse getById(Long id);

    TripResponse create(TripCreateRequest request);

    TripResponse update(Long id, TripUpdateRequest request);

    void delete(Long id);
}
