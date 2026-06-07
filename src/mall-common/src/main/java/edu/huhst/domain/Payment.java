package edu.huhst.domain;

import edu.huhst.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 关联的订单项ID（示例中原样，若改为关联订单则使用 orderId）
     */

    @Column(name = "order_id",nullable = false)
    private Integer orderId;

    /**
     * 支付状态：created,pending, paid, failed 等
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    /**
     * 支付状态：created,pending, paid, failed 等
     */
    @Column(nullable = false,length = 1024)
    private String paymentMessage;


    /**
     * 支付者地址
     */
    @Column(name = "buyer_address",nullable = false, length = 64)
    private String buyerAddress;

    /**
     * 收款着地址
     */
    @Column(name = "seller_address",nullable = false, length = 64)
    private String sellerAddress;

    /**
     * 需要支付的金额，单位 wei（使用字符串避免溢出）
     */
    @Column(name = "value",nullable = false,length = 64)
    private BigInteger value;

    /**
     * 实际支付的金额，单位 wei（使用字符串避免溢出）
     */
    @Column(name = "real_value",length = 64)
    private BigInteger realValue;

    /**
     * 网络标识
     */
    @Column(name = "chain_id",nullable = false)
    private BigInteger chainId;

    /**
     * 链上交易哈希
     */
    @Column(length = 128)
    private String tx;

    @Column(name = "tx_time")
    private LocalDateTime txTime;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = createTime;
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

}
