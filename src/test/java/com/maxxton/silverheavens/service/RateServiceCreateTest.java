package com.maxxton.silverheavens.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.maxxton.silverheavens.entity.Rates;
import com.maxxton.silverheavens.repository.RateRepository;

public class RateServiceCreateTest {
    
    @Mock
    private RateRepository ratesRepository;

    @InjectMocks
    private RateService ratesService;

    private Rates sampleRate;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        sampleRate = new Rates();
        sampleRate.setId(1L);
        sampleRate.setBungalowId(10L);
        sampleRate.setStayDateFrom(LocalDate.of(2025, 3, 1));
        sampleRate.setStayDateTo(LocalDate.of(2025, 3, 10));
        sampleRate.setValue(3000);
        sampleRate.setNights(1);
    }

    @Test
    void testCreateRate_Success() {
        // given
        when(ratesRepository.findByBungalowIdOrderByStayDateFrom(10L))
                .thenReturn(Collections.emptyList());

        when(ratesRepository.save(any(Rates.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Rates result = ratesService.createRate(sampleRate);

        // then
        assertNotNull(result);
        assertNull(result.getBookDateTo());
        assertEquals(sampleRate.getBungalowId(), result.getBungalowId());
        verify(ratesRepository, times(1)).findByBungalowIdOrderByStayDateFrom(10L);
        verify(ratesRepository, atLeastOnce()).save(any(Rates.class));
    }

    @Test
    void testCreateRate_NormalizesValue_WhenMultipleNights() {
        sampleRate.setNights(3);
        sampleRate.setValue(9000);

        when(ratesRepository.findByBungalowIdOrderByStayDateFrom(10L))
                .thenReturn(Collections.emptyList());
        when(ratesRepository.save(any(Rates.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Rates result = ratesService.createRate(sampleRate);

        assertEquals(1, result.getNights());
        assertEquals(3000, result.getValue());
    }

    @Test
    void testCreateRate_WithOverlappingRates() {
        Rates existing = new Rates();
        existing.setBungalowId(10L);
        existing.setStayDateFrom(LocalDate.of(2025, 3, 5));
        existing.setStayDateTo(LocalDate.of(2025, 3, 15));
        existing.setBookDateTo(null);
        existing.setValue(3000);
        existing.setNights(1);

        when(ratesRepository.findByBungalowIdOrderByStayDateFrom(10L))
                .thenReturn(List.of(existing));
        when(ratesRepository.save(any(Rates.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Rates result = ratesService.createRate(sampleRate);

        assertNotNull(result);
        verify(ratesRepository, atLeastOnce()).save(any(Rates.class));
    }

    @Test
    void testCreateRate_WhenValidationFails() {
        doThrow(new RuntimeException("Invalid rate"))
                .when(ratesRepository).findByBungalowIdOrderByStayDateFrom(10L);

        assertThrows(RuntimeException.class, () -> ratesService.createRate(sampleRate));
    }

    @Test
    void testMergeAdjacentRates_MergesSuccessfully() {
        // Arrange
        Rates r1 = new Rates();
        r1.setId(1L);
        r1.setBungalowId(10L);
        r1.setStayDateFrom(LocalDate.of(2025, 3, 1));
        r1.setStayDateTo(LocalDate.of(2025, 3, 31));
        r1.setValue(3000);
        r1.setBookDateFrom(LocalDate.of(2025, 1, 1));
        r1.setBookDateTo(null);

        Rates r2 = new Rates();
        r2.setId(2L);
        r2.setBungalowId(10L);
        r2.setStayDateFrom(LocalDate.of(2025, 4, 1));
        r2.setStayDateTo(LocalDate.of(2025, 4, 30));
        r2.setValue(3000);
        r2.setBookDateFrom(LocalDate.of(2025, 2, 1));
        r2.setBookDateTo(null);

        when(ratesRepository.findByBungalowIdAndBookDateToIsNullOrderByStayDateFrom(10L))
                .thenReturn(List.of(r1, r2));

        when(ratesRepository.save(any(Rates.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ratesService.mergeAdjacentRates(10L);

        // Assert
        verify(ratesRepository, times(1)).save(r1);
        verify(ratesRepository, atLeastOnce()).save(any(Rates.class));
        verify(ratesRepository, times(1)).delete(r2);
    }

    @Test
    void testMergeAdjacentRates_DoesNotMergeWhenValuesDiffer() {
        Rates r1 = new Rates();
        r1.setStayDateFrom(LocalDate.of(2025, 3, 1));
        r1.setStayDateTo(LocalDate.of(2025, 3, 31));
        r1.setValue(3000);
        r1.setBungalowId(10L);

        Rates r2 = new Rates();
        r2.setStayDateFrom(LocalDate.of(2025, 4, 1));
        r2.setStayDateTo(LocalDate.of(2025, 4, 30));
        r2.setValue(4000);
        r2.setBungalowId(10L);

        when(ratesRepository.findByBungalowIdAndBookDateToIsNullOrderByStayDateFrom(10L))
                .thenReturn(List.of(r1, r2));

        ratesService.mergeAdjacentRates(10L);

        verify(ratesRepository, never()).delete(r2);
        verify(ratesRepository, never()).save(argThat(rate ->
                rate.getStayDateFrom().equals(LocalDate.of(2025, 3, 1)) &&
                rate.getStayDateTo().equals(LocalDate.of(2025, 4, 30))
        ));
    }

    @Test
    void testMergeAdjacentRates_SingleRateDoesNothing() {
        Rates r1 = new Rates();
        r1.setBungalowId(10L);
        r1.setStayDateFrom(LocalDate.of(2025, 3, 1));
        r1.setStayDateTo(LocalDate.of(2025, 3, 31));

        when(ratesRepository.findByBungalowIdAndBookDateToIsNullOrderByStayDateFrom(10L))
                .thenReturn(List.of(r1));

        ratesService.mergeAdjacentRates(10L);

        verify(ratesRepository, never()).save(any());
    }

    @Test
    void testSplitOverlappingRates_SplitsCorrectly() throws Exception {
        // Arrange
        Rates existing = new Rates();
        existing.setBungalowId(10L);
        existing.setStayDateFrom(LocalDate.of(2025, 3, 1));
        existing.setStayDateTo(LocalDate.of(2025, 3, 31));
        existing.setValue(3000);
        existing.setNights(1);
        existing.setBookDateFrom(LocalDate.of(2025, 1, 1));
        existing.setBookDateTo(null);

        Rates newRate = new Rates();
        newRate.setBungalowId(10L);
        newRate.setStayDateFrom(LocalDate.of(2025, 3, 10));
        newRate.setStayDateTo(LocalDate.of(2025, 3, 20));
        newRate.setBookDateFrom(LocalDate.of(2025, 2, 1));
        newRate.setValue(3500);
        newRate.setNights(1);

        when(ratesRepository.findByBungalowIdAndBookDateToIsNullAndStayDateToGreaterThanEqualAndStayDateFromLessThanEqual(
                anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(existing));

        when(ratesRepository.save(any(Rates.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Access private method using reflection
        var method = RateService.class.getDeclaredMethod("splitOverlappingRates", Rates.class);
        method.setAccessible(true);
        method.invoke(ratesService, newRate);

        // Assert that save() was called multiple times (old, before, after)
        verify(ratesRepository, atLeast(3)).save(any(Rates.class));
    }

    @Test
    void testSplitOverlappingRates_NoOverlap() throws Exception {
        Rates newRate = new Rates();
        newRate.setBungalowId(10L);
        newRate.setStayDateFrom(LocalDate.of(2025, 5, 1));
        newRate.setStayDateTo(LocalDate.of(2025, 5, 10));
        newRate.setBookDateFrom(LocalDate.of(2025, 2, 1));

        when(ratesRepository.findByBungalowIdAndBookDateToIsNullAndStayDateToGreaterThanEqualAndStayDateFromLessThanEqual(
                anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        var method = RateService.class.getDeclaredMethod("splitOverlappingRates", Rates.class);
        method.setAccessible(true);
        method.invoke(ratesService, newRate);

        verify(ratesRepository, never()).save(any());
    }


}
