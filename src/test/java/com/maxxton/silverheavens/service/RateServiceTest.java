package com.maxxton.silverheavens.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.maxxton.silverheavens.entity.Rates;
import com.maxxton.silverheavens.repository.RateRepository;

@ExtendWith(MockitoExtension.class)
class RateServiceTest {

    @Mock
    private RateRepository rateRepository;

    @InjectMocks
    private RateService rateService;

    private Rates rate1;
    private Rates rate2;

    @BeforeEach
    void setUp() {
        rate1 = new Rates();
        rate1.setId(1L);
        rate1.setBungalowId(101L);
        rate1.setStayDateFrom(LocalDate.of(2025, 1, 1));
        rate1.setStayDateTo(LocalDate.of(2025, 1, 10));
        rate1.setValue(5000);
        rate1.setNights(10);

        rate2 = new Rates();
        rate2.setId(2L);
        rate2.setBungalowId(101L);
        rate2.setStayDateFrom(LocalDate.of(2025, 1, 11));
        rate2.setStayDateTo(LocalDate.of(2025, 1, 20));
        rate2.setValue(7000);
        rate2.setNights(10);
    }

    @Test
    void testGetAllRates_returnsAllRates() {
        when(rateRepository.findAll()).thenReturn(List.of(rate1, rate2));

        List<Rates> result = rateService.getAllRates();

        assertEquals(2, result.size());
        assertEquals(101L, result.get(0).getBungalowId());
        verify(rateRepository, times(1)).findAll();
    }

    @Test
    void testGetRateById_found() {
        when(rateRepository.findById(1L)).thenReturn(Optional.of(rate1));

        Rates result = rateService.getRateById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(5000, result.getValue());
        verify(rateRepository, times(1)).findById(1L);
    }

    @Test
    void testGetRateById_notFound_throwsException() {
        when(rateRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rateService.getRateById(99L));

        assertTrue(ex.getMessage().contains("Rate not found"));
        verify(rateRepository, times(1)).findById(99L);
    }

    @Test
    void testGetRatesByBungalowId_returnsSortedRates() {
        when(rateRepository.findByBungalowIdOrderByStayDateFrom(101L))
                .thenReturn(List.of(rate1, rate2));

        List<Rates> result = rateService.getRatesByBungalowId(101L);

        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2025, 1, 1), result.get(0).getStayDateFrom());
        verify(rateRepository, times(1)).findByBungalowIdOrderByStayDateFrom(101L);
    }

    


}