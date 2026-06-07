package edu.huhst.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductVO {
    private String productName;
    private BigDecimal price;
    private Integer inventory;
    private String imageUrl;
}