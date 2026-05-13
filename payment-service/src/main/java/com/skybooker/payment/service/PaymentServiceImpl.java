package com.skybooker.payment.service;

import com.skybooker.payment.client.BookingClient;
import com.skybooker.payment.client.dto.UpdateBookingStatusRequestDto;
import com.skybooker.payment.config.PaymentProperties;
import com.skybooker.payment.dto.PaymentInitiateRequest;
import com.skybooker.payment.dto.PaymentResponse;
import com.skybooker.payment.dto.PaymentStatusUpdateRequest;
import com.skybooker.payment.dto.ReceiptResponse;
import com.skybooker.payment.dto.RefundRequest;
import com.skybooker.payment.dto.RevenueResponse;
import com.skybooker.payment.dto.RazorpayOrderResponse;
import com.skybooker.payment.dto.VerifyPaymentRequest;
import com.skybooker.payment.entity.Payment;
import com.skybooker.payment.entity.PaymentStatus;
import com.skybooker.payment.exception.BadRequestException;
import com.skybooker.payment.exception.ResourceNotFoundException;
import com.skybooker.payment.gateway.PaymentGatewayService;
import com.skybooker.payment.messaging.PaymentEventPublisher;
import com.skybooker.payment.repository.PaymentRepository;
import com.skybooker.payment.service.PaymentService;
import com.skybooker.payment.service.PdfReceiptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentEventPublisher paymentEventPublisher;
    private final BookingClient bookingClient;
    private final PaymentProperties paymentProperties;
    private final PdfReceiptService pdfReceiptService;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              PaymentGatewayService paymentGatewayService,
                              PaymentEventPublisher paymentEventPublisher,
                              BookingClient bookingClient,
                              PaymentProperties paymentProperties,
                              PdfReceiptService pdfReceiptService) {
        this.paymentRepository = paymentRepository;
        this.paymentGatewayService = paymentGatewayService;
        this.paymentEventPublisher = paymentEventPublisher;
        this.bookingClient = bookingClient;
        this.paymentProperties = paymentProperties;
        this.pdfReceiptService = pdfReceiptService;
    }

    @Override
    public PaymentResponse initiatePayment(PaymentInitiateRequest request) {
        Payment payment = new Payment();
        payment.setBookingId(request.bookingId());
        payment.setUserId(request.userId());
        payment.setAmount(request.amount());
        payment.setCurrency(paymentProperties.getCurrency());
        payment.setPaymentMode(request.paymentMode());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setGatewayName("RAZORPAY");

        Payment savedPayment = paymentRepository.save(payment);
        String orderId = paymentGatewayService.createPaymentSession(savedPayment, request);
        savedPayment.setGatewayOrderId(orderId);

        Payment updatedPayment = paymentRepository.save(savedPayment);
        return map(updatedPayment, orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public RazorpayOrderResponse getRazorpayOrder(UUID paymentId) {
        Payment payment = findPayment(paymentId);
        return new RazorpayOrderResponse(
                payment.getPaymentId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getGatewayOrderId(),
                paymentProperties.getRazorpay().getKeyId(),
                payment.getStatus().name()
        );
    }

    @Override
    public PaymentResponse verifyPayment(UUID paymentId, VerifyPaymentRequest request) {
        Payment payment = findPayment(paymentId);

        if (!request.razorpayOrderId().equals(payment.getGatewayOrderId())) {
            throw new BadRequestException("Razorpay order id mismatch");
        }

        paymentGatewayService.verifyPaymentSignature(request);

        payment.setTransactionId(request.razorpayPaymentId());
        payment.setGatewaySignature(request.razorpaySignature());
        payment.setGatewayResponse("PAYMENT_SIGNATURE_VERIFIED");
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);
        paymentEventPublisher.publishPaymentCompleted(savedPayment);
        confirmBookingAfterCommit(savedPayment.getBookingId());

        return map(savedPayment, savedPayment.getGatewayOrderId());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBooking(UUID bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking"));
        return map(payment, payment.getGatewayOrderId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUser(UUID userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(payment -> map(payment, payment.getGatewayOrderId()))
                .toList();
    }

    @Override
    public PaymentResponse refundPayment(RefundRequest request) {
        Payment payment = findPayment(request.paymentId());

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BadRequestException("Only paid transaction can be refunded");
        }

        payment.setRefundAmount(request.refundAmount());
        payment.setGatewayResponse(paymentGatewayService.processRefund(payment));
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);
        return map(savedPayment, savedPayment.getGatewayOrderId());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatus getPaymentStatus(UUID paymentId) {
        return findPayment(paymentId).getStatus();
    }

    @Override
    public PaymentResponse updatePaymentStatus(UUID paymentId, PaymentStatusUpdateRequest request) {
        Payment payment = findPayment(paymentId);
        payment.setStatus(request.status());

        if (request.gatewayResponse() != null && !request.gatewayResponse().isBlank()) {
            payment.setGatewayResponse(request.gatewayResponse());
        }

        if (request.status() == PaymentStatus.PAID && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }

        Payment savedPayment = paymentRepository.save(payment);

        if (savedPayment.getStatus() == PaymentStatus.PAID) {
            paymentEventPublisher.publishPaymentCompleted(savedPayment);
            confirmBookingAfterCommit(savedPayment.getBookingId());
        }

        return map(savedPayment, savedPayment.getGatewayOrderId());
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptResponse generateReceipt(UUID paymentId) {
        Payment payment = findPayment(paymentId);
        byte[] pdfBytes = pdfReceiptService.generateReceiptPdf(payment);
        String encoded = Base64.getEncoder().encodeToString(pdfBytes);

        return new ReceiptResponse(
                payment.getPaymentId(),
                "receipt-" + payment.getPaymentId() + ".pdf",
                "application/pdf",
                encoded
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueResponse getRevenue(LocalDateTime from, LocalDateTime to) {
        return new RevenueResponse(
                paymentRepository.sumRevenueBetween(from, to),
                from,
                to
        );
    }

    private Payment findPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    private void confirmBookingAfterCommit(UUID bookingId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            confirmBooking(bookingId);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                confirmBooking(bookingId);
            }
        });
    }

    private void confirmBooking(UUID bookingId) {
        bookingClient.updateBookingStatus(
                bookingId,
                new UpdateBookingStatusRequestDto("CONFIRMED")
        );
    }

    private PaymentResponse map(Payment payment, String paymentSessionUrl) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getPaymentMode(),
                payment.getTransactionId(),
                payment.getGatewayOrderId(),
                payment.getGatewayName(),
                payment.getGatewaySignature(),
                payment.getGatewayResponse(),
                payment.getPaidAt(),
                payment.getRefundedAt(),
                payment.getRefundAmount(),
                paymentSessionUrl,
                paymentProperties.getRazorpay().getKeyId()
        );
    }
}
