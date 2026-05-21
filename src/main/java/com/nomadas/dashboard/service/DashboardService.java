package com.nomadas.dashboard.service;

import com.nomadas.dashboard.dto.CurrentYearRevenueResponse;
import com.nomadas.dashboard.dto.TopTripResponse;
import com.nomadas.dashboard.dto.TripsByYearResponse;

import java.util.List;

public interface DashboardService {
    TripsByYearResponse getTripsByYear(int year);

    CurrentYearRevenueResponse getCurrentYearRevenue();

    List<TopTripResponse> getTopTrips(int year);
}
