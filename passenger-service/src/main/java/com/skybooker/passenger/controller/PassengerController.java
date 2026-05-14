package com.skybooker.passenger.controller;

import com.skybooker.passenger.dto.ApiResponse;
import com.skybooker.passenger.dto.PassengerCountResponse;
import com.skybooker.passenger.dto.PassengerRequest;
import com.skybooker.passenger.dto.PassengerResponse;
import com.skybooker.passenger.dto.SeatAssignmentRequest;
import com.skybooker.passenger.service.PassengerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/passengers")
@Tag(name = "Passengers", description = "Passenger details and seat assignment APIs")
public class PassengerController {
    private final PassengerService passengerService;
    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @Operation(summary = "Add passenger")
    @PostMapping
    public ResponseEntity<PassengerResponse> addPassenger(@Valid @RequestBody PassengerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(passengerService.addPassenger(request));
    }

    @Operation(summary = "Get passenger by id")
    @GetMapping("/{passengerId}")
    public ResponseEntity<PassengerResponse> getPassengerById(@PathVariable UUID passengerId) {
        return ResponseEntity.ok(passengerService.getPassengerById(passengerId));
    }

    @Operation(summary = "Get all passengers by booking")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PassengerResponse>> getPassengersByBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(passengerService.getPassengersByBooking(bookingId));
    }

    @Operation(summary = "Get passenger by passport number")
    @GetMapping("/passport/{passportNumber}")
    public ResponseEntity<PassengerResponse> getByPassportNumber(@PathVariable String passportNumber) {
        return ResponseEntity.ok(passengerService.getByPassportNumber(passportNumber));
    }

    @Operation(summary = "Update passenger")
    @PutMapping("/{passengerId}")
    public ResponseEntity<PassengerResponse> updatePassenger(@PathVariable UUID passengerId,@Valid @RequestBody PassengerRequest request) {
        return ResponseEntity.ok(passengerService.updatePassenger(passengerId, request));
    }

    @Operation(summary = "Assign seat to passenger")
    @PutMapping("/{passengerId}/assign-seat")
    public ResponseEntity<ApiResponse> assignSeat(@PathVariable UUID passengerId,@Valid @RequestBody SeatAssignmentRequest request) {
        return ResponseEntity.ok(passengerService.assignSeat(passengerId, request));
    }

    @Operation(summary = "Delete passenger")
    @DeleteMapping("/{passengerId}")
    public ResponseEntity<ApiResponse> deletePassenger(@PathVariable UUID passengerId) {
        return ResponseEntity.ok(passengerService.deletePassenger(passengerId));
    }

    @Operation(summary = "Delete passengers by booking")
    @DeleteMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse> deletePassengersByBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(passengerService.deletePassengersByBooking(bookingId));
    }

    @Operation(summary = "Get passenger count by booking")
    @GetMapping("/booking/{bookingId}/count")
    public ResponseEntity<PassengerCountResponse> getPassengerCount(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(passengerService.getPassengerCount(bookingId));
    }
}
