package com.nomadas.dashboard.service;

import com.nomadas.booking.repository.BookingRepository;
import com.nomadas.dashboard.dto.CurrentYearRevenueResponse;
import com.nomadas.dashboard.dto.TopTripResponse;
import com.nomadas.dashboard.dto.TripsByYearResponse;
import com.nomadas.trip.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final int TOP_TRIPS_LIMIT = 3;

    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;

    public DashboardServiceImpl(TripRepository tripRepository, BookingRepository bookingRepository) {
        this.tripRepository = tripRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public TripsByYearResponse getTripsByYear(int year) {
        LocalDate start = LocalDate.of(year, Month.JANUARY, 1);
        LocalDate end = LocalDate.of(year, Month.DECEMBER, 31);
        long total = tripRepository.findByDepartureDateBetween(start, end).size();
        return new TripsByYearResponse(year, total);
    }

    @Override
    public CurrentYearRevenueResponse getCurrentYearRevenue() {
        int year = LocalDate.now().getYear();
        LocalDate start = LocalDate.of(year, Month.JANUARY, 1);
        LocalDate end = LocalDate.of(year, Month.DECEMBER, 31);
        BigDecimal revenue = bookingRepository.sumRevenueByDepartureDateBetween(start, end);
        return new CurrentYearRevenueResponse(year, revenue == null ? BigDecimal.ZERO : revenue);
    }

    @Override
    public List<TopTripResponse> getTopTrips(int year) {
        LocalDate start = LocalDate.of(year, Month.JANUARY, 1);
        LocalDate end = LocalDate.of(year, Month.DECEMBER, 31);
        return bookingRepository.findTopTripRevenuesBetween(start, end).stream()
                .limit(TOP_TRIPS_LIMIT)
                .map(row -> new TopTripResponse(row.getTripId(), row.getDestination(), row.getRevenue()))
                .toList();
    }
}
