package com.nomadas.booking.service;

import com.nomadas.booking.dto.BookingCreateRequest;
import com.nomadas.booking.dto.BookingResponse;

import java.util.List;

public interface BookingService {
    List<BookingResponse> getAll();

    BookingResponse getById(Long id);

    List<BookingResponse> getByUserId(Long userId);

    List<BookingResponse> getMyBookings(Long credentialId);

    BookingResponse create(BookingCreateRequest request);

    void delete(Long id);
}
