package com.nomadas.hotel.dto;

import java.math.BigDecimal;

public record HotelResponse(
        Long id,
        String name,
        String description,
        String location,
        Integer totalRooms,
        Integer availableRooms,
        Integer totalPlaces,
        Integer availablePlaces,
        BigDecimal halfBoardPrice,
        BigDecimal fullBoardPrice,
        String imageUrl
) {
}

