package com.skybooker.booking.client;

import com.skybooker.booking.client.dto.FlightResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
        name = "flight-service",
        url = "${application.clients.flight-service-url:http://localhost:8083}"
)
public interface FlightClient {

    @GetMapping("/flights/{flightId}")
    FlightResponse getFlightById(@PathVariable("flightId") UUID flightId);

    @PutMapping("/flights/{flightId}/decrement-seats")
    Object decrementSeats(@PathVariable("flightId") UUID flightId,
                          @RequestBody SeatCountRequest request);

    @PutMapping("/flights/{flightId}/increment-seats")
    Object incrementSeats(@PathVariable("flightId") UUID flightId,
                          @RequestBody SeatCountRequest request);

    record SeatCountRequest(Integer seats) {
    }
}
