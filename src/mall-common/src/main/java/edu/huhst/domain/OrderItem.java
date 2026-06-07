package edu.huhst.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id",nullable = false)
    private Integer orderId;

    @Column(name = "product_id",nullable = false)
    private Integer productId;

    @Column(name = "buy_num",nullable = false)
    private Integer buyNum;


    @Column(name = "product_name",nullable = false)
    private String productName;



    @Column(name = "price",nullable = false)
    private Double price;
}