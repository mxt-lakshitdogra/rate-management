package com.maxxton.silverheavens.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.maxxton.silverheavens.entity.Rates;
import com.maxxton.silverheavens.repository.RateRepository;

@ExtendWith(MockitoExtension.class)
class RateServicePriceCalculatorTest {

    @Mock
    private RateRepository ratesRepository;

    @InjectMocks
    private RateService rateService;

    private Rates buildRate(Long bungalowId, LocalDate stayFrom, LocalDate stayTo,
                            LocalDate bookFrom, LocalDate bookTo, double value, int nights) {
        Rates rate = new Rates();
        rate.setBungalowId(bungalowId);
        rate.setStayDateFrom(stayFrom);
        rate.setStayDateTo(stayTo);
        rate.setBookDateFrom(bookFrom);
        rate.setBookDateTo(bookTo);
        rate.setValue(value);
        rate.setNights(nights);
        return rate;
    }

    @Test
    void testCalculatePrice_UsesActiveRate() {
        Rates active = buildRate(1L,
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 10),
                LocalDate.of(2025, 1, 1),
                null,
                10000, 10);

        when(ratesRepository.findByBungalowIdOrderByStayDateFrom(1L))
                .thenReturn(List.of(active));

        double result = rateService.calculatePrice(
                1L,
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 6),
                LocalDate.of(2025, 2, 1)
        );

        assertEquals(5000.0, result, 0.01);
    }

    @Test
    void testCalculatePrice_UsesClosedRate() {
        Rates closed = buildRate(1L,
                LocalDate.of(2025, 2, 1),
                LocalDate.of(2025, 2, 28),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                28000, 28);

        when(ratesRepository.findByBungalowIdOrderByStayDateFrom(1L))
                .thenReturn(List.of(closed));

        double result = rateService.calculatePrice(
                1L,
                LocalDate.of(2025, 2, 10),
                LocalDate.of(2025, 2, 13),
                LocalDate.of(2025, 1, 20)
        );

        assertEquals(3000.0, result, 0.01);
    }

    @Test
    void testCalculatePrice_UsesClosedThenActiveRates() {
        Rates closed = buildRate(1L,
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 10),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 2, 1),
                10000, 10);

        Rates active = buildRate(1L,
                LocalDate.of(2025, 3, 11),
                LocalDate.of(2025, 3, 20),
                LocalDate.of(2025, 2, 2),
                null,
                20000, 10);

        when(ratesRepository.findByBungalowIdOrderByStayDateFrom(1L))
                .thenReturn(List.of(closed, active));

        double result = rateService.calculatePrice(
                1L,
                LocalDate.of(2025, 3, 9),
                LocalDate.of(2025, 3, 13),
                LocalDate.of(2025, 2, 5)
        );

        // (2 nights @ 1000) + (2 nights @ 2000)
        assertEquals(6000.0, result, 0.01);
    }

    @Test
    void testCalculatePrice_InvalidDateRangeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                rateService.calculatePrice(
                        1L,
                        LocalDate.of(2025, 3, 5),
                        LocalDate.of(2025, 3, 5),
                        LocalDate.of(2025, 2, 1)
                ));
    }

    @Test
    void testCalculatePrice_NoRateFoundThrows() {
        when(ratesRepository.findByBungalowIdOrderByStayDateFrom(1L))
                .thenReturn(Collections.emptyList());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                rateService.calculatePrice(
                        1L,
                        LocalDate.of(2025, 3, 1),
                        LocalDate.of(2025, 3, 5),
                        LocalDate.of(2025, 2, 1)
                ));

        assertTrue(ex.getMessage().contains("No rate found"));
    }
}
