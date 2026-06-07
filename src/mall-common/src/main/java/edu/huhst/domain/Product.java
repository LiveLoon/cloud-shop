package edu.huhst.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品实体类
 *
 * @author generated
 * @date 2026-05-31
 */
@Data
@Entity
@Table(name = "product")
public class Product {

    /**
     * 主键ID，自增策略
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 商品名称
     */
    @Column(name = "product_name",unique = true)
    private String productName;

    /**
     * 产品图片
     */
    @Column(name = "image_url")
    private String imageUrl;
    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer inventory;
}