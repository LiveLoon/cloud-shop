package edu.huhst.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
* 订单主表实体类
*
* @author generated
* @date 2026-05-31
*/
@Data
@Entity
@Table(name = "`order`")
public class Order {

    /**
     * 主键ID，自增策略
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 下单时间
     */
    @Column( name = "create_time",nullable = false)
    private LocalDateTime createTime;

    /**
     * 用户ID，关联用户表
     */
    @Column(name = "user_id",nullable = false)
    private Integer userId;

    @Column(name = "total_price")
    private double totalPrice;
}