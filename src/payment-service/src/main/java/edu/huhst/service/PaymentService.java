package edu.huhst.service;

import edu.huhst.domain.Payment;
import edu.huhst.dto.PaymentDTO;
import edu.huhst.dto.PaymentTxDTO;
import edu.huhst.dto.Result;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

public interface PaymentService {
    Payment createPayment(PaymentDTO paymentDTO);
    Payment getPaymentById(Integer paymentId);
    Payment getPaymentByOrderId(Integer orderId);
    @Transactional
    void markAsPaid(String txHash, Integer paymentId, BigInteger realValue, int confirmations);
    @Transactional
    void markAsFailed(Integer paymentId, String reason);

    Payment updatePayment(Integer paymentId, Payment updateDTO);

    void deletePayment(Integer paymentId);
}
