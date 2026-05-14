package com.skybooker.payment.controller;

import com.skybooker.payment.dto.*;
import com.skybooker.payment.entity.PaymentStatus;
import com.skybooker.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Razorpay payment and receipt APIs")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Initiate payment and create Razorpay order")
    @PostMapping("/initiate")
    public PaymentResponse initiatePayment(@Valid @RequestBody PaymentInitiateRequest request) {
        return paymentService.initiatePayment(request);
    }

    @Operation(summary = "Get Razorpay order details for frontend checkout")
    @GetMapping("/{paymentId}/razorpay-order")
    public RazorpayOrderResponse getRazorpayOrder(@PathVariable UUID paymentId) {
        return paymentService.getRazorpayOrder(paymentId);
    }

    @Operation(summary = "Verify successful Razorpay payment")
    @PostMapping("/{paymentId}/verify")
    public PaymentResponse verifyPayment(@PathVariable UUID paymentId,
                                         @Valid @RequestBody VerifyPaymentRequest request) {
        return paymentService.verifyPayment(paymentId, request);
    }

    @Operation(summary = "Refund payment")
    @PostMapping("/refund")
    public PaymentResponse refundPayment(@Valid @RequestBody RefundRequest request) {
        return paymentService.refundPayment(request);
    }

    @Operation(summary = "Get payment by booking")
    @GetMapping("/booking/{bookingId}")
    public PaymentResponse getPaymentByBooking(@PathVariable UUID bookingId) {
        return paymentService.getPaymentByBooking(bookingId);
    }

    @Operation(summary = "Get payments by user")
    @GetMapping("/user/{userId}")
    public List<PaymentResponse> getPaymentsByUser(@PathVariable UUID userId) {
        return paymentService.getPaymentsByUser(userId);
    }

    @Operation(summary = "Get payment status")
    @GetMapping("/{paymentId}/status")
    public PaymentStatus getPaymentStatus(@PathVariable UUID paymentId) {
        return paymentService.getPaymentStatus(paymentId);
    }

    @Operation(summary = "Manual payment status update")
    @PutMapping("/{paymentId}/status")
    public PaymentResponse updatePaymentStatus(@PathVariable UUID paymentId,
                                               @Valid @RequestBody PaymentStatusUpdateRequest request) {
        return paymentService.updatePaymentStatus(paymentId, request);
    }

    @Operation(summary = "Get receipt as base64 PDF response")
    @GetMapping("/receipt/{paymentId}")
    public ReceiptResponse generateReceipt(@PathVariable UUID paymentId) {
        return paymentService.generateReceipt(paymentId);
    }

    @Operation(summary = "Download receipt PDF")
    @GetMapping("/receipt/{paymentId}/download")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable UUID paymentId) {
        ReceiptResponse receipt = paymentService.generateReceipt(paymentId);
        byte[] pdfBytes = Base64.getDecoder().decode(receipt.receiptContent());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + receipt.fileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @Operation(summary = "Get revenue between dates")
    @GetMapping("/revenue")
    public RevenueResponse getRevenue(@RequestParam LocalDateTime from,
                                      @RequestParam LocalDateTime to) {
        return paymentService.getRevenue(from, to);
    }
}
