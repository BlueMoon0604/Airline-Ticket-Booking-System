package com.skybooker.booking.client;

import com.skybooker.booking.client.dto.BookingConfirmationRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/notifications/booking-confirmation")
    Object sendBookingConfirmation(@RequestBody BookingConfirmationRequestDto request);
}
