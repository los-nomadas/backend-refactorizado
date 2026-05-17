package com.nomadas.trip.model;

import com.nomadas.bus.model.Bus;
import com.nomadas.hotel.model.Hotel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String destination;

    @NotBlank
    @Column(length = 2000)
    private String description;

    @NotNull
    private LocalDate departureDate;

    @NotNull
    private LocalDate returnDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @NotNull
    @Enumerated(EnumType.STRING)
    private BoardType boardType;

    @NotNull
    @Positive
    private BigDecimal priceAdult;

    @NotNull
    @Positive
    private BigDecimal priceChild;

    @NotNull
    @Positive
    private BigDecimal priceSenior;

    @NotNull
    @Positive
    private Integer totalSeats;

    @NotNull
    @PositiveOrZero
    private Integer availableSeats;

    @NotNull
    private Boolean isOffer;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TripStatus status;

    @NotBlank
    private String imageUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
