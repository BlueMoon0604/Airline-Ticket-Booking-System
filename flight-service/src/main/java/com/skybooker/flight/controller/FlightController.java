package com.skybooker.flight.controller;

import com.skybooker.flight.dto.ApiResponse;
import com.skybooker.flight.dto.FlightRequest;
import com.skybooker.flight.dto.FlightResponse;
import com.skybooker.flight.dto.FlightSearchRequest;
import com.skybooker.flight.dto.RoundTripSearchRequest;
import com.skybooker.flight.dto.RoundTripSearchResponse;
import com.skybooker.flight.dto.SeatCountUpdateRequest;
import com.skybooker.flight.dto.UpdateFlightStatusRequest;
import com.skybooker.flight.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/flights")
@Tag(name = "Flights", description = "Flight management and search APIs")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @Operation(summary = "Add flight")
    @PostMapping
    public ResponseEntity<FlightResponse> addFlight(@Valid @RequestBody FlightRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.addFlight(request));
    }

    @Operation(summary = "Get all flights")
    @GetMapping
    public ResponseEntity<List<FlightResponse>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllFlights());
    }

    @Operation(summary = "Get flight by id")
    @GetMapping("/{flightId}")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable UUID flightId) {
        return ResponseEntity.ok(flightService.getFlightById(flightId));
    }

    @Operation(summary = "Get flight by number")
    @GetMapping("/number/{flightNumber}")
    public ResponseEntity<FlightResponse> getFlightByNumber(@PathVariable String flightNumber) {
        return ResponseEntity.ok(flightService.getFlightByNumber(flightNumber));
    }

    @Operation(summary = "Get flights by airline")
    @GetMapping("/airline/{airlineId}")
    public ResponseEntity<List<FlightResponse>> getFlightsByAirline(@PathVariable UUID airlineId) {
        return ResponseEntity.ok(flightService.getFlightsByAirline(airlineId));
    }

    @Operation(summary = "Search one-way flights")
    @PostMapping("/search")
    public ResponseEntity<List<FlightResponse>> searchFlights(@Valid @RequestBody FlightSearchRequest request) {
        return ResponseEntity.ok(flightService.searchFlights(request));
    }

    @Operation(summary = "Search round-trip flights")
    @PostMapping("/search/round-trip")
    public ResponseEntity<RoundTripSearchResponse> searchRoundTrip(
            @Valid @RequestBody RoundTripSearchRequest request
    ) {
        return ResponseEntity.ok(flightService.searchRoundTrip(request));
    }

    @Operation(summary = "Update flight")
    @PutMapping("/{flightId}")
    public ResponseEntity<FlightResponse> updateFlight(
            @PathVariable UUID flightId,
            @Valid @RequestBody FlightRequest request
    ) {
        return ResponseEntity.ok(flightService.updateFlight(flightId, request));
    }

    @Operation(summary = "Update flight status")
    @PutMapping("/{flightId}/status")
    public ResponseEntity<FlightResponse> updateStatus(
            @PathVariable UUID flightId,
            @Valid @RequestBody UpdateFlightStatusRequest request
    ) {
        return ResponseEntity.ok(flightService.updateStatus(flightId, request));
    }

    @Operation(summary = "Decrement available seats")
    @PutMapping("/{flightId}/decrement-seats")
    public ResponseEntity<ApiResponse> decrementSeats(
            @PathVariable UUID flightId,
            @Valid @RequestBody SeatCountUpdateRequest request
    ) {
        return ResponseEntity.ok(flightService.decrementSeats(flightId, request));
    }

    @Operation(summary = "Increment available seats")
    @PutMapping("/{flightId}/increment-seats")
    public ResponseEntity<ApiResponse> incrementSeats(
            @PathVariable UUID flightId,
            @Valid @RequestBody SeatCountUpdateRequest request
    ) {
        return ResponseEntity.ok(flightService.incrementSeats(flightId, request));
    }

    @Operation(summary = "Delete flight")
    @DeleteMapping("/{flightId}")
    public ResponseEntity<ApiResponse> deleteFlight(@PathVariable UUID flightId) {
        return ResponseEntity.ok(flightService.deleteFlight(flightId));
    }
}
