package com.nomadas.dashboard.controller;

import com.nomadas.dashboard.dto.CurrentYearRevenueResponse;
import com.nomadas.dashboard.dto.TopTripResponse;
import com.nomadas.dashboard.dto.TripsByYearResponse;
import com.nomadas.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Métricas para la dirección de la agencia")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/trips-by-year")
    public TripsByYearResponse getTripsByYear(@RequestParam int year) {
        return dashboardService.getTripsByYear(year);
    }

    @GetMapping("/current-year-revenue")
    public CurrentYearRevenueResponse getCurrentYearRevenue() {
        return dashboardService.getCurrentYearRevenue();
    }

    @GetMapping("/top-trips")
    public List<TopTripResponse> getTopTrips(@RequestParam int year) {
        return dashboardService.getTopTrips(year);
    }
}
