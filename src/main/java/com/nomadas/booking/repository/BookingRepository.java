package com.nomadas.booking.repository;

import com.nomadas.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByTripId(Long tripId);

    @Query("select count(b) from Booking b where b.trip.departureDate between :start and :end")
    long countByDepartureDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("select coalesce(sum(b.totalPrice), 0) from Booking b where b.trip.departureDate between :start and :end")
    BigDecimal sumRevenueByDepartureDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("""
            select b.trip.id as tripId, b.trip.destination as destination, sum(b.totalPrice) as revenue
            from Booking b
            where b.trip.departureDate between :start and :end
            group by b.trip.id, b.trip.destination
            order by sum(b.totalPrice) desc
            """)
    List<TripRevenueProjection> findTopTripRevenuesBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    interface TripRevenueProjection {
        Long getTripId();

        String getDestination();

        BigDecimal getRevenue();
    }
}
