package edu.huhst.controller;

import edu.huhst.domain.Payment;
import edu.huhst.dto.PaymentDTO;
import edu.huhst.dto.Result;
import edu.huhst.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 1. 创建支付（用户点击支付时调用）
     * POST /api/payment/create
     */
    @PostMapping("/create")
    public Result<Payment> createPayment(@RequestBody PaymentDTO request) {
        try {
            Payment payment = paymentService.createPayment(request);
            return Result.success(payment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 2. 根据支付 ID 查询支付详情
     * GET /api/payment/{paymentId}
     */
    @GetMapping("/{paymentId}")
    public Result<Payment> getPaymentById(@PathVariable Integer paymentId) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId);
            return Result.success(payment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 3. 根据订单 ID 查询支付详情
     * GET /api/payment/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public Result<Payment> getPaymentByOrderId(@PathVariable Integer orderId) {
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            return Result.success(payment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 5. 更新支付记录（仅允许更新部分字段，且仅对状态为 CREATED 的记录有效）
     * PUT /api/payment/{paymentId}
     */
    @PutMapping("/{paymentId}")
    public Result<Payment> updatePayment(@PathVariable Integer paymentId,
                                         @RequestBody Payment updateDTO) {
        try {
            Payment updated = paymentService.updatePayment(paymentId, updateDTO);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 6. 删除支付记录（仅允许删除未支付的记录）
     * DELETE /api/payment/{paymentId}
     */
    @DeleteMapping("/{paymentId}")
    public Result<Void> deletePayment(@PathVariable Integer paymentId) {
        try {
            paymentService.deletePayment(paymentId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
