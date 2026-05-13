package com.skybooker.payment.gateway;

import com.skybooker.payment.dto.PaymentInitiateRequest;
import com.skybooker.payment.dto.VerifyPaymentRequest;
import com.skybooker.payment.entity.Payment;

public interface PaymentGatewayService {

    String createPaymentSession(Payment payment, PaymentInitiateRequest request);

    String verifyPaymentSignature(VerifyPaymentRequest request);

    String verifyWebhookSignature(String rawPayload, String signature);

    String processRefund(Payment payment);
}
