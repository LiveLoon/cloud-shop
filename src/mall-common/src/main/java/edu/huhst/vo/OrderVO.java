package edu.huhst.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Integer orderId;
    private LocalDateTime createTime;
    private UserVO userInfo;
    private List<OrderItemVO> itemList;
}