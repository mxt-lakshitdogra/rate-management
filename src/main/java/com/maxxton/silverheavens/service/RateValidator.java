package com.maxxton.silverheavens.service;

import com.maxxton.silverheavens.entity.Rates;
import java.time.LocalDate;
import java.util.List;

public class RateValidator {

    public static void validateNewRate(Rates newRate, List<Rates> existingRates) {

        // 1. Basic sanity
        if (newRate.getStayDateFrom() == null || newRate.getStayDateTo() == null) {
            throw new IllegalArgumentException("Stay date range cannot be null");
        }

        if (newRate.getStayDateFrom().isAfter(newRate.getStayDateTo())) {
            throw new IllegalArgumentException("Stay start date cannot be after end date");
        }

        if (newRate.getValue() <= 0) {
            throw new IllegalArgumentException("Rate value must be positive");
        }

        if (newRate.getNights() <= 0) {
            throw new IllegalArgumentException("Number of nights must be positive");
        }

        if (newRate.getBungalowId() == null) {
            throw new IllegalArgumentException("Bungalow ID is required");
        }

        // 2. Booking date sanity
        LocalDate bookFrom = newRate.getBookDateFrom();
        LocalDate bookTo = newRate.getBookDateTo();

        if (bookFrom != null && bookTo != null && bookFrom.isAfter(bookTo)) {
            throw new IllegalArgumentException("Booking start date cannot be after booking end date");
        }

        // 3. Contained duplicate check
        for (Rates existing : existingRates) {
            boolean fullyInside =
                    !newRate.getStayDateFrom().isBefore(existing.getStayDateFrom())
                    && !newRate.getStayDateTo().isAfter(existing.getStayDateTo());

            boolean sameValue = Double.compare(newRate.getValue(), existing.getValue()) == 0;

            if (fullyInside && sameValue && (existing.getBookDateTo() == null || existing.getBookDateTo().isAfter(LocalDate.now()))) {
                throw new IllegalArgumentException("Identical rate already exists within this period.");
            }
        }
    }
}
