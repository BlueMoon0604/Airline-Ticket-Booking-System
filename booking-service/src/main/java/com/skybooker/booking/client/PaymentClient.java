package com.skybooker.booking.client;

import com.skybooker.booking.client.dto.PaymentInitiateRequestDto;
import com.skybooker.booking.client.dto.PaymentResponseDto;
import com.skybooker.booking.client.dto.RefundRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/payments/initiate")
    PaymentResponseDto initiatePayment(@RequestBody PaymentInitiateRequestDto request);

    @GetMapping("/payments/booking/{bookingId}")
    PaymentResponseDto getPaymentByBooking(@PathVariable("bookingId") UUID bookingId);

    @PostMapping("/payments/refund")
    PaymentResponseDto refundPayment(@RequestBody RefundRequestDto request);
}
