package edu.huhst.service;

import edu.huhst.domain.Payment;
import edu.huhst.dto.PaymentDTO;
import edu.huhst.dto.PaymentTxDTO;
import edu.huhst.dto.Result;
import edu.huhst.enums.PaymentStatus;
import edu.huhst.feign.OrderFeignApi;
import edu.huhst.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService{
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderFeignApi orderFeignApi;

    @Value("${blockchain.platform-address}")
    // 平台收款地址（实际可从配置读取）
    private  String platformAddress;

    @Override
    @Transactional
    public Payment createPayment(PaymentDTO paymentDTO) {
        // 1. 校验订单是否存在（可选但推荐）
        Result<?> orderResult = orderFeignApi.getOrderById(paymentDTO.getOrderId());
        if (orderResult == null || orderResult.getCode() != 200) {
            throw new RuntimeException("订单不存在");
        }
        // 2. 检查是否已存在该订单的支付记录（避免重复创建）
        Optional<Payment> existing = paymentRepository.findByOrderId(paymentDTO.getOrderId());
        if (existing.isPresent()) {
            throw new RuntimeException("该订单已创建支付，不能重复创建");
        }

        Payment payment = new Payment();
        payment.setOrderId(paymentDTO.getOrderId());
        payment.setStatus(PaymentStatus.CREATED);
        payment.setPaymentMessage("等待用户支付");
        payment.setBuyerAddress(paymentDTO.getBuyerAddress());
        payment.setSellerAddress(platformAddress);
        payment.setValue(paymentDTO.getValue());
        payment.setChainId(paymentDTO.getChainId());

        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("支付记录不存在"));
    }

    @Override
    public Payment getPaymentByOrderId(Integer orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("该订单无支付记录"));
    }

    @Override
    @Transactional
    public void markAsPaid(String txHash, Integer paymentId, BigInteger realValue, int confirmations) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("支付记录不存在"));
        if (!PaymentStatus.CREATED.equals(payment.getStatus())) {
            return; // 已经是终态
        }
        payment.setTx(txHash);
        payment.setRealValue(realValue);
        payment.setStatus(PaymentStatus.PAID);
        payment.setTxTime(LocalDateTime.now());
        payment.setPaymentMessage("交易已确认，到账 " + realValue + " Wei");
        paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void markAsFailed(Integer paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("支付记录不存在"));
        if (!PaymentStatus.CREATED.equals(payment.getStatus())) {
            return;
        }
        payment.setStatus(PaymentStatus.FAILED);
        payment.setPaymentMessage(reason);
        paymentRepository.save(payment);
    }


    @Override
    @Transactional
    public Payment updatePayment(Integer paymentId, Payment updateDTO) {
        Payment payment = getPaymentById(paymentId);
        return paymentRepository.save(updateDTO);
    }

    @Override
    @Transactional
    public void deletePayment(Integer paymentId) {
        Payment payment = getPaymentById(paymentId);
        paymentRepository.deleteById(paymentId);
    }
}
