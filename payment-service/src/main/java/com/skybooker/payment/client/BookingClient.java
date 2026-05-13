package com.skybooker.payment.client;

import com.skybooker.payment.client.dto.BookingResponseDto;
import com.skybooker.payment.client.dto.UpdateBookingStatusRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "booking-service")
public interface BookingClient {

    @PutMapping("/bookings/{bookingId}/status")
    BookingResponseDto updateBookingStatus(@PathVariable("bookingId") UUID bookingId,
                                           @RequestBody UpdateBookingStatusRequestDto request);
}
