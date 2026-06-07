package edu.huhst.service;

import edu.huhst.domain.Order;
import edu.huhst.domain.OrderItem;
import edu.huhst.domain.Product;
import edu.huhst.domain.User;
import edu.huhst.dto.OrderCreateDTO;
import edu.huhst.dto.OrderItemDTO;
import edu.huhst.dto.Result;
import edu.huhst.feign.ProductFeignApi;
import edu.huhst.feign.UserFeignApi;
import edu.huhst.repository.OrderItemRepository;
import edu.huhst.repository.OrderRepository;
import edu.huhst.vo.OrderItemVO;
import edu.huhst.vo.OrderVO;
import edu.huhst.vo.UserVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService{
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserFeignApi userFeignApi;

    @Autowired
    private ProductFeignApi productFeignApi;


    @Override
    public OrderVO getOrderWithDetails(Integer orderId) {
        return null;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void createOrder(OrderCreateDTO createDTO) {
        // 1. Validate input
        if (createDTO.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (createDTO.getItemList() == null || createDTO.getItemList().isEmpty()) {
            throw new IllegalArgumentException("订单项不能为空");
        }

        Order order = new Order();
        order.setCreateTime(java.time.LocalDateTime.now());
        //check user is exists ?
        Result<User> userResult = userFeignApi.getUserById(createDTO.getUserId());
        if(userResult == null || userResult.getCode() != 200 || userResult.getData() == null){
            throw  new RuntimeException("用户不存在，userId: " + createDTO.getUserId());
        }
        order.setUserId(createDTO.getUserId());
        order.setTotalPrice(0.0);

        Order saveOrder  = orderRepository.save(order);

        double total = 0.0;

        for (OrderItemDTO itemDTO: createDTO.getItemList()){
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(saveOrder.getId());          // link to order
            Result<Product> productById = productFeignApi.getProductById(itemDTO.getProductId());
            if(productById==null){
                throw new RuntimeException("产品不存在");
            }
            orderItem.setProductId(itemDTO.getProductId());
            orderItem.setBuyNum(itemDTO.getBuyNum());
            orderItem.setProductName(itemDTO.getProductName());
            orderItem.setPrice(itemDTO.getPrice());
            orderItemRepository.save(orderItem);
        }
    }

    @Override
    public OrderVO getOrdersById(Integer id) {
        Order order = orderRepository.findById(id).get();


        User data = userFeignApi.getUserById(order.getUserId()).getData();
        UserVO userVO = new UserVO();
        userVO.setUserId(data.getId());
        userVO.setUsername(data.getUsername());
        userVO.setRealName(data.getRealName());

        List<OrderItem> list = orderItemService.getOrderItemListByOrderId(id);
        List<OrderItemVO> orderItemVOList = list.stream()
                .map(item -> {
                    OrderItemVO vo = new OrderItemVO();
                    vo.setProductId(item.getProductId());
                    vo.setProductName(item.getProductName());
                    vo.setBuyNum(item.getBuyNum());
                    vo.setPrice(item.getPrice());
                    return vo;
                }).collect(Collectors.toList());

        OrderVO orderVO = new OrderVO();
        orderVO.setOrderId(order.getId());
        orderVO.setItemList(orderItemVOList);
        orderVO.setCreateTime(order.getCreateTime());
        orderVO.setUserInfo(userVO);

        return orderVO;
    }

    @Override
    public List<OrderVO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        if (orders == null) {
            return Collections.emptyList();
        }
        List<OrderVO> orderVOList = new ArrayList<OrderVO>();
        for (Order order : orders){
            OrderVO orderVO = new OrderVO();
            Result<User> userById = userFeignApi.getUserById(order.getUserId());
            UserVO userVO = new UserVO();
            orderVO.setUserInfo(userVO);
            orderVO.setCreateTime(order.getCreateTime());
            orderVO.setOrderId(order.getId());
            List<OrderItem> itemList = orderItemService.getOrderItemListByOrderId(order.getId());
            ArrayList<OrderItemVO> orderItemVOS = new ArrayList<>();
            for(OrderItem item : itemList){
                OrderItemVO orderItemVO = new OrderItemVO();
                orderItemVO.setBuyNum(item.getBuyNum());
                orderItemVO.setProductName(item.getProductName());
                orderItemVO.setProductId(item.getProductId());
                orderItemVOS.add(orderItemVO);
            }
            orderVO.setItemList(orderItemVOS);
            orderVOList.add(orderVO);
        }

        return orderVOList;
    }

    @Override
    @Transactional
    public void deleteOrder(Integer id) {
        Order order = orderRepository.findById(id).get();
        orderItemService.deleteAllOrderItemsByOrderId(order.getId());
        orderRepository.delete(order);
    }
    @Override
    public Page<OrderVO> getOrderPage(int page, int size) {
        // 1. 参数校验：page 可为 0 或正数，size 至少为 1
        if (page < 0) page = 0;
        if (size < 1) size = 10;

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findAll(pageable);

        // 2. 将 Page<Order> 转换为 Page<OrderVO>
        return orderPage.map(order -> {
            // 2.1 获取用户信息（Feign 调用）
            Result<User> userResult = userFeignApi.getUserById(order.getUserId());
            UserVO userVO = new UserVO();
            if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                User user = userResult.getData();
                userVO.setUserId(user.getId());
                userVO.setUsername(user.getUsername());
                userVO.setRealName(user.getRealName());
            }

            // 2.2 获取订单项列表
            List<OrderItem> itemList = orderItemService.getOrderItemListByOrderId(order.getId());
            List<OrderItemVO> orderItemVOList = itemList.stream()
                    .map(item -> {
                        OrderItemVO vo = new OrderItemVO();
                        vo.setProductId(item.getProductId());
                        vo.setProductName(item.getProductName());
                        vo.setBuyNum(item.getBuyNum());
                        vo.setPrice(item.getPrice());
                        return vo;
                    })
                    .collect(Collectors.toList());

            // 2.3 构建 OrderVO
            OrderVO orderVO = new OrderVO();
            orderVO.setOrderId(order.getId());
            orderVO.setCreateTime(order.getCreateTime());
            orderVO.setUserInfo(userVO);
            orderVO.setItemList(orderItemVOList);
            return orderVO;
        });
    }


    @Override
    public Page<OrderVO> getOrderPage(int page, int size, Integer userId) {
        // 参数校验
        if (page < 0) page = 0;
        if (size < 1) size = 10;

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage;

        // 根据 userId 是否为空，选择不同的查询方式
        if (userId == null) {
            orderPage = orderRepository.findAll(pageable);
        } else {
            // 先校验用户是否存在（可选）
            Result<User> userResult = userFeignApi.getUserById(userId);
            if (userResult == null || userResult.getCode() != 200 || userResult.getData() == null) {
                throw new RuntimeException("用户不存在，userId: " + userId);
            }
            orderPage = orderRepository.findByUserId(userId, pageable);
        }

        // 将 Page<Order> 转换为 Page<OrderVO>
        return orderPage.map(order -> {
            // 获取用户信息
            Result<User> userResult = userFeignApi.getUserById(order.getUserId());
            UserVO userVO = new UserVO();
            if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                User user = userResult.getData();
                userVO.setUserId(user.getId());
                userVO.setUsername(user.getUsername());
                userVO.setRealName(user.getRealName());
            }

            // 获取订单项列表
            List<OrderItem> itemList = orderItemService.getOrderItemListByOrderId(order.getId());
            List<OrderItemVO> orderItemVOList = itemList.stream()
                    .map(item -> {
                        OrderItemVO vo = new OrderItemVO();
                        vo.setProductId(item.getProductId());
                        vo.setProductName(item.getProductName());
                        vo.setBuyNum(item.getBuyNum());
                        vo.setPrice(item.getPrice());
                        return vo;
                    })
                    .collect(Collectors.toList());

            // 构建 OrderVO
            OrderVO orderVO = new OrderVO();
            orderVO.setOrderId(order.getId());
            orderVO.setCreateTime(order.getCreateTime());
            orderVO.setUserInfo(userVO);
            orderVO.setItemList(orderItemVOList);
            return orderVO;
        });
    }

}
