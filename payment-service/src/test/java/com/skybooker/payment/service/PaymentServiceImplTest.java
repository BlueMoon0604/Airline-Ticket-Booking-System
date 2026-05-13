package com.skybooker.payment.service;

import com.skybooker.payment.client.BookingClient;
import com.skybooker.payment.client.dto.UpdateBookingStatusRequestDto;
import com.skybooker.payment.config.PaymentProperties;
import com.skybooker.payment.dto.PaymentStatusUpdateRequest;
import com.skybooker.payment.dto.VerifyPaymentRequest;
import com.skybooker.payment.entity.Payment;
import com.skybooker.payment.entity.PaymentMode;
import com.skybooker.payment.entity.PaymentStatus;
import com.skybooker.payment.exception.BadRequestException;
import com.skybooker.payment.gateway.PaymentGatewayService;
import com.skybooker.payment.messaging.PaymentEventPublisher;
import com.skybooker.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentGatewayService paymentGatewayService;
    @Mock private PaymentEventPublisher paymentEventPublisher;
    @Mock private BookingClient bookingClient;
    @Mock private PaymentProperties paymentProperties;
    @Mock private PdfReceiptService pdfReceiptService;

    private PaymentServiceImpl paymentService;

    private UUID paymentId;
    private UUID bookingId;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(
                paymentRepository,
                paymentGatewayService,
                paymentEventPublisher,
                bookingClient,
                paymentProperties,
                pdfReceiptService
        );

        paymentId = UUID.randomUUID();
        bookingId = UUID.randomUUID();

        payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setBookingId(bookingId);
        payment.setUserId(UUID.randomUUID());
        payment.setAmount(BigDecimal.valueOf(6200));
        payment.setCurrency("INR");
        payment.setPaymentMode(PaymentMode.UPI);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setGatewayOrderId("order_123");
        payment.setGatewayName("RAZORPAY");
    }

    @Test
    void updatePaymentStatusShouldPublishEventAndConfirmBookingWhenMarkedPaid() {
        PaymentProperties.Razorpay razorpay = new PaymentProperties.Razorpay();
        razorpay.setKeyId("rzp_test_key");
        when(paymentProperties.getRazorpay()).thenReturn(razorpay);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = paymentService.updatePaymentStatus(
                paymentId,
                new PaymentStatusUpdateRequest(PaymentStatus.PAID, "MANUAL_TEST_SUCCESS")
        );

        assertThat(response.status()).isEqualTo(PaymentStatus.PAID);
        assertThat(response.paidAt()).isNotNull();
        verify(paymentEventPublisher).publishPaymentCompleted(payment);
        verify(bookingClient).updateBookingStatus(bookingId, new UpdateBookingStatusRequestDto("CONFIRMED"));
    }

    @Test
    void verifyPaymentShouldThrowWhenOrderIdDoesNotMatch() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThrows(
                BadRequestException.class,
                () -> paymentService.verifyPayment(
                        paymentId,
                        new VerifyPaymentRequest("pay_123", "order_other", "sig_123")
                )
        );
    }
}
