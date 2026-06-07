package edu.huhst.domain;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 用户实体类（对应 shop_user 表）
 *
 * @author generated
 * @date 2026-05-31
 */
@Data
@Entity
@Table(name = "shop_user")
public class User {

    /**
     * 主键ID，自增策略
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 登录账号
     */
    @Column(name = "username",unique = true)
    private String username;

    /**
     * 登录密码
     */
    @Column(name = "password")
    private String password;

    /**
     * 真实姓名
     */
    @Column(name = "real_name")
    private String realName;

    /**
     * 用户积分
     */
    @Column(name = "score")
    private Integer score;
}