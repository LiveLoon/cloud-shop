package edu.huhst.repository;

import edu.huhst.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Integer> {
    Optional<Payment> findByOrderId(Integer orderId);
    Optional<Payment> findByTx(String tx);
}
