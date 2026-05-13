package com.skybooker.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import com.skybooker.booking.client.dto.SeatResponseDto;


import java.util.UUID;

@FeignClient(
        name = "seat-service",
        url = "${application.clients.seat-service-url:http://localhost:8084}"
)
public interface SeatClient {

    @PutMapping("/seats/{seatId}/hold")
    Object holdSeat(@PathVariable("seatId") UUID seatId);

    @PutMapping("/seats/{seatId}/confirm")
    Object confirmSeat(@PathVariable("seatId") UUID seatId);

    @PutMapping("/seats/{seatId}/release")
    Object releaseSeat(@PathVariable("seatId") UUID seatId);
    
    @GetMapping("/seats/{seatId}")
    SeatResponseDto getSeatById(@PathVariable("seatId") UUID seatId);

}
