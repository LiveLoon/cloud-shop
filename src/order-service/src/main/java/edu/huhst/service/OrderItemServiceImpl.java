package edu.huhst.service;

import edu.huhst.domain.OrderItem;
import edu.huhst.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService{
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Override
    public List<OrderItem> getOrderItemListByOrderId(Integer id) {
        List<OrderItem> byOrderId = orderItemRepository.findByOrderId(id);
        return byOrderId;
    }

    @Override
    public void deleteAllOrderItemsByOrderId(Integer id) {
        orderItemRepository.deleteByOrderId(id);
    }
}
