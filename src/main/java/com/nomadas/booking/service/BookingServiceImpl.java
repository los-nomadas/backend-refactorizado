package com.nomadas.booking.service;

import com.nomadas.booking.dto.BookingCreateRequest;
import com.nomadas.booking.dto.BookingResponse;
import com.nomadas.booking.dto.CompanionRequest;
import com.nomadas.booking.dto.CompanionResponse;
import com.nomadas.booking.model.Booking;
import com.nomadas.booking.model.Companion;
import com.nomadas.booking.repository.BookingRepository;
import com.nomadas.booking.service.notification.BookingNotificationService;
import com.nomadas.booking.service.pricing.AgeBracket;
import com.nomadas.booking.service.pricing.BookingPricingPolicy;
import com.nomadas.booking.service.pricing.BookingPricingPolicy.PricingResult;
import com.nomadas.auth.repository.InternalCredentialRepository;
import com.nomadas.entity.InternalCredential;
import com.nomadas.exception.ConflictException;
import com.nomadas.exception.ResourceNotFoundException;
import com.nomadas.hotel.model.Hotel;
import com.nomadas.trip.model.Trip;
import com.nomadas.trip.model.TripStatus;
import com.nomadas.trip.repository.TripRepository;
import com.nomadas.user.model.User;
import com.nomadas.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final InternalCredentialRepository credentialRepository;
    private final BookingPricingPolicy pricingPolicy;
    private final BookingNotificationService notificationService;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            TripRepository tripRepository,
            InternalCredentialRepository credentialRepository,
            BookingPricingPolicy pricingPolicy,
            BookingNotificationService notificationService
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.credentialRepository = credentialRepository;
        this.pricingPolicy = pricingPolicy;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAll() {
        return bookingRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        return toResponse(loadBooking(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(Long credentialId) {
        InternalCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found"));
        User user = credential.getUser();
        if (user == null) {
            throw new ResourceNotFoundException("No customer linked to this account");
        }
        return bookingRepository.findByUserId(user.getId()).stream().map(this::toResponse).toList();
    }

    @Override
    public BookingResponse create(BookingCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
        Hotel hotel = trip.getHotel();

        validateTripIsBookable(trip);
        validateSeatsAvailable(trip, request.companions().size());
        validateBusCapacity(trip);
        validateHotelCapacity(hotel);
        validateMinorHasAdult(request.companions());

        PricingResult pricing = pricingPolicy.compute(
                trip,
                request.companions().stream().map(CompanionRequest::birthDate).toList(),
                request.groupType()
        );

        Booking booking = Booking.builder()
                .user(user)
                .trip(trip)
                .boardType(request.boardType())
                .groupType(request.groupType())
                .totalPrice(pricing.totalPrice())
                .groupDiscount(pricing.groupDiscount())
                .companions(new ArrayList<>())
                .build();

        for (CompanionRequest companionRequest : request.companions()) {
            booking.getCompanions().add(Companion.builder()
                    .firstName(companionRequest.firstName())
                    .lastName(companionRequest.lastName())
                    .birthDate(companionRequest.birthDate())
                    .booking(booking)
                    .build());
        }

        trip.setAvailableSeats(trip.getAvailableSeats() - request.companions().size());
        if (trip.getAvailableSeats() == 0) {
            trip.setStatus(TripStatus.FULL);
        }
        hotel.setAvailablePlaces(hotel.getAvailablePlaces() - request.companions().size());

        Booking saved = bookingRepository.save(booking);
        notificationService.sendBookingConfirmation(saved);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Booking booking = loadBooking(id);
        Trip trip = booking.getTrip();
        Hotel hotel = trip.getHotel();
        int seats = booking.getCompanions().size();
        trip.setAvailableSeats(trip.getAvailableSeats() + seats);
        if (trip.getStatus() == TripStatus.FULL && trip.getAvailableSeats() > 0) {
            trip.setStatus(TripStatus.AVAILABLE);
        }
        hotel.setAvailablePlaces(hotel.getAvailablePlaces() + seats);
        bookingRepository.delete(booking);
    }

    private Booking loadBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private void validateTripIsBookable(Trip trip) {
        if (trip.getStatus() == TripStatus.CANCELLED) {
            throw new ConflictException("The trip has been cancelled");
        }
        if (trip.getDepartureDate().isBefore(LocalDate.now())) {
            throw new ConflictException("Cannot book a trip that has already departed");
        }
    }

    private void validateSeatsAvailable(Trip trip, int requestedSeats) {
        if (trip.getAvailableSeats() < requestedSeats) {
            throw new ConflictException("Not enough seats available on this trip");
        }
    }

    private void validateBusCapacity(Trip trip) {
        if (trip.getBus().getAvailableSeats() <= 0) {
            throw new ConflictException("The bus is full and cannot be booked");
        }
    }

    private void validateHotelCapacity(Hotel hotel) {
        if (hotel.getAvailablePlaces() <= 0) {
            throw new ConflictException("The hotel is full and cannot be booked");
        }
    }

    private void validateMinorHasAdult(List<CompanionRequest> companions) {
        boolean anyMinor = companions.stream()
                .map(CompanionRequest::birthDate)
                .map(pricingPolicy::bracketFor)
                .anyMatch(b -> b == AgeBracket.CHILD);
        if (!anyMinor) {
            return;
        }
        boolean anyAdult = companions.stream()
                .map(CompanionRequest::birthDate)
                .map(pricingPolicy::bracketFor)
                .anyMatch(b -> b == AgeBracket.ADULT || b == AgeBracket.SENIOR);
        if (!anyAdult) {
            throw new ConflictException("A minor cannot travel without an adult");
        }
    }

    private BookingResponse toResponse(Booking booking) {
        User user = booking.getUser();
        Trip trip = booking.getTrip();
        List<CompanionResponse> companions = booking.getCompanions().stream()
                .map(c -> new CompanionResponse(c.getId(), c.getFirstName(), c.getLastName(), c.getBirthDate()))
                .toList();
        return new BookingResponse(
                booking.getId(),
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                trip.getId(),
                trip.getDestination(),
                booking.getBoardType(),
                booking.getGroupType(),
                booking.getTotalPrice(),
                booking.getGroupDiscount(),
                companions,
                booking.getCreatedAt()
        );
    }
}
