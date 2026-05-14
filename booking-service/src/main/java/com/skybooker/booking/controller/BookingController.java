package com.skybooker.booking.controller;

import com.skybooker.booking.dto.AddOnRequest;
import com.skybooker.booking.dto.BoardingPassResponse;
import com.skybooker.booking.dto.BookingResponse;
import com.skybooker.booking.dto.CreateBookingRequest;
import com.skybooker.booking.dto.FareSummaryResponse;
import com.skybooker.booking.dto.TicketResponse;
import com.skybooker.booking.dto.UpdateBookingStatusRequest;
import com.skybooker.booking.entity.BookingStatus;
import com.skybooker.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@Tag(name = "Bookings", description = "Booking lifecycle APIs")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Create booking with passenger, seat and payment orchestration")
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @Operation(summary = "Get booking by id")
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    @Operation(summary = "Get booking by PNR")
    @GetMapping("/pnr/{pnrCode}")
    public ResponseEntity<BookingResponse> getBookingByPnr(@PathVariable String pnrCode) {
        return ResponseEntity.ok(bookingService.getBookingByPnr(pnrCode));
    }

    @Operation(summary = "Get bookings by user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    @Operation(summary = "Get bookings by flight")
    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByFlight(@PathVariable UUID flightId) {
        return ResponseEntity.ok(bookingService.getBookingsByFlight(flightId));
    }

    @Operation(summary = "Get upcoming bookings")
    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<BookingResponse>> getUpcomingBookings(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getUpcomingBookings(userId));
    }

    @Operation(summary = "Cancel booking and trigger release/refund")
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }

    @Operation(summary = "Update booking status")
    @PutMapping("/{bookingId}/status")
    public ResponseEntity<BookingResponse> updateStatus(
            @PathVariable UUID bookingId,
            @Valid @RequestBody UpdateBookingStatusRequest request
    ) {
        return ResponseEntity.ok(bookingService.updateStatus(bookingId, request));
    }

    @Operation(summary = "Calculate fare from flight-service price")
    @GetMapping("/calculate-fare")
    public ResponseEntity<FareSummaryResponse> calculateFare(
            @RequestParam UUID flightId,
            @RequestParam Integer passengerCount,
            @RequestParam Integer luggageKg
    ) {
        return ResponseEntity.ok(bookingService.calculateFare(flightId, passengerCount, luggageKg));
    }

    @Operation(summary = "Add or update add-ons")
    @PostMapping("/{bookingId}/addons")
    public ResponseEntity<BookingResponse> addAddOn(
            @PathVariable UUID bookingId,
            @Valid @RequestBody AddOnRequest request
    ) {
        return ResponseEntity.ok(bookingService.addAddOn(bookingId, request));
    }

    @Operation(summary = "Get bookings by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingResponse>> getBookingsByStatus(@PathVariable BookingStatus status) {
        return ResponseEntity.ok(bookingService.getBookingsByStatus(status));
    }

    @Operation(summary = "Get e-ticket as base64 PDF")
    @GetMapping("/{bookingId}/eticket")
    public ResponseEntity<TicketResponse> generateETicket(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.generateETicket(bookingId));
    }

    @Operation(summary = "Download e-ticket PDF")
    @GetMapping("/{bookingId}/eticket/download")
    public ResponseEntity<byte[]> downloadETicket(@PathVariable UUID bookingId) {
        TicketResponse ticket = bookingService.generateETicket(bookingId);
        byte[] pdfBytes = Base64.getDecoder().decode(ticket.content());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + ticket.fileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @Operation(summary = "Get boarding pass as base64 PDF")
    @GetMapping("/{bookingId}/boarding-pass/{passengerId}")
    public ResponseEntity<BoardingPassResponse> generateBoardingPass(@PathVariable UUID bookingId,
                                                                     @PathVariable UUID passengerId) {
        return ResponseEntity.ok(bookingService.generateBoardingPass(bookingId, passengerId));
    }

    @Operation(summary = "Download boarding pass PDF")
    @GetMapping("/{bookingId}/boarding-pass/{passengerId}/download")
    public ResponseEntity<byte[]> downloadBoardingPass(@PathVariable UUID bookingId,
                                                       @PathVariable UUID passengerId) {
        BoardingPassResponse response = bookingService.generateBoardingPass(bookingId, passengerId);
        byte[] pdfBytes = Base64.getDecoder().decode(response.content());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.fileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
