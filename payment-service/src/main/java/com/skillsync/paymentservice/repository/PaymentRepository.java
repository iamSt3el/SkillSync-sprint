package com.skillsync.paymentservice.repository;

import com.skillsync.paymentservice.entity.Payment;
import com.skillsync.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findBySessionId(Long sessionId);

    List<Payment> findByLearnerId(Long learnerId);

    List<Payment> findByStatus(PaymentStatus status);

    boolean existsBySessionIdAndStatus(Long sessionId, PaymentStatus status);
}
