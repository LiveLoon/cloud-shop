package edu.huhst.service;

import edu.huhst.domain.OrderItem;

import java.util.List;

public interface OrderItemService {
    List<OrderItem> getOrderItemListByOrderId(Integer id);
    void deleteAllOrderItemsByOrderId(Integer id);
}
