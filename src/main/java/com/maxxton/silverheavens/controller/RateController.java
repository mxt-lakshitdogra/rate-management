package com.maxxton.silverheavens.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.maxxton.silverheavens.entity.Rates;
import com.maxxton.silverheavens.service.RateService;

/**
 * Controller handling REST endpoints for managing rate configurations
 * for bungalows including CRUD operations, imports, exports, and price calculations.
 */
@RestController
@RequestMapping("/rates")
public class RateController {
    
    @Autowired
    private RateService rateService;

    /**
     * Creates and stores a new rate configuration.
     *
     * @param rate rate details including date ranges and value
     * @return newly created rate record
     */
    @PostMapping
    public Rates createRate(Rates rate){
        return rateService.createRate(rate);
    }

    /**
     * Retrieves all rate records in the system.
     *
     * @return list of all rates
     */
    @GetMapping
    public List<Rates> getAllRates() {
        return rateService.getAllRates();
    }

    /**
     * Fetches rates configured for a specific bungalow, ordered by stay start date.
     *
     * @param bungId identifier of the bungalow
     * @return list of valid rate definitions for that bungalow
     */
    @GetMapping("/get-bungalow-rates")
    public List<Rates> getRatesByBungalowId(@RequestParam(name = "bungalowId") long bungId) {
        return rateService.getRatesByBungalowId(bungId);
    }

    /**
     * Retrieves a specific rate record based on ID.
     *
     * @param id identifier for the desired rate
     * @return matching rate record if found
     */
    @GetMapping("/get-rate")
    public Rates getRateById(@RequestParam(name = "rateId") long id) {
        return rateService.getRateById(id);
    }

    /**
     * Updates an existing rate definition by soft-closing the current
     * and creating a new updated version.
     *
     * @param id identifier of the existing rate
     * @param updatedRate modified rate payload
     * @return latest active version after update logic runs
     */
    @PutMapping
    public Rates updateRate(@RequestParam(name = "rateId") Long id, Rates updatedRate){
        return rateService.updateRate(id, updatedRate);
    }

    /**
     * Soft-closes an active rate by updating its booking validity period.
     *
     * @param rateId identifier of the rate to close
     * @param dated last available booking date
     */
    @DeleteMapping
    public void closeRate(@RequestParam(name = "rateId") Long rateId, @RequestParam(name = "Date") LocalDate dated){
        rateService.closeRate(rateId, dated);
    }

    /**
     * Permanently deletes a rate record.
     *
     * @param rateId identifier of rate to delete
     */
    @DeleteMapping("/delete")
    public void deleteRate(@RequestParam(name = "rateId") Long rateId){
        rateService.deleteRate(rateId);
    }
    

    /**
     * Exports all stored rates to an Excel file for download.
     *
     * @return downloadable XLSX resource
     * @throws IOException failure in writing Excel content
     */
    @GetMapping("/export")
    public ResponseEntity<Resource> exportRates() throws IOException {
        ByteArrayInputStream stream = rateService.exportRatesToExcel();
        InputStreamResource resource = new InputStreamResource(stream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rates.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(resource);
    }

    /**
     * Imports rates from an uploaded Excel sheet.
     *
     * @param file XLSX file containing rate data
     * @return success message upon completion
     * @throws IOException parsing errors or invalid file format
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importRates(@RequestParam("file") MultipartFile file) throws IOException {
        rateService.importRatesFromExcel(file);
        return ResponseEntity.ok("Rates uploaded successfully!");
    }

    /**
     * Calculates the total price for a stay by evaluating nightly rates
     * based on stay and booking dates.
     *
     * @param bungalowId ID of the bungalow
     * @param arrivalDate date stay begins (inclusive)
     * @param departureDate date stay ends (exclusive)
     * @param bookingDate date booking is made for rate validity
     * @return computed price for the requested stay period
     */
    @GetMapping("/calculatePrice")
    public double calculatePrice(
            @RequestParam(name = "bungId") Long bungalowId,
            @RequestParam(name = "arrrivalDate") LocalDate arrivalDate,
            @RequestParam(name = "departureDate") LocalDate departureDate,
            @RequestParam(name = "bookingDate") LocalDate bookingDate) {

        return rateService.calculatePrice(bungalowId, arrivalDate, departureDate, bookingDate);
    }

}
