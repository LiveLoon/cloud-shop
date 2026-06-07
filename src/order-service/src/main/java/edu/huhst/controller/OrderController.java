package edu.huhst.controller;

import edu.huhst.dto.Result;
import edu.huhst.dto.OrderCreateDTO;
import edu.huhst.service.OrderService;
import edu.huhst.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private  OrderService orderService;

    /**
     * GET /api/order/{id} - Query order with details
     */
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderById (@PathVariable Integer id){
        OrderVO orderVO = orderService.getOrdersById(id);
        if(orderVO == null){
            return  Result.error("订单不存在");
        }
        return Result.success(orderVO);
    }

    /**
     * POST /api/order/add - Create new order
     */


    @PostMapping("/add")
    public Result<Void> addOrder(@RequestBody OrderCreateDTO createDTO){
        try {
            orderService.createOrder(createDTO);
            return Result.success("订单添加成功",null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/listAll")
    public Result<List<OrderVO>> getAllOrders(){
        List<OrderVO> orderList =  orderService.getAllOrders();

        if (orderList == null || orderList.isEmpty()) {
            return Result.success("暂无订单数据", orderList);
        }
        return Result.success(orderList);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteOrder(@PathVariable Integer id){
        try {
            orderService.deleteOrder(id);
            return Result.success();
        }catch (Exception e){
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询订单列表
     * GET /api/order/list?page=0&size=10
     */
    @GetMapping("/list")
    public Result<Page<OrderVO>> getOrderList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 参数二次校验（防御性）
        if (page < 0) page = 0;
        if (size < 1) size = 10;

        Page<OrderVO> orderPage = orderService.getOrderPage(page, size);
        return Result.success(orderPage);
    }

    /**
     * 分页查询订单列表（支持按用户 ID 过滤）
     * GET /api/order/list?page=0&size=10&userId=6
     */
    @GetMapping("/userList")
    public Result<Page<OrderVO>> getUserOrderList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer userId) {   // 新增 userId 参数

        // 参数校验
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        try {
            Page<OrderVO> orderPage = orderService.getOrderPage(page, size, userId); // 调用新方法
            return Result.success(orderPage);

        }catch (Exception e){
            return  Result.error(404,e.getMessage());
        }

    }
}
