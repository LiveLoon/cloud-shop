package edu.huhst.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderCreateDTO {
    private Integer userId;
    private List<OrderItemDTO> itemList;
}