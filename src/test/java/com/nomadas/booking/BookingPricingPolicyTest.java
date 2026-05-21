package com.nomadas.booking;

import com.nomadas.booking.model.GroupType;
import com.nomadas.booking.service.pricing.AgeBracket;
import com.nomadas.booking.service.pricing.BookingPricingPolicy;
import com.nomadas.booking.service.pricing.BookingPricingPolicy.PricingResult;
import com.nomadas.trip.model.BoardType;
import com.nomadas.trip.model.Trip;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingPricingPolicyTest {

    private final BookingPricingPolicy policy = new BookingPricingPolicy();

    @Test
    void bracketFor_returnsChild_whenUnderEighteen() {
        LocalDate birth = LocalDate.now().minusYears(10);
        assertThat(policy.bracketFor(birth)).isEqualTo(AgeBracket.CHILD);
    }

    @Test
    void bracketFor_returnsAdult_whenEighteenToSixtyFour() {
        LocalDate birth = LocalDate.now().minusYears(30);
        assertThat(policy.bracketFor(birth)).isEqualTo(AgeBracket.ADULT);
    }

    @Test
    void bracketFor_returnsSenior_whenSixtyFiveOrMore() {
        LocalDate birth = LocalDate.now().minusYears(70);
        assertThat(policy.bracketFor(birth)).isEqualTo(AgeBracket.SENIOR);
    }

    @Test
    void compute_addsPricesByBracket_withoutDiscount_whenGroupIsNone() {
        Trip trip = trip("50.00", "100.00", "70.00");
        List<LocalDate> birthDates = List.of(
                LocalDate.now().minusYears(10),
                LocalDate.now().minusYears(30),
                LocalDate.now().minusYears(70)
        );

        PricingResult result = policy.compute(trip, birthDates, GroupType.NONE);

        assertThat(result.totalPrice()).isEqualByComparingTo("220.00");
        assertThat(result.groupDiscount()).isEqualByComparingTo("0.00");
    }

    @Test
    void compute_appliesTwentyPercentDiscount_whenGroupIsImserso() {
        Trip trip = trip("50.00", "100.00", "70.00");
        List<LocalDate> birthDates = List.of(
                LocalDate.now().minusYears(70),
                LocalDate.now().minusYears(68)
        );

        PricingResult result = policy.compute(trip, birthDates, GroupType.IMSERSO);

        assertThat(result.totalPrice()).isEqualByComparingTo("112.00");
        assertThat(result.groupDiscount()).isEqualByComparingTo("28.00");
    }

    @Test
    void compute_appliesFifteenPercentDiscount_whenGroupIsSchool() {
        Trip trip = trip("50.00", "100.00", "70.00");
        List<LocalDate> birthDates = List.of(
                LocalDate.now().minusYears(10),
                LocalDate.now().minusYears(12),
                LocalDate.now().minusYears(30)
        );

        PricingResult result = policy.compute(trip, birthDates, GroupType.SCHOOL);

        assertThat(result.totalPrice()).isEqualByComparingTo("170.00");
        assertThat(result.groupDiscount()).isEqualByComparingTo("30.00");
    }

    private Trip trip(String child, String adult, String senior) {
        return Trip.builder()
                .priceChild(new BigDecimal(child))
                .priceAdult(new BigDecimal(adult))
                .priceSenior(new BigDecimal(senior))
                .boardType(BoardType.FULL_BOARD)
                .build();
    }
}
