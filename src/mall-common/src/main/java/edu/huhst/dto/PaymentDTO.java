package edu.huhst.dto;

import lombok.Data;

import java.math.BigInteger;

@Data
public class PaymentDTO{
    Integer orderId;
    String buyerAddress;
    BigInteger value;
    BigInteger chainId;
}
