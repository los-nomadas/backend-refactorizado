package com.nomadas.hotel;

import com.nomadas.exception.ResourceNotFoundException;
import com.nomadas.hotel.dto.HotelCreateRequest;
import com.nomadas.hotel.dto.HotelResponse;
import com.nomadas.hotel.dto.HotelUpdateRequest;
import com.nomadas.hotel.repository.HotelRepository;
import com.nomadas.hotel.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class HotelServiceTest {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelRepository hotelRepository;

    @BeforeEach
    void setUp() {
        hotelRepository.deleteAll();
    }

    @Test
    void create_shouldCreateHotel() {
        HotelCreateRequest request = new HotelCreateRequest(
                "Hotel Magic Park",
                "Hotel familiar situado junto al parque.",
                "Granada",
                120,
                120,
                240,
                240,
                new BigDecimal("80.0"),
                new BigDecimal("120.0"),
                "https://example.com/hotel.jpg"
        );

        HotelResponse created = hotelService.create(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Hotel Magic Park");
        assertThat(created.totalPlaces()).isEqualTo(240);
        assertThat(created.fullBoardPrice()).isEqualByComparingTo(new BigDecimal("120.0"));
    }

    @Test
    void update_shouldThrowNotFound_whenHotelDoesNotExist() {
        HotelUpdateRequest request = new HotelUpdateRequest(
                "Hotel Magic Park Resort",
                "Hotel familiar situado junto al parque.",
                "Granada",
                120,
                80,
                240,
                160,
                new BigDecimal("90.0"),
                new BigDecimal("130.0"),
                "https://example.com/hotel.jpg"
        );

        assertThatThrownBy(() -> hotelService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hotel not found");
    }
}

