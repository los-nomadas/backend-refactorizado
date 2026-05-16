package com.parque.config;

import com.parque.auth.model.InternalRole;
import com.parque.auth.repository.InternalCredentialRepository;
import com.parque.attraction.model.Attraction;
import com.parque.attraction.repository.AttractionRepository;
import com.parque.booking.dto.BookingCreateRequest;
import com.parque.booking.dto.CompanionRequest;
import com.parque.booking.repository.BookingRepository;
import com.parque.booking.service.booking.BookingService;
import com.parque.employee.model.Employee;
import com.parque.employee.repository.EmployeeRepository;
import com.parque.entity.InternalCredential;
import com.parque.hotel.model.Hotel;
import com.parque.hotel.repository.HotelRepository;
import com.parque.maintenance.dto.MaintenanceGenerateRequest;
import com.parque.maintenance.service.MaintenanceService;
import com.parque.offer.model.Offer;
import com.parque.offer.repository.OfferRepository;
import com.parque.shift.dto.ShiftGenerateRequest;
import com.parque.shift.service.ShiftService;
import com.parque.user.model.User;
import com.parque.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Configuration
@Profile({"dev", "e2e"})
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataConfig {

    @Value("${app.demo-admin.username}")
    private String demoAdminUsername;

    @Value("${app.demo-admin.email}")
    private String demoAdminEmail;

    @Value("${app.demo-admin.password}")
    private String demoAdminPassword;

    @Bean
    public CommandLineRunner demoDataInitializer(
            InternalCredentialRepository internalCredentialRepository,
            UserRepository userRepository,
            HotelRepository hotelRepository,
            AttractionRepository attractionRepository,
            EmployeeRepository employeeRepository,
            OfferRepository offerRepository,
            BookingRepository bookingRepository,
            BookingService bookingService,
            ShiftService shiftService,
            MaintenanceService maintenanceService,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            seedInternalCredential(internalCredentialRepository, passwordEncoder);

            Hotel magicPark = syncHotel(hotelRepository, demoMagicParkHotel());
            Hotel adventure = syncHotel(hotelRepository, demoAdventureHotel());
            Hotel fantasy = syncHotel(hotelRepository, demoFantasyHotel());
            Hotel puertaNegra = hotelRepository.findByName("Hotel Puerta Negra").orElse(magicPark);
            Hotel mansionCarmesi = hotelRepository.findByName("Hotel Mansión Carmesí").orElse(adventure);

            syncAttraction(attractionRepository, demoDragonCoasterAttraction());
            syncAttraction(attractionRepository, demoSplashRiverAttraction());
            syncAttraction(attractionRepository, demoFantasyCarouselAttraction());

            syncOffer(offerRepository, bookingRepository, demoMagicParkOffer(puertaNegra));
            syncOffer(offerRepository, bookingRepository, demoAdventureOffer(mansionCarmesi));

            if (userRepository.count() > 0 || bookingRepository.count() > 0 || employeeRepository.count() > 0) {
                return;
            }

            User david = userRepository.save(User.builder()
                    .firstName("David")
                    .lastName("Navarro")
                    .dni("12345678A")
                    .email("david@example.com")
                    .phone("600123123")
                    .birthDate(LocalDate.parse("1990-04-15"))
                    .build());

            User ana = userRepository.save(User.builder()
                    .firstName("Ana")
                    .lastName("Garcia")
                    .dni("87654321B")
                    .email("ana@example.com")
                    .phone("611222333")
                    .birthDate(LocalDate.parse("1988-03-10"))
                    .build());

            employeeRepository.saveAll(List.of(
                    employee("Laura", "Gomez", "11111111A", "laura.gomez@example.com", "TECHNICIAN", "MORNING"),
                    employee("Mario", "Lopez", "22222222B", "mario.lopez@example.com", "TECHNICIAN", "AFTERNOON"),
                    employee("Clara", "Santos", "33333333C", "clara.santos@example.com", "TECHNICIAN", "MORNING"),
                    employee("Lucia", "Martin", "44444444D", "lucia.martin@example.com", "CLEANER", "MORNING"),
                    employee("Jorge", "Ruiz", "55555555E", "jorge.ruiz@example.com", "CLEANER", "AFTERNOON"),
                    employee("Paula", "Vega", "66666666F", "paula.vega@example.com", "CLEANER", "MORNING"),
                    employee("Elena", "Perez", "77777777G", "elena.perez@example.com", "ANIMATOR", "MORNING"),
                    employee("Diego", "Moreno", "88888888H", "diego.moreno@example.com", "ANIMATOR", "AFTERNOON"),
                    employee("Sofia", "Navarro", "99999999J", "sofia.navarro@example.com", "ANIMATOR", "MORNING")
            ));

            bookingService.create(new BookingCreateRequest(
                    david.getId(),
                    null,
                    magicPark.getId(),
                    "FULL_BOARD",
                    LocalDate.now().plusDays(10),
                    List.of(
                            new CompanionRequest("David", "Navarro", LocalDate.parse("1990-04-15")),
                            new CompanionRequest("Lucas", "Navarro", LocalDate.parse("2015-07-20")),
                            new CompanionRequest("Maria", "Navarro", LocalDate.parse("1952-02-11"))
                    )
            ));

            bookingService.create(new BookingCreateRequest(
                    ana.getId(),
                    null,
                    adventure.getId(),
                    "HALF_BOARD",
                    LocalDate.now().plusDays(20),
                    List.of(
                            new CompanionRequest("Ana", "Garcia", LocalDate.parse("1988-03-10")),
                            new CompanionRequest("Nora", "Garcia", LocalDate.parse("2014-09-22"))
                    )
            ));

            bookingService.create(new BookingCreateRequest(
                    david.getId(),
                    null,
                    fantasy.getId(),
                    "HALF_BOARD",
                    LocalDate.now().plusDays(30),
                    List.of(
                            new CompanionRequest("Pedro", "Lopez", LocalDate.parse("1984-11-05")),
                            new CompanionRequest("Laura", "Lopez", LocalDate.parse("1986-06-18"))
                    )
            ));

            LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            shiftService.generate(new ShiftGenerateRequest(monthStart, monthEnd));
            maintenanceService.generate(new MaintenanceGenerateRequest(LocalDate.now(), LocalDate.now().plusDays(30)));
        };
    }

    private void seedInternalCredential(
            InternalCredentialRepository internalCredentialRepository,
            PasswordEncoder passwordEncoder
    ) {
        if (internalCredentialRepository.count() > 0) {
            return;
        }

        internalCredentialRepository.save(InternalCredential.builder()
                .username(demoAdminUsername)
                .email(demoAdminEmail)
                .passwordHash(passwordEncoder.encode(demoAdminPassword))
                .role(InternalRole.ADMIN)
                .active(true)
                .build());

        internalCredentialRepository.save(InternalCredential.builder()
                .username("manager")
                .email("manager@parque.local")
                .passwordHash(passwordEncoder.encode("manager123"))
                .role(InternalRole.MANAGER)
                .active(true)
                .build());

        internalCredentialRepository.save(InternalCredential.builder()
                .username("employee")
                .email("employee@parque.local")
                .passwordHash(passwordEncoder.encode("employee123"))
                .role(InternalRole.EMPLOYEE)
                .active(true)
                .build());

        internalCredentialRepository.save(InternalCredential.builder()
                .username("user")
                .email("user@parque.local")
                .passwordHash(passwordEncoder.encode("user12345"))
                .role(InternalRole.USER)
                .active(true)
                .build());
    }

    private Hotel syncHotel(HotelRepository hotelRepository, Hotel demoHotel) {
        Hotel hotel = hotelRepository.findByName(demoHotel.getName())
                .map(existingHotel -> {
                    existingHotel.setDescription(demoHotel.getDescription());
                    existingHotel.setHalfBoardPrice(demoHotel.getHalfBoardPrice());
                    existingHotel.setFullBoardPrice(demoHotel.getFullBoardPrice());
                    existingHotel.setImageUrl(demoHotel.getImageUrl());
                    return existingHotel;
                })
                .orElse(demoHotel);

        return hotelRepository.save(hotel);
    }

    private Attraction syncAttraction(AttractionRepository attractionRepository, Attraction demoAttraction) {
        Attraction attraction = attractionRepository.findByName(demoAttraction.getName())
                .map(existingAttraction -> {
                    existingAttraction.setDescription(demoAttraction.getDescription());
                    existingAttraction.setSize(demoAttraction.getSize());
                    existingAttraction.setImageUrl(demoAttraction.getImageUrl());
                    existingAttraction.setMaintenanceFrequencyDays(demoAttraction.getMaintenanceFrequencyDays());
                    return existingAttraction;
                })
                .orElse(demoAttraction);

        return attractionRepository.save(attraction);
    }

    private Offer syncOffer(OfferRepository offerRepository, BookingRepository bookingRepository, Offer demoOffer) {
        List<Offer> matchingOffers = offerRepository.findAll().stream()
                .filter(existingOffer -> matchesDemoOffer(existingOffer, demoOffer))
                .sorted(Comparator.comparing(Offer::getId))
                .toList();

        Offer offer = matchingOffers.isEmpty()
                ? demoOffer
                : matchingOffers.getFirst();

        offer.setTitle(demoOffer.getTitle());
        offer.setDescription(demoOffer.getDescription());
        offer.setHotel(demoOffer.getHotel());
        offer.setBoardType(demoOffer.getBoardType());
        offer.setIncludedTickets(demoOffer.getIncludedTickets());
        offer.setTotalPrice(demoOffer.getTotalPrice());
        offer.setImageUrl(demoOffer.getImageUrl());

        Offer savedOffer = offerRepository.save(offer);

        matchingOffers.stream()
                .skip(1)
                .filter(existingOffer -> !bookingRepository.existsByOfferId(existingOffer.getId()))
                .forEach(offerRepository::delete);

        return savedOffer;
    }

    private boolean matchesDemoOffer(Offer existingOffer, Offer demoOffer) {
        if (existingOffer.getTitle().equals(demoOffer.getTitle())) {
            return true;
        }

        if (existingOffer.getImageUrl().equals(demoOffer.getImageUrl())) {
            return true;
        }

        return normalizeOfferTitle(existingOffer.getTitle()).equals(normalizeOfferTitle(demoOffer.getTitle()));
    }

    private String normalizeOfferTitle(String title) {
        return title == null
                ? ""
                : title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Hotel demoMagicParkHotel() {
        return Hotel.builder()
                .name("Hotel Umbral Nocturno")
                .description("Hotel familiar situado junto al acceso principal, con ambiente oscuro, detalles rojos y una estancia cómoda antes de cruzar la puerta.")
                .location("Granada")
                .totalRooms(120)
                .availableRooms(120)
                .totalPlaces(240)
                .availablePlaces(240)
                .halfBoardPrice(new BigDecimal("80.00"))
                .fullBoardPrice(new BigDecimal("120.00"))
                .imageUrl("https://res.cloudinary.com/dp3qqp2ns/image/upload/v1778588321/hotels/Hotel_Refugio_de_las_Sombras_m7bsct.png")
                .build();
    }

    private Hotel demoAdventureHotel() {
        return Hotel.builder()
                .name("Hotel Sendero Carmesí")
                .description("Alojamiento temático para escapadas breves, con pasillos de luz roja, estética de misterio y acceso rápido a las zonas del parque.")
                .location("Granada")
                .totalRooms(90)
                .availableRooms(90)
                .totalPlaces(180)
                .availablePlaces(180)
                .halfBoardPrice(new BigDecimal("70.00"))
                .fullBoardPrice(new BigDecimal("110.00"))
                .imageUrl("https://res.cloudinary.com/dp3qqp2ns/image/upload/v1778589573/hotels/Hotel_Mansi%C3%B3n_Cremes%C3%AD_fdxncs.png")
                .build();
    }

    private Hotel demoFantasyHotel() {
        return Hotel.builder()
                .name("Hotel Refugio de las Sombras")
                .description("Hotel premium para familias, con habitaciones amplias, ambiente nocturno y una estética cuidada para descansar tras la visita.")
                .location("Granada")
                .totalRooms(80)
                .availableRooms(80)
                .totalPlaces(160)
                .availablePlaces(160)
                .halfBoardPrice(new BigDecimal("65.00"))
                .fullBoardPrice(new BigDecimal("95.00"))
                .imageUrl("https://res.cloudinary.com/dp3qqp2ns/image/upload/v1778588939/hotels/Hotel_Fantas%C3%ADa_Nocturna_pfwnhg.png")
                .build();
    }

    private Attraction demoDragonCoasterAttraction() {
        return Attraction.builder()
                .name("Torre del Terror")
                .description("Atracción intensa de altura con ambiente oscuro, vistas al parque y una caída diseñada para los visitantes más valientes.")
                .size("LARGE")
                .status("OPEN")
                .totalSeats(32)
                .availableSeats(32)
                .maintenanceFrequencyDays(7)
                .imageUrl("https://res.cloudinary.com/dp3qqp2ns/image/upload/v1778222227/attractions/attractionTerrorTower_hbkqm6.png")
                .build();
    }

    private Attraction demoSplashRiverAttraction() {
        return Attraction.builder()
                .name("Río de Sangre")
                .description("Recorrido acuático oscuro con barcas temáticas, niebla baja y luces rojas para una experiencia intensa pero controlada.")
                .size("MEDIUM")
                .status("OPEN")
                .totalSeats(24)
                .availableSeats(24)
                .maintenanceFrequencyDays(14)
                .imageUrl("https://res.cloudinary.com/dp3qqp2ns/image/upload/v1778221870/attractions/attractionBloodRiver_kx4mxb.png")
                .build();
    }

    private Attraction demoFantasyCarouselAttraction() {
        return Attraction.builder()
                .name("Laberinto de las Sombras")
                .description("Recorrido inmersivo a pie entre pasillos oscuros, niebla baja y luces rojas diseñado para perder la orientación sin perder la seguridad.")
                .size("SMALL")
                .status("OPEN")
                .totalSeats(18)
                .availableSeats(18)
                .maintenanceFrequencyDays(30)
                .imageUrl("https://res.cloudinary.com/dp3qqp2ns/image/upload/v1778221799/attractions/attractionDarkLabyrinth_yqjgnt.png")
                .build();
    }

    private Offer demoMagicParkOffer(Hotel hotel) {
        return Offer.builder()
                .title("Pack Familiar Puerta Negra")
                .description("Hotel + entradas familiares para cruzar la puerta con una estancia completa junto al parque.")
                .hotel(hotel)
                .boardType("FULL_BOARD")
                .includedTickets(4)
                .totalPrice(new BigDecimal("399.99"))
                .imageUrl("https://res.cloudinary.com/dp3qqp2ns/image/upload/v1778758628/offers/Pack_Familiar_Puerta_Negra_ivhhzf.png")
                .build();
    }

    private Offer demoAdventureOffer(Hotel hotel) {
        return Offer.builder()
                .title("Pack Noche Carmesí")
                .description("Escapada temática con hotel, entradas y ambiente nocturno para vivir el parque al caer la noche.")
                .hotel(hotel)
                .boardType("HALF_BOARD")
                .includedTickets(2)
                .totalPrice(new BigDecimal("249.99"))
                .imageUrl("https://res.cloudinary.com/dp3qqp2ns/image/upload/v1778756951/offers/Pack_Noche_Carmes%C3%AD_cpmqkz.png")
                .build();
    }

    private Employee employee(
            String firstName,
            String lastName,
            String dni,
            String email,
            String employeeType,
            String shift
    ) {
        return Employee.builder()
                .firstName(firstName)
                .lastName(lastName)
                .dni(dni)
                .email(email)
                .employeeType(employeeType)
                .shift(shift)
                .active(true)
                .build();
    }
}
