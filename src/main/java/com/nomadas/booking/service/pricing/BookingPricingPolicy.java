package com.nomadas.booking.service.pricing;

import com.nomadas.booking.model.GroupType;
import com.nomadas.trip.model.Trip;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Component
public class BookingPricingPolicy {

    private static final int CHILD_MAX_AGE = 18;
    private static final int SENIOR_MIN_AGE = 65;
    private static final BigDecimal IMSERSO_DISCOUNT_RATE = new BigDecimal("0.20");
    private static final BigDecimal SCHOOL_DISCOUNT_RATE = new BigDecimal("0.15");

    public AgeBracket bracketFor(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < CHILD_MAX_AGE) {
            return AgeBracket.CHILD;
        }
        if (age >= SENIOR_MIN_AGE) {
            return AgeBracket.SENIOR;
        }
        return AgeBracket.ADULT;
    }

    public PricingResult compute(Trip trip, List<LocalDate> passengerBirthDates, GroupType groupType) {
        BigDecimal grossTotal = passengerBirthDates.stream()
                .map(birth -> priceFor(trip, bracketFor(birth)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountRate = discountRateFor(groupType);
        BigDecimal discountAmount = grossTotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netTotal = grossTotal.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        return new PricingResult(netTotal, discountAmount);
    }

    private BigDecimal priceFor(Trip trip, AgeBracket bracket) {
        return switch (bracket) {
            case CHILD -> trip.getPriceChild();
            case ADULT -> trip.getPriceAdult();
            case SENIOR -> trip.getPriceSenior();
        };
    }

    private BigDecimal discountRateFor(GroupType groupType) {
        return switch (groupType) {
            case IMSERSO -> IMSERSO_DISCOUNT_RATE;
            case SCHOOL -> SCHOOL_DISCOUNT_RATE;
            case NONE -> BigDecimal.ZERO;
        };
    }

    public record PricingResult(BigDecimal totalPrice, BigDecimal groupDiscount) {
    }
}
