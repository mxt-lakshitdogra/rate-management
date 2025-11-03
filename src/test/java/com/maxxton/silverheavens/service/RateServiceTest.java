package com.maxxton.silverheavens.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.maxxton.silverheavens.entity.Rates;
import com.maxxton.silverheavens.repository.RateRepository;

@DataJpaTest
@Import(RateService.class)
class RateServiceTest {

    @Autowired
    private RateRepository rateRepository;

    @Autowired
    private RateService rateService;

    @BeforeEach
    void setup() {
        rateRepository.deleteAll(); // clean up before each test
    }

    @Test
    void createRate_shouldInsertNewRate() {
        Rates rate = new Rates();
        rate.setBungalowId(100L);
        rate.setStayDateFrom(LocalDate.of(2025, 1, 1));
        rate.setStayDateTo(LocalDate.of(2025, 12, 31));
        rate.setNights(1);
        rate.setValue(3000);
        rate.setBookDateFrom(LocalDate.of(2024, 12, 16));

        Rates saved = rateService.createRate(rate);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBungalowId()).isEqualTo(100L);
        assertThat(rateRepository.count()).isEqualTo(1);
    }

    @Test
    void createRate_shouldSplitWhenOverlapping() {
        // Existing rate
        Rates janToDec = new Rates();
        janToDec.setBungalowId(100L);
        janToDec.setStayDateFrom(LocalDate.of(2025, 1, 1));
        janToDec.setStayDateTo(LocalDate.of(2025, 12, 31));
        janToDec.setNights(1);
        janToDec.setValue(3000);
        janToDec.setBookDateFrom(LocalDate.of(2024, 12, 15));
        rateRepository.save(janToDec);

        // New overlapping rate (March)
        Rates march = new Rates();
        march.setBungalowId(100L);
        march.setStayDateFrom(LocalDate.of(2025, 3, 1));
        march.setStayDateTo(LocalDate.of(2025, 3, 31));
        march.setNights(1);
        march.setValue(4000);
        march.setBookDateFrom(LocalDate.of(2025, 1, 1));

        rateService.createRate(march);

        List<Rates> all = rateRepository.findAll();
        assertThat(all.size()).isGreaterThan(1);
    }

}
