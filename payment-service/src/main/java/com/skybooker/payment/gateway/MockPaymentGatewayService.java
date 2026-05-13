package com.skybooker.payment.gateway;

import com.skybooker.payment.dto.PaymentInitiateRequest;
import com.skybooker.payment.dto.VerifyPaymentRequest;
import com.skybooker.payment.entity.Payment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "application.payment.mock-mode", havingValue = "true")
public class MockPaymentGatewayService implements PaymentGatewayService {

    @Override
    public String createPaymentSession(Payment payment, PaymentInitiateRequest request) {
        return "MOCK_ORDER_" + payment.getPaymentId();
    }

    @Override
    public String verifyPaymentSignature(VerifyPaymentRequest request) {
        return "MOCK_SIGNATURE_VERIFIED";
    }

    @Override
    public String verifyWebhookSignature(String rawPayload, String signature) {
        return "MOCK_WEBHOOK_VERIFIED";
    }

    @Override
    public String processRefund(Payment payment) {
        return "MOCK_REFUND_SUCCESS";
    }
}
