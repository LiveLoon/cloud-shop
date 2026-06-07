package edu.huhst.vo;

import lombok.Data;

@Data
public class OrderItemVO {
    private Integer productId;
    private String productName;
    private Integer buyNum;
    private Double price;
}
