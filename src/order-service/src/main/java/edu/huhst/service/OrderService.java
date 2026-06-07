package edu.huhst.service;

import edu.huhst.dto.OrderCreateDTO;
import edu.huhst.vo.OrderVO;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderService {
    OrderVO getOrderWithDetails(Integer orderId);

    void createOrder(OrderCreateDTO createDTO);


    OrderVO getOrdersById(Integer id);

    List<OrderVO> getAllOrders();

    void deleteOrder(Integer id);

    // 新增：分页查询订单列表
    Page<OrderVO> getOrderPage(int page, int size);

    Page<OrderVO> getOrderPage(int page, int size, Integer userId);
}