package com.skybooker.payment.service;

import com.skybooker.payment.dto.*;
import com.skybooker.payment.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiatePayment(PaymentInitiateRequest request);

    RazorpayOrderResponse getRazorpayOrder(UUID paymentId);

    PaymentResponse verifyPayment(UUID paymentId, VerifyPaymentRequest request);

    PaymentResponse getPaymentByBooking(UUID bookingId);

    List<PaymentResponse> getPaymentsByUser(UUID userId);

    PaymentResponse refundPayment(RefundRequest request);

    PaymentStatus getPaymentStatus(UUID paymentId);

    PaymentResponse updatePaymentStatus(UUID paymentId, PaymentStatusUpdateRequest request);

    ReceiptResponse generateReceipt(UUID paymentId);

    RevenueResponse getRevenue(LocalDateTime from, LocalDateTime to);
}
