package com.maxxton.silverheavens.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.maxxton.silverheavens.entity.Rates;

/**
 * Repository for accessing and managing {@link Rates} data.
 * Provides a set of specialized query methods to retrieve pricing records
 * based on booking validity and stay period rules for a specific bungalow.
 */
public interface RateRepository extends JpaRepository<Rates, Long> {

    @Query("""
        SELECT r FROM Rates r
        WHERE r.bungalowId = :bungalowId
        AND r.stayDateTo >= :arrival
        AND r.stayDateFrom <= :departure
        AND (r.bookDateTo IS NULL OR r.bookDateTo >= :bookingDate)
        AND r.bookDateFrom <= :bookingDate
        ORDER BY r.stayDateFrom
    """)
    List<Rates> findRelevantRates(
            @Param("bungalowId") Long bungalowId,
            @Param("arrival") LocalDate arrival,
            @Param("departure") LocalDate departure,
            @Param("bookingDate") LocalDate bookingDate);

    /**
     * Retrieves active rate entries for a bungalow where booking end date is not defined,
     * and the stay date range overlaps with the provided date range.
     *
     * @param bungalowId ID of the bungalow being queried
     * @param newFrom start of the stay date range to check
     * @param newTo end of the stay date range to check
     * @return list of matching {@link Rates} entries
     */
    List<Rates> findByBungalowIdAndBookDateToIsNullAndStayDateToGreaterThanEqualAndStayDateFromLessThanEqual(
        Long bungalowId, LocalDate newFrom, LocalDate newTo);

    /**
     * Retrieves active rate entries for a bungalow that start before or on the given date,
     * ordered by the stay start date.
     *
     * @param bungalowId ID of the bungalow
     * @param max latest allowed stay start date
     * @return ordered list of {@link Rates} entries
     */
    List<Rates> findByBungalowIdAndBookDateToIsNullAndStayDateFromLessThanEqualOrderByStayDateFrom(
        Long bungalowId, LocalDate max);
    
    /**
     * Retrieves all active rate entries (no booking end limit)
     * for a given bungalow sorted by stay start date.
     *
     * @param bungalowId ID of the bungalow
     * @return sorted list of {@link Rates} entries
     */
    List<Rates> findByBungalowIdAndBookDateToIsNullOrderByStayDateFrom(Long bungalowId);
    
    /**
     * Retrieves all rate entries for a given bungalow sorted by stay start date,
     * regardless of booking validity.
     *
     * @param bungalowId ID of the bungalow
     * @return sorted list of {@link Rates} entries
     */
    List<Rates> findByBungalowIdOrderByStayDateFrom(Long bungalowId);

    
}
