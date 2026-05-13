package com.skybooker.notification.client;

import com.skybooker.notification.client.dto.BookingResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @GetMapping("/bookings/flight/{flightId}")
    List<BookingResponseDto> getBookingsByFlight(@PathVariable("flightId") UUID flightId);
}
