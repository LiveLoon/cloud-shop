package edu.huhst.dto;

import lombok.Data;

@Data
public class PaymentTxDTO {
    String tx;
    String realValue;
    String status;
    String message;
}
