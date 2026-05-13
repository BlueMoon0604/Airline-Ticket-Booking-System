package com.skybooker.booking.client;

import com.skybooker.booking.client.dto.PassengerRequestDto;
import com.skybooker.booking.client.dto.PassengerResponseDto;
import com.skybooker.booking.client.dto.SeatAssignmentRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "passenger-service",
        url = "${application.clients.passenger-service-url:http://localhost:8085}"
)
public interface PassengerClient {

    @PostMapping("/passengers")
    PassengerResponseDto addPassenger(@RequestBody PassengerRequestDto request);

    @PutMapping("/passengers/{passengerId}/assign-seat")
    Object assignSeat(@PathVariable("passengerId") UUID passengerId,
                      @RequestBody SeatAssignmentRequestDto request);

    @GetMapping("/passengers/booking/{bookingId}")
    List<PassengerResponseDto> getPassengersByBooking(@PathVariable("bookingId") UUID bookingId);

    @DeleteMapping("/passengers/booking/{bookingId}")
    Object deletePassengersByBooking(@PathVariable("bookingId") UUID bookingId);
}
