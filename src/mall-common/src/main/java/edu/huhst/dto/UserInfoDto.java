package edu.huhst.dto;

import lombok.Data;

@Data
public class UserInfoDto {
    private Integer id;
    private String username;
    private String realName;
    private String password;
    private Integer score;
}