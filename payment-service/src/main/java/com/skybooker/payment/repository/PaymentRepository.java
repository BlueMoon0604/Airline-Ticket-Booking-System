package com.skybooker.payment.repository;

import com.skybooker.payment.entity.Payment;
import com.skybooker.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByBookingId(UUID bookingId);

    List<Payment> findByUserId(UUID userId);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByPaidAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.userId = :userId and p.status = com.skybooker.payment.entity.PaymentStatus.PAID")
    BigDecimal sumAmountByUserId(UUID userId);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = com.skybooker.payment.entity.PaymentStatus.PAID and p.paidAt between :start and :end")
    BigDecimal sumRevenueBetween(LocalDateTime start, LocalDateTime end);
}
