package com.skybooker.payment.gateway;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import com.razorpay.Utils;
import com.skybooker.payment.config.PaymentProperties;
import com.skybooker.payment.dto.PaymentInitiateRequest;
import com.skybooker.payment.dto.VerifyPaymentRequest;
import com.skybooker.payment.entity.Payment;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(name = "application.payment.mock-mode", havingValue = "false")
public  class RazorpayPaymentGatewayService implements PaymentGatewayService {

    private final PaymentProperties paymentProperties;
    private final RazorpayClient razorpayClient;

    public RazorpayPaymentGatewayService(PaymentProperties paymentProperties) throws Exception {
        this.paymentProperties = paymentProperties;
        this.razorpayClient = new RazorpayClient(
                paymentProperties.getRazorpay().getKeyId(),
                paymentProperties.getRazorpay().getKeySecret()
        );
    }

    @Override
    public String createPaymentSession(Payment payment, PaymentInitiateRequest request) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.amount().multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", paymentProperties.getCurrency());
            orderRequest.put("receipt", payment.getPaymentId().toString());
            orderRequest.put("notes", new JSONObject()
                    .put("bookingId", request.bookingId().toString())
                    .put("userId", request.userId().toString()));

            Order order = razorpayClient.orders.create(orderRequest);
            return order.get("id");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create Razorpay order", ex);
        }
    }

    @Override
    public String verifyPaymentSignature(VerifyPaymentRequest request) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.razorpayOrderId());
            options.put("razorpay_payment_id", request.razorpayPaymentId());
            options.put("razorpay_signature", request.razorpaySignature());

            boolean verified = Utils.verifyPaymentSignature(options, paymentProperties.getRazorpay().getKeySecret());
            if (!verified) {
                throw new RuntimeException("Razorpay payment signature verification failed");
            }
            return "PAYMENT_SIGNATURE_VERIFIED";
        } catch (Exception ex) {
            throw new RuntimeException("Failed to verify Razorpay payment signature", ex);
        }
    }

    @Override
    public String verifyWebhookSignature(String rawPayload, String signature) {
        try {
            Utils.verifyWebhookSignature(
                    rawPayload,
                    signature,
                    paymentProperties.getRazorpay().getWebhookSecret()
            );
            return "WEBHOOK_SIGNATURE_VERIFIED";
        } catch (Exception ex) {
            throw new RuntimeException("Failed to verify Razorpay webhook signature", ex);
        }
    }

    @Override
    public String processRefund(Payment payment) {
        try {
            JSONObject refundRequest = new JSONObject();
            if (payment.getRefundAmount() != null) {
                refundRequest.put("amount", payment.getRefundAmount().multiply(BigDecimal.valueOf(100)).intValue());
            }
            Refund refund = razorpayClient.payments.refund(payment.getTransactionId(), refundRequest);
            return refund.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to process Razorpay refund", ex);
        }
    }
}
