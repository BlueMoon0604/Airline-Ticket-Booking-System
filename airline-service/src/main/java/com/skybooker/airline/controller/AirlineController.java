package com.skybooker.airline.controller;

import com.skybooker.airline.dto.AirlineRequest;
import com.skybooker.airline.dto.AirlineResponse;
import com.skybooker.airline.dto.ApiResponse;
import com.skybooker.airline.service.AirlineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/airlines")
@Tag(name = "Airlines", description = "Airline master data APIs")
public class AirlineController {

    private final AirlineService airlineService;

    public AirlineController(AirlineService airlineService) {
        this.airlineService = airlineService;
    }

    @Operation(summary = "Create airline")
    @PostMapping
    public ResponseEntity<AirlineResponse> createAirline(@Valid @RequestBody AirlineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(airlineService.createAirline(request));
    }

    @Operation(summary = "Get all airlines")
    @GetMapping
    public ResponseEntity<List<AirlineResponse>> getAllAirlines() {
        return ResponseEntity.ok(airlineService.getAllAirlines());
    }

    @Operation(summary = "Get active airlines")
    @GetMapping("/active")
    public ResponseEntity<List<AirlineResponse>> getActiveAirlines() {
        return ResponseEntity.ok(airlineService.getActiveAirlines());
    }

    @Operation(summary = "Get airline by id")
    @GetMapping("/{airlineId}")
    public ResponseEntity<AirlineResponse> getAirlineById(@PathVariable UUID airlineId) {
        return ResponseEntity.ok(airlineService.getAirlineById(airlineId));
    }

    @Operation(summary = "Get airline by IATA code")
    @GetMapping("/iata/{iataCode}")
    public ResponseEntity<AirlineResponse> getAirlineByIata(@PathVariable String iataCode) {
        return ResponseEntity.ok(airlineService.getAirlineByIata(iataCode));
    }

    @Operation(summary = "Update airline")
    @PutMapping("/{airlineId}")
    public ResponseEntity<AirlineResponse> updateAirline(
            @PathVariable UUID airlineId,
            @Valid @RequestBody AirlineRequest request
    ) {
        return ResponseEntity.ok(airlineService.updateAirline(airlineId, request));
    }

    @Operation(summary = "Deactivate airline")
    @PutMapping("/{airlineId}/deactivate")
    public ResponseEntity<ApiResponse> deactivateAirline(@PathVariable UUID airlineId) {
        return ResponseEntity.ok(airlineService.deactivateAirline(airlineId));
    }

    @Operation(summary = "Reactivate airline")
    @PutMapping("/{airlineId}/activate")
    public ResponseEntity<ApiResponse> activateAirline(@PathVariable UUID airlineId) {
        return ResponseEntity.ok(airlineService.activateAirline(airlineId));
    }
}
