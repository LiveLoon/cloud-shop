package edu.huhst.vo;

import lombok.Data;

@Data
public class PaymentVO {
    Integer orderId;
    String buyerAddress;
    String value;
    Integer chainId;
}
