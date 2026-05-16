package com.parque.booking;

import com.parque.booking.dto.BookingCreateRequest;
import com.parque.booking.dto.BookingResponse;
import com.parque.booking.dto.CompanionRequest;
import com.parque.booking.model.Booking;
import com.parque.booking.repository.BookingRepository;
import com.parque.booking.service.booking.BookingService;
import com.parque.booking.service.notification.NotificationService;
import com.parque.exception.ConflictException;
import com.parque.exception.ResourceNotFoundException;
import com.parque.hotel.model.Hotel;
import com.parque.hotel.repository.HotelRepository;
import com.parque.offer.repository.OfferRepository;
import com.parque.user.model.User;
import com.parque.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private OfferRepository offerRepository;

    @MockitoBean
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        offerRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void create_shouldCreateBookingDecreaseHotelAvailabilityAndMarkEmailAsSent() {
        User user = saveUser("david@example.com", "12345678A");
        Hotel hotel = saveHotel(4, new BigDecimal("80.00"), new BigDecimal("120.00"));
        when(notificationService.sendBookingConfirmation(anyList(), any(Booking.class))).thenReturn(true);

        BookingResponse created = bookingService.create(new BookingCreateRequest(
                user.getId(),
                null,
                hotel.getId(),
                "FULL_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(
                        new CompanionRequest("Ana", "Garcia", LocalDate.parse("1988-03-10")),
                        new CompanionRequest("Lucas", "Garcia", LocalDate.parse("2015-07-20"))
                )
        ));

        assertThat(created.id()).isNotNull();
        assertThat(created.userId()).isEqualTo(user.getId());
        assertThat(created.userFullName()).isEqualTo("David Navarro");
        assertThat(created.hotelId()).isEqualTo(hotel.getId());
        assertThat(created.hotelName()).isEqualTo("Hotel Magic Park");
        assertThat(created.boardType()).isEqualTo("FULL_BOARD");
        assertThat(created.tickets()).hasSize(2);
        assertThat(created.tickets()).extracting(ticket -> ticket.ageRange())
                .containsExactlyInAnyOrder("ADULT", "CHILD");
        assertThat(created.totalPrice()).isEqualByComparingTo("215.00");
        assertThat(created.emailSent()).isTrue();
        assertThat(created.createdAt()).isNotNull();

        verify(notificationService).sendBookingConfirmation(eq(List.of("david@example.com")), any(Booking.class));

        Hotel updatedHotel = hotelRepository.findById(hotel.getId()).orElseThrow();
        assertThat(updatedHotel.getAvailablePlaces()).isEqualTo(2);
    }

    @Test
    void create_shouldKeepEmailSentFalse_whenNotificationFails() {
        User user = saveUser("david@example.com", "12345678A");
        Hotel hotel = saveHotel(4, new BigDecimal("80.00"), new BigDecimal("120.00"));
        when(notificationService.sendBookingConfirmation(anyList(), any(Booking.class))).thenReturn(false);

        BookingResponse created = bookingService.create(new BookingCreateRequest(
                user.getId(),
                null,
                hotel.getId(),
                "FULL_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(
                        new CompanionRequest("Ana", "Garcia", LocalDate.parse("1988-03-10")),
                        new CompanionRequest("Lucas", "Garcia", LocalDate.parse("2015-07-20"))
                )
        ));

        assertThat(created.emailSent()).isFalse();
    }

    @Test
    void create_shouldThrowNotFound_whenUserDoesNotExist() {
        Hotel hotel = saveHotel(4, new BigDecimal("80.00"), new BigDecimal("120.00"));

        assertThatThrownBy(() -> bookingService.create(new BookingCreateRequest(
                999L,
                null,
                hotel.getId(),
                "FULL_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(new CompanionRequest("Ana", "Garcia", LocalDate.parse("1988-03-10")))
        ))).isInstanceOf(ResourceNotFoundException.class).hasMessage("User not found");
    }

    @Test
    void create_shouldThrowConflict_whenHotelIsFull() {
        User user = saveUser("david@example.com", "12345678A");
        Hotel hotel = saveHotel(1, new BigDecimal("80.00"), new BigDecimal("120.00"));

        assertThatThrownBy(() -> bookingService.create(new BookingCreateRequest(
                user.getId(),
                null,
                hotel.getId(),
                "FULL_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(
                        new CompanionRequest("Ana", "Garcia", LocalDate.parse("1988-03-10")),
                        new CompanionRequest("Lucas", "Garcia", LocalDate.parse("2015-07-20"))
                )
        ))).isInstanceOf(ConflictException.class).hasMessage("Hotel is full");
    }

    @Test
    void create_shouldThrowConflict_whenMinorTravelsWithoutAdult() {
        User user = saveUser("david@example.com", "12345678A");

        assertThatThrownBy(() -> bookingService.create(new BookingCreateRequest(
                user.getId(),
                null,
                null,
                "HALF_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(new CompanionRequest("Lucas", "Garcia", LocalDate.parse("2015-07-20")))
        ))).isInstanceOf(ConflictException.class).hasMessage("A minor cannot travel without an adult");
    }

    @Test
    void create_shouldThrowNotFound_whenHotelDoesNotExist() {
        User user = saveUser("david@example.com", "12345678A");

        assertThatThrownBy(() -> bookingService.create(new BookingCreateRequest(
                user.getId(),
                null,
                999L,
                "FULL_BOARD",
                LocalDate.parse("2026-05-22"),
                List.of(new CompanionRequest("Ana", "Garcia", LocalDate.parse("1988-03-10")))
        ))).isInstanceOf(ResourceNotFoundException.class).hasMessage("Hotel not found");
    }

    @Test
    void getById_shouldThrowNotFound_whenBookingDoesNotExist() {
        assertThatThrownBy(() -> bookingService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Booking not found");
    }

    private User saveUser(String email, String dni) {
        return userRepository.save(User.builder()
                .firstName("David")
                .lastName("Navarro")
                .dni(dni)
                .email(email)
                .phone("600123123")
                .birthDate(LocalDate.parse("1990-04-15"))
                .build());
    }

    private Hotel saveHotel(int availablePlaces, BigDecimal halfBoardPrice, BigDecimal fullBoardPrice) {
        return hotelRepository.save(Hotel.builder()
                .name("Hotel Magic Park")
                .description("Hotel familiar situado junto al parque.")
                .location("Granada")
                .totalRooms(120)
                .availableRooms(120)
                .totalPlaces(240)
                .availablePlaces(availablePlaces)
                .halfBoardPrice(halfBoardPrice)
                .fullBoardPrice(fullBoardPrice)
                .imageUrl("https://example.com/hotel.jpg")
                .build());
    }
}
