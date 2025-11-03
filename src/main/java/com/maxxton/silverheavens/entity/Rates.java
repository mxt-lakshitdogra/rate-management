package com.maxxton.silverheavens.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the pricing configuration for a bungalow stay within a specific
 * date range. This entity stores both the applicable stay duration along with
 * the booking window in which this rate is valid.
 * <p>
 * Each record defines:
 * <ul>
 *     <li>The bungalow associated with the rate</li>
 *     <li>The eligible stay dates</li>
 *     <li>The booking period when the rate can be applied</li>
 *     <li>The number of nights and the total value for that period</li>
 * </ul>
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rates {

    /**
     * Unique identifier for each rate entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifier of the bungalow to which this rate applies.
     */
    @Column(name = "bungalow_id", nullable = false)
    private Long bungalowId;

    /**
     * Start date of the eligible stay period.
     */
    @Column(name = "stay_date_from", nullable = false)
    private LocalDate stayDateFrom;

    /**
     * End date of the eligible stay period.
     */
    @Column(name = "stay_date_to", nullable = false)
    private LocalDate stayDateTo;

    /**
     * Number of nights included in this rate period.
     * Usually derived from the stay date range.
     */
    @Column(nullable = false)
    private Integer nights;

    /**
     * The price applicable for this stay period, covering the defined number of nights.
     */
    @Column(name = "`value`",nullable = false)
    private double value;

    /**
     * The first date from which customers are allowed to book this rate.
     */
    @Column(name = "book_date_from", nullable = false)
    private LocalDate bookDateFrom;

    /**
     * The last date until which customers can book this rate.
     * This field may be null, indicating an open-ended booking window.
     */
    @Column(name = "book_date_to")
    private LocalDate bookDateTo;
}
