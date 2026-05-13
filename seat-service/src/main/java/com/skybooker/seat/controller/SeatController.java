package com.skybooker.seat.controller;

import com.skybooker.seat.dto.ApiResponse;
import com.skybooker.seat.dto.SeatCountByClassResponse;
import com.skybooker.seat.dto.SeatMapResponse;
import com.skybooker.seat.dto.SeatRequest;
import com.skybooker.seat.dto.SeatResponse;
import com.skybooker.seat.entity.SeatClass;
import com.skybooker.seat.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/seats")
@Tag(name = "Seats", description = "Seat inventory and seat map APIs")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @Operation(summary = "Add a single seat")
    @PostMapping
    public ResponseEntity<SeatResponse> addSeat(@Valid @RequestBody SeatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seatService.addSeat(request));
    }

    @Operation(summary = "Add multiple seats for a flight")
    @PostMapping("/bulk")
    public ResponseEntity<List<SeatResponse>> addSeatsForFlight(@Valid @RequestBody List<SeatRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seatService.addSeatsForFlight(requests));
    }

    @Operation(summary = "Get all seats by flight")
    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<SeatResponse>> getSeatsByFlight(@PathVariable UUID flightId) {
        return ResponseEntity.ok(seatService.getSeatsByFlight(flightId));
    }

    @Operation(summary = "Get available seats by flight")
    @GetMapping("/flight/{flightId}/available")
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(@PathVariable UUID flightId) {
        return ResponseEntity.ok(seatService.getAvailableSeats(flightId));
    }

    @Operation(summary = "Get available seats by class")
    @GetMapping("/flight/{flightId}/class/{seatClass}")
    public ResponseEntity<List<SeatResponse>> getAvailableByClass(
            @PathVariable UUID flightId,
            @PathVariable SeatClass seatClass) {
        return ResponseEntity.ok(seatService.getAvailableByClass(flightId, seatClass));
    }

    @Operation(summary = "Get seat by id")
    @GetMapping("/{seatId}")
    public ResponseEntity<SeatResponse> getSeatById(@PathVariable UUID seatId) {
        return ResponseEntity.ok(seatService.getSeatById(seatId));
    }

    @Operation(summary = "Get seat map for a flight")
    @GetMapping("/flight/{flightId}/map")
    public ResponseEntity<SeatMapResponse> getSeatMap(@PathVariable UUID flightId) {
        return ResponseEntity.ok(seatService.getSeatMap(flightId));
    }

    @Operation(summary = "Hold a seat for 15 minutes")
    @PutMapping("/{seatId}/hold")
    public ResponseEntity<ApiResponse> holdSeat(@PathVariable UUID seatId) {
        return ResponseEntity.ok(seatService.holdSeat(seatId));
    }

    @Operation(summary = "Release a held seat")
    @PutMapping("/{seatId}/release")
    public ResponseEntity<ApiResponse> releaseSeat(@PathVariable UUID seatId) {
        return ResponseEntity.ok(seatService.releaseSeat(seatId));
    }

    @Operation(summary = "Confirm a held seat")
    @PutMapping("/{seatId}/confirm")
    public ResponseEntity<ApiResponse> confirmSeat(@PathVariable UUID seatId) {
        return ResponseEntity.ok(seatService.confirmSeat(seatId));
    }

    @Operation(summary = "Update seat")
    @PutMapping("/{seatId}")
    public ResponseEntity<SeatResponse> updateSeat(
            @PathVariable UUID seatId,
            @Valid @RequestBody SeatRequest request) {
        return ResponseEntity.ok(seatService.updateSeat(seatId, request));
    }

    @Operation(summary = "Count available seats by class")
    @GetMapping("/flight/{flightId}/count")
    public ResponseEntity<List<SeatCountByClassResponse>> countAvailableByClass(@PathVariable UUID flightId) {
        return ResponseEntity.ok(seatService.countAvailableByClass(flightId));
    }

    @Operation(summary = "Delete all seats for a flight")
    @DeleteMapping("/flight/{flightId}")
    public ResponseEntity<ApiResponse> deleteSeatsForFlight(@PathVariable UUID flightId) {
        return ResponseEntity.ok(seatService.deleteSeatsForFlight(flightId));
    }

    @Operation(summary = "Release expired seat holds manually")
    @PutMapping("/release-expired")
    public ResponseEntity<ApiResponse> releaseExpiredHolds() {
        return ResponseEntity.ok(seatService.releaseExpiredHolds());
    }
}
