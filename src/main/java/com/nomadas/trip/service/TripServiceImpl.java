package com.nomadas.trip.service;

import com.nomadas.bus.model.Bus;
import com.nomadas.bus.repository.BusRepository;
import com.nomadas.exception.ConflictException;
import com.nomadas.exception.ResourceNotFoundException;
import com.nomadas.hotel.model.Hotel;
import com.nomadas.hotel.repository.HotelRepository;
import com.nomadas.trip.dto.TripCreateRequest;
import com.nomadas.trip.dto.TripResponse;
import com.nomadas.trip.dto.TripUpdateRequest;
import com.nomadas.trip.model.Trip;
import com.nomadas.trip.model.TripStatus;
import com.nomadas.trip.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final HotelRepository hotelRepository;
    private final BusRepository busRepository;

    public TripServiceImpl(
            TripRepository tripRepository,
            HotelRepository hotelRepository,
            BusRepository busRepository
    ) {
        this.tripRepository = tripRepository;
        this.hotelRepository = hotelRepository;
        this.busRepository = busRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getAll() {
        return tripRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getOffers() {
        return tripRepository.findByIsOfferTrue().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getById(Long id) {
        return toResponse(loadTrip(id));
    }

    @Override
    public TripResponse create(TripCreateRequest request) {
        validateDateRange(request.departureDate(), request.returnDate());
        validateSeatCapacity(request.availableSeats(), request.totalSeats());
        Hotel hotel = loadHotel(request.hotelId());
        Bus bus = loadBus(request.busId());
        validateBusCapacity(bus, request.totalSeats());

        Trip trip = Trip.builder()
                .destination(request.destination())
                .description(request.description())
                .departureDate(request.departureDate())
                .returnDate(request.returnDate())
                .hotel(hotel)
                .bus(bus)
                .boardType(request.boardType())
                .priceAdult(request.priceAdult())
                .priceChild(request.priceChild())
                .priceSenior(request.priceSenior())
                .totalSeats(request.totalSeats())
                .availableSeats(request.availableSeats())
                .isOffer(request.isOffer())
                .status(resolveStatus(request.departureDate(), request.availableSeats()))
                .imageUrl(request.imageUrl())
                .build();

        return toResponse(tripRepository.save(trip));
    }

    @Override
    public TripResponse update(Long id, TripUpdateRequest request) {
        Trip trip = loadTrip(id);
        validateDateRange(request.departureDate(), request.returnDate());
        validateSeatCapacity(request.availableSeats(), request.totalSeats());
        Hotel hotel = loadHotel(request.hotelId());
        Bus bus = loadBus(request.busId());
        validateBusCapacity(bus, request.totalSeats());

        trip.setDestination(request.destination());
        trip.setDescription(request.description());
        trip.setDepartureDate(request.departureDate());
        trip.setReturnDate(request.returnDate());
        trip.setHotel(hotel);
        trip.setBus(bus);
        trip.setBoardType(request.boardType());
        trip.setPriceAdult(request.priceAdult());
        trip.setPriceChild(request.priceChild());
        trip.setPriceSenior(request.priceSenior());
        trip.setTotalSeats(request.totalSeats());
        trip.setAvailableSeats(request.availableSeats());
        trip.setIsOffer(request.isOffer());
        trip.setStatus(request.status());
        trip.setImageUrl(request.imageUrl());

        return toResponse(tripRepository.save(trip));
    }

    @Override
    public void delete(Long id) {
        if (!tripRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trip not found");
        }
        tripRepository.deleteById(id);
    }

    private Trip loadTrip(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
    }

    private Hotel loadHotel(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
    }

    private Bus loadBus(Long id) {
        return busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));
    }

    private void validateDateRange(LocalDate departure, LocalDate returnDate) {
        if (returnDate.isBefore(departure)) {
            throw new ConflictException("Return date cannot be before departure date");
        }
    }

    private void validateSeatCapacity(Integer available, Integer total) {
        if (available > total) {
            throw new ConflictException("Available seats cannot exceed total seats");
        }
    }

    private void validateBusCapacity(Bus bus, Integer totalSeats) {
        if (totalSeats > bus.getTotalSeats()) {
            throw new ConflictException("Trip seats exceed bus capacity");
        }
    }

    private TripStatus resolveStatus(LocalDate departureDate, Integer availableSeats) {
        if (departureDate.isBefore(LocalDate.now())) {
            return TripStatus.PAST;
        }
        if (availableSeats == 0) {
            return TripStatus.FULL;
        }
        return TripStatus.AVAILABLE;
    }

    private TripResponse toResponse(Trip trip) {
        Hotel hotel = trip.getHotel();
        Bus bus = trip.getBus();
        return new TripResponse(
                trip.getId(),
                trip.getDestination(),
                trip.getDescription(),
                trip.getDepartureDate(),
                trip.getReturnDate(),
                hotel.getId(),
                hotel.getName(),
                bus.getId(),
                bus.getPlateNumber(),
                trip.getBoardType(),
                trip.getPriceAdult(),
                trip.getPriceChild(),
                trip.getPriceSenior(),
                trip.getTotalSeats(),
                trip.getAvailableSeats(),
                trip.getIsOffer(),
                trip.getStatus(),
                trip.getImageUrl()
        );
    }
}
