package com.parque.hotel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import com.parque.booking.model.Booking;
import com.parque.offer.model.Offer;

@Entity
@Table(name = "hotels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String location;

    @NotNull
    @Positive
    private Integer totalRooms;

    @NotNull
    @PositiveOrZero
    private Integer availableRooms;

    @NotNull
    @Positive
    private Integer totalPlaces;

    @NotNull
    @PositiveOrZero
    private Integer availablePlaces;

    @NotNull
    @Positive
    private BigDecimal halfBoardPrice;

    @NotNull
    @Positive
    private BigDecimal fullBoardPrice;

    @NotBlank
    private String imageUrl;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.REMOVE)
    private List<Booking> bookings;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.REMOVE)
    private List<Offer> offers;
}
