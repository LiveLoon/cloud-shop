package edu.huhst.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class OrderItemDTO {
    private Integer orderId;
    private Integer productId;
    private Integer buyNum;
    private String productName;
    private Double price;
}
