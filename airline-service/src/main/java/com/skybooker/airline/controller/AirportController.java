package com.skybooker.airline.controller;

import com.skybooker.airline.dto.AirportRequest;
import com.skybooker.airline.dto.AirportResponse;
import com.skybooker.airline.service.AirportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/airports")
@Tag(name = "Airports", description = "Airport master data APIs")
public class AirportController {

    private final AirportService airportService;

    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    @Operation(summary = "Create airport")
    @PostMapping
    public ResponseEntity<AirportResponse> createAirport(@Valid @RequestBody AirportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(airportService.createAirport(request));
    }

    @Operation(summary = "Get all airports")
    @GetMapping
    public ResponseEntity<List<AirportResponse>> getAllAirports() {
        return ResponseEntity.ok(airportService.getAllAirports());
    }

    @Operation(summary = "Get airport by id")
    @GetMapping("/{airportId}")
    public ResponseEntity<AirportResponse> getAirportById(@PathVariable UUID airportId) {
        return ResponseEntity.ok(airportService.getAirportById(airportId));
    }

    @Operation(summary = "Get airport by IATA")
    @GetMapping("/iata/{iataCode}")
    public ResponseEntity<AirportResponse> getAirportByIata(@PathVariable String iataCode) {
        return ResponseEntity.ok(airportService.getAirportByIata(iataCode));
    }

    @Operation(summary = "Get airports by city")
    @GetMapping("/city/{city}")
    public ResponseEntity<List<AirportResponse>> getAirportsByCity(@PathVariable String city) {
        return ResponseEntity.ok(airportService.getAirportsByCity(city));
    }

    @Operation(summary = "Get airports by country")
    @GetMapping("/country/{country}")
    public ResponseEntity<List<AirportResponse>> getAirportsByCountry(@PathVariable String country) {
        return ResponseEntity.ok(airportService.getAirportsByCountry(country));
    }

    @Operation(summary = "Search airports")
    @GetMapping("/search")
    public ResponseEntity<List<AirportResponse>> searchAirports(@RequestParam String keyword) {
        return ResponseEntity.ok(airportService.searchAirports(keyword));
    }

    @Operation(summary = "Update airport")
    @PutMapping("/{airportId}")
    public ResponseEntity<AirportResponse> updateAirport(
            @PathVariable UUID airportId,
            @Valid @RequestBody AirportRequest request
    ) {
        return ResponseEntity.ok(airportService.updateAirport(airportId, request));
    }
}
