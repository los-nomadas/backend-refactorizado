package com.nomadas.config;

import com.nomadas.auth.model.InternalRole;
import com.nomadas.auth.repository.InternalCredentialRepository;
import com.nomadas.bus.model.Bus;
import com.nomadas.bus.repository.BusRepository;
import com.nomadas.driver.model.Driver;
import com.nomadas.driver.repository.DriverRepository;
import com.nomadas.entity.InternalCredential;
import com.nomadas.hotel.model.Hotel;
import com.nomadas.hotel.repository.HotelRepository;
import com.nomadas.trip.model.BoardType;
import com.nomadas.trip.model.Trip;
import com.nomadas.trip.model.TripStatus;
import com.nomadas.trip.repository.TripRepository;
import com.nomadas.user.model.User;
import com.nomadas.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
            PasswordEncoder passwordEncoder,
            DriverRepository driverRepository,
            BusRepository busRepository,
            HotelRepository hotelRepository,
            TripRepository tripRepository
    ) {
        return args -> {
            seedAuth(internalCredentialRepository, userRepository, passwordEncoder);
            seedTrips(driverRepository, busRepository, hotelRepository, tripRepository);
        };
    }

    private void seedAuth(
            InternalCredentialRepository internalCredentialRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        if (internalCredentialRepository.count() > 0) {
            return;
        }

        User demoCustomer = userRepository.save(User.builder()
                .firstName("Admin")
                .lastName("Nomadas")
                .dni("00000000A")
                .email(demoAdminEmail)
                .phone("600000000")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build());

        internalCredentialRepository.save(InternalCredential.builder()
                .username(demoAdminUsername)
                .email(demoAdminEmail)
                .passwordHash(passwordEncoder.encode(demoAdminPassword))
                .role(InternalRole.ADMIN)
                .active(true)
                .user(demoCustomer)
                .build());
    }

    private void seedTrips(
            DriverRepository driverRepository,
            BusRepository busRepository,
            HotelRepository hotelRepository,
            TripRepository tripRepository
    ) {
        if (tripRepository.count() > 0) {
            return;
        }

        List<Driver> drivers = driverRepository.saveAll(List.of(
                Driver.builder().firstName("Juan").lastName("García Martínez").dni("12345678A")
                        .licenseNumber("LIC-ES-001").phone("600100001").email("juan.garcia@nomadas.local").available(true).build(),
                Driver.builder().firstName("María").lastName("López Sánchez").dni("23456789B")
                        .licenseNumber("LIC-ES-002").phone("600100002").email("maria.lopez@nomadas.local").available(true).build(),
                Driver.builder().firstName("Carlos").lastName("Rodríguez Pérez").dni("34567890C")
                        .licenseNumber("LIC-ES-003").phone("600100003").email("carlos.rodriguez@nomadas.local").available(true).build(),
                Driver.builder().firstName("Ana").lastName("Fernández González").dni("45678901D")
                        .licenseNumber("LIC-ES-004").phone("600100004").email("ana.fernandez@nomadas.local").available(true).build(),
                Driver.builder().firstName("Pedro").lastName("Martínez García").dni("56789012E")
                        .licenseNumber("LIC-ES-005").phone("600100005").email("pedro.martinez@nomadas.local").available(true).build(),
                Driver.builder().firstName("Laura").lastName("Sánchez López").dni("67890123F")
                        .licenseNumber("LIC-ES-006").phone("600100006").email("laura.sanchez@nomadas.local").available(true).build(),
                Driver.builder().firstName("Manuel").lastName("González Rodríguez").dni("78901234G")
                        .licenseNumber("LIC-ES-007").phone("600100007").email("manuel.gonzalez@nomadas.local").available(true).build(),
                Driver.builder().firstName("Isabel").lastName("Pérez Fernández").dni("89012345H")
                        .licenseNumber("LIC-ES-008").phone("600100008").email("isabel.perez@nomadas.local").available(true).build(),
                Driver.builder().firstName("Francisco").lastName("Martín Torres").dni("90123456J")
                        .licenseNumber("LIC-ES-009").phone("600100009").email("francisco.martin@nomadas.local").available(true).build(),
                Driver.builder().firstName("Elena").lastName("Torres Martín").dni("01234567K")
                        .licenseNumber("LIC-ES-010").phone("600100010").email("elena.torres@nomadas.local").available(true).build()
        ));

        List<Bus> buses = busRepository.saveAll(List.of(
                Bus.builder().plateNumber("1234NMD").totalSeats(55).availableSeats(55).driver(drivers.get(0)).build(),
                Bus.builder().plateNumber("2345NMD").totalSeats(55).availableSeats(55).driver(drivers.get(1)).build(),
                Bus.builder().plateNumber("3456NMD").totalSeats(55).availableSeats(55).driver(drivers.get(2)).build(),
                Bus.builder().plateNumber("4567NMD").totalSeats(55).availableSeats(55).driver(drivers.get(3)).build(),
                Bus.builder().plateNumber("5678NMD").totalSeats(55).availableSeats(55).driver(drivers.get(4)).build(),
                Bus.builder().plateNumber("6789NMD").totalSeats(55).availableSeats(55).driver(drivers.get(5)).build(),
                Bus.builder().plateNumber("7890NMD").totalSeats(55).availableSeats(55).driver(drivers.get(6)).build(),
                Bus.builder().plateNumber("8901NMD").totalSeats(55).availableSeats(55).driver(drivers.get(7)).build(),
                Bus.builder().plateNumber("9012NMD").totalSeats(55).availableSeats(55).driver(drivers.get(8)).build(),
                Bus.builder().plateNumber("0123NMD").totalSeats(55).availableSeats(55).driver(drivers.get(9)).build()
        ));

        List<Hotel> hotels = hotelRepository.saveAll(List.of(
                Hotel.builder().name("Hôtel Plaza Athénée").description("Hotel de lujo en el corazón de París, frente a la Torre Eiffel. Elegancia francesa en su máxima expresión con habitaciones de diseño exclusivo.")
                        .location("Avenue Montaigne, París, Francia").totalRooms(150).availableRooms(150).totalPlaces(300).availablePlaces(300)
                        .halfBoardPrice(new BigDecimal("180.00")).fullBoardPrice(new BigDecimal("240.00"))
                        .imageUrl("https://images.unsplash.com/photo-1499856871958-5b9627545d1a?auto=format&fit=crop&w=1200&q=80").build(),
                Hotel.builder().name("Hotel Eden Roma").description("Hotel clásico con vistas al Coliseo. Arquitectura histórica italiana con todas las comodidades modernas a pasos de los monumentos más emblemáticos de Roma.")
                        .location("Via Nazionale, Roma, Italia").totalRooms(120).availableRooms(120).totalPlaces(240).availablePlaces(240)
                        .halfBoardPrice(new BigDecimal("130.00")).fullBoardPrice(new BigDecimal("175.00"))
                        .imageUrl("https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=1200&q=80").build(),
                Hotel.builder().name("Park Hyatt Tokyo").description("Hotel de lujo en Shinjuku con impresionantes vistas al Monte Fuji y al skyline de Tokio. Diseño contemporáneo japonés con spa y restaurantes de primera.")
                        .location("Shinjuku, Tokio, Japón").totalRooms(180).availableRooms(180).totalPlaces(360).availablePlaces(360)
                        .halfBoardPrice(new BigDecimal("200.00")).fullBoardPrice(new BigDecimal("260.00"))
                        .imageUrl("https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?auto=format&fit=crop&w=1200&q=80").build(),
                Hotel.builder().name("The Peninsula New York").description("Icónico hotel de Manhattan con vistas espectaculares al skyline neoyorkino. Servicio legendario a una manzana del Central Park y la Quinta Avenida.")
                        .location("Fifth Avenue, Nueva York, EE.UU.").totalRooms(235).availableRooms(235).totalPlaces(470).availablePlaces(470)
                        .halfBoardPrice(new BigDecimal("210.00")).fullBoardPrice(new BigDecimal("275.00"))
                        .imageUrl("https://images.unsplash.com/photo-1546436836-07a91091f160?auto=format&fit=crop&w=1200&q=80").build(),
                Hotel.builder().name("Canaves Oia Suites").description("Suites exclusivas en el pueblo de Oia con las famosas cúpulas azules y puestas de sol legendarias. Piscinas privadas con vistas a la caldera volcánica.")
                        .location("Oia, Santorini, Grecia").totalRooms(30).availableRooms(30).totalPlaces(60).availablePlaces(60)
                        .halfBoardPrice(new BigDecimal("160.00")).fullBoardPrice(new BigDecimal("210.00"))
                        .imageUrl("https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=1200&q=80").build(),
                Hotel.builder().name("The Savoy").description("El hotel más legendario de Londres fundado en 1889. Situado junto al Támesis con vistas al Puente de Waterloo. Historia viva de la cultura británica.")
                        .location("Strand, Londres, Reino Unido").totalRooms(267).availableRooms(267).totalPlaces(534).availablePlaces(534)
                        .halfBoardPrice(new BigDecimal("170.00")).fullBoardPrice(new BigDecimal("230.00"))
                        .imageUrl("https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?auto=format&fit=crop&w=1200&q=80").build(),
                Hotel.builder().name("Burj Al Arab").description("El único hotel de 7 estrellas del mundo, construido sobre una isla artificial con forma de vela. Helipuerto, suite real y gastronomía de autor en Dubái.")
                        .location("Jumeirah Beach, Dubái, EAU").totalRooms(202).availableRooms(202).totalPlaces(404).availablePlaces(404)
                        .halfBoardPrice(new BigDecimal("250.00")).fullBoardPrice(new BigDecimal("320.00"))
                        .imageUrl("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=1200&q=80").build(),
                Hotel.builder().name("Four Seasons Bali at Sayan").description("Resort de lujo entre los arrozales de Ubud, suspendido sobre el río Ayung. Villas privadas con bañera exterior y acceso a los templos sagrados balineses.")
                        .location("Sayan, Ubud, Bali, Indonesia").totalRooms(60).availableRooms(60).totalPlaces(120).availablePlaces(120)
                        .halfBoardPrice(new BigDecimal("140.00")).fullBoardPrice(new BigDecimal("195.00"))
                        .imageUrl("https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1200&q=80").build(),
                Hotel.builder().name("Hotel Paris Prague").description("Hotel elegante en el corazón del Casco Antiguo de Praga, a pasos del Puente de Carlos. Arquitectura art nouveau con todas las comodidades contemporáneas.")
                        .location("Staré Město, Praga, República Checa").totalRooms(94).availableRooms(94).totalPlaces(188).availablePlaces(188)
                        .halfBoardPrice(new BigDecimal("90.00")).fullBoardPrice(new BigDecimal("125.00"))
                        .imageUrl("https://images.unsplash.com/photo-1592906209472-a36b1f3782ef?auto=format&fit=crop&w=1200&q=80").build(),
                Hotel.builder().name("Soneva Fushi").description("El resort más exclusivo de las Maldivas, con villas sobre el agua con tobogán privado al océano. Arrecifes de coral vírgenes y máxima privacidad en el paraíso.")
                        .location("Baa Atoll, Maldivas").totalRooms(65).availableRooms(65).totalPlaces(130).availablePlaces(130)
                        .halfBoardPrice(new BigDecimal("280.00")).fullBoardPrice(new BigDecimal("360.00"))
                        .imageUrl("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?auto=format&fit=crop&w=1200&q=80").build()
        ));

        tripRepository.saveAll(List.of(
                Trip.builder().destination("París").description("Descubre la Ciudad de la Luz: la Torre Eiffel, el Louvre y los bulevares más románticos del mundo. Incluye visitas guiadas al Museo de Orsay y crucero nocturno por el Sena.")
                        .departureDate(LocalDate.of(2026, 7, 10)).returnDate(LocalDate.of(2026, 7, 17))
                        .hotel(hotels.get(0)).bus(buses.get(0)).boardType(BoardType.FULL_BOARD)
                        .priceAdult(new BigDecimal("1890.00")).priceChild(new BigDecimal("1250.00")).priceSenior(new BigDecimal("1680.00"))
                        .totalSeats(50).availableSeats(50).isOffer(true).status(TripStatus.AVAILABLE)
                        .imageUrl("https://images.unsplash.com/photo-1499856871958-5b9627545d1a?auto=format&fit=crop&w=1200&q=80").build(),

                Trip.builder().destination("Roma").description("La Ciudad Eterna te espera: el Coliseo, el Vaticano y la Fontana di Trevi. Pasta fresca, gelato y arte renacentista en cada esquina. Visitas guiadas incluidas.")
                        .departureDate(LocalDate.of(2026, 6, 15)).returnDate(LocalDate.of(2026, 6, 22))
                        .hotel(hotels.get(1)).bus(buses.get(1)).boardType(BoardType.HALF_BOARD)
                        .priceAdult(new BigDecimal("1490.00")).priceChild(new BigDecimal("990.00")).priceSenior(new BigDecimal("1320.00"))
                        .totalSeats(50).availableSeats(50).isOffer(false).status(TripStatus.AVAILABLE)
                        .imageUrl("https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=1200&q=80").build(),

                Trip.builder().destination("Tokio").description("Sumérgete en el contraste entre tradición y modernidad de la capital japonesa. Templos milenarios, tecnología punta, sushi auténtico y la magia del distrito Shibuya.")
                        .departureDate(LocalDate.of(2026, 8, 5)).returnDate(LocalDate.of(2026, 8, 17))
                        .hotel(hotels.get(2)).bus(buses.get(2)).boardType(BoardType.FULL_BOARD)
                        .priceAdult(new BigDecimal("3190.00")).priceChild(new BigDecimal("1990.00")).priceSenior(new BigDecimal("2790.00"))
                        .totalSeats(50).availableSeats(50).isOffer(false).status(TripStatus.AVAILABLE)
                        .imageUrl("https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?auto=format&fit=crop&w=1200&q=80").build(),

                Trip.builder().destination("Nueva York").description("La Gran Manzana en todo su esplendor: Times Square, Central Park, la Estatua de la Libertad y los mejores musicales de Broadway. La ciudad que nunca duerme te espera.")
                        .departureDate(LocalDate.of(2026, 9, 12)).returnDate(LocalDate.of(2026, 9, 21))
                        .hotel(hotels.get(3)).bus(buses.get(3)).boardType(BoardType.FULL_BOARD)
                        .priceAdult(new BigDecimal("2590.00")).priceChild(new BigDecimal("1690.00")).priceSenior(new BigDecimal("2290.00"))
                        .totalSeats(50).availableSeats(50).isOffer(true).status(TripStatus.AVAILABLE)
                        .imageUrl("https://images.unsplash.com/photo-1546436836-07a91091f160?auto=format&fit=crop&w=1200&q=80").build(),

                Trip.builder().destination("Santorini").description("Las icónicas cúpulas azules de Oia y los atardeceres más espectaculares del Mediterráneo. Vino local, gastronomía griega y playas de arena volcánica negra.")
                        .departureDate(LocalDate.of(2026, 7, 20)).returnDate(LocalDate.of(2026, 7, 27))
                        .hotel(hotels.get(4)).bus(buses.get(4)).boardType(BoardType.HALF_BOARD)
                        .priceAdult(new BigDecimal("1790.00")).priceChild(new BigDecimal("1150.00")).priceSenior(new BigDecimal("1590.00"))
                        .totalSeats(50).availableSeats(50).isOffer(false).status(TripStatus.AVAILABLE)
                        .imageUrl("https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=1200&q=80").build(),

                Trip.builder().destination("Londres").description("La capital británica con el Big Ben, el Palacio de Buckingham y los mejores museos del mundo, todos gratuitos. Afternoon tea, pubs históricos y teatro en el West End.")
                        .departureDate(LocalDate.of(2026, 6, 5)).returnDate(LocalDate.of(2026, 6, 9))
                        .hotel(hotels.get(5)).bus(buses.get(5)).boardType(BoardType.HALF_BOARD)
                        .priceAdult(new BigDecimal("1390.00")).priceChild(new BigDecimal("950.00")).priceSenior(new BigDecimal("1250.00"))
                        .totalSeats(50).availableSeats(50).isOffer(false).status(TripStatus.AVAILABLE)
                        .imageUrl("https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?auto=format&fit=crop&w=1200&q=80").build(),

                Trip.builder().destination("Dubái").description("El futuro hecho realidad: el Burj Khalifa, el Mall of the Emirates y el desierto en camello. Shopping de lujo, playas privadas y la arquitectura más audaz del planeta.")
                        .departureDate(LocalDate.of(2026, 10, 15)).returnDate(LocalDate.of(2026, 10, 23))
                        .hotel(hotels.get(6)).bus(buses.get(6)).boardType(BoardType.FULL_BOARD)
                        .priceAdult(new BigDecimal("2990.00")).priceChild(new BigDecimal("1890.00")).priceSenior(new BigDecimal("2650.00"))
                        .totalSeats(50).availableSeats(50).isOffer(true).status(TripStatus.AVAILABLE)
                        .imageUrl("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=1200&q=80").build(),

                Trip.builder().destination("Bali").description("La Isla de los Dioses: terrazas de arroz esmeralda, templos hindúes entre la selva y playas de arena blanca. Yoga, spa balinés y la hospitalidad más cálida de Asia.")
                        .departureDate(LocalDate.of(2026, 8, 20)).returnDate(LocalDate.of(2026, 9, 1))
                        .hotel(hotels.get(7)).bus(buses.get(7)).boardType(BoardType.HALF_BOARD)
                        .priceAdult(new BigDecimal("2790.00")).priceChild(new BigDecimal("1750.00")).priceSenior(new BigDecimal("2480.00"))
                        .totalSeats(50).availableSeats(50).isOffer(false).status(TripStatus.AVAILABLE)
                        .imageUrl("https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1200&q=80").build(),

                Trip.builder().destination("Praga").description("La Ciudad Dorada del centro de Europa: el Castillo de Praga, el Puente de Carlos y la Plaza de la Ciudad Vieja. Cerveza artesanal, gastronomía checa y arquitectura barroca incomparable.")
                        .departureDate(LocalDate.of(2026, 6, 25)).returnDate(LocalDate.of(2026, 6, 30))
                        .hotel(hotels.get(8)).bus(buses.get(8)).boardType(BoardType.HALF_BOARD)
                        .priceAdult(new BigDecimal("1190.00")).priceChild(new BigDecimal("790.00")).priceSenior(new BigDecimal("1050.00"))
                        .totalSeats(50).availableSeats(50).isOffer(false).status(TripStatus.AVAILABLE)
                        .imageUrl("https://images.unsplash.com/photo-1592906209472-a36b1f3782ef?auto=format&fit=crop&w=1200&q=80").build(),

                Trip.builder().destination("Maldivas").description("El paraíso terrenal: villas sobre el agua turquesa del Índico, arrecifes de coral con tortugas marinas y tiburones ballena. La luna de miel o el descanso absoluto que mereces.")
                        .departureDate(LocalDate.of(2026, 11, 1)).returnDate(LocalDate.of(2026, 11, 10))
                        .hotel(hotels.get(9)).bus(buses.get(9)).boardType(BoardType.FULL_BOARD)
                        .priceAdult(new BigDecimal("3990.00")).priceChild(new BigDecimal("2490.00")).priceSenior(new BigDecimal("3550.00"))
                        .totalSeats(50).availableSeats(50).isOffer(false).status(TripStatus.AVAILABLE)
                        .imageUrl("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?auto=format&fit=crop&w=1200&q=80").build()
        ));
    }
}
