package edu.huhst.feign;

import edu.huhst.dto.Result;
import edu.huhst.vo.OrderVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(value = "order-service",path = "/api/order",fallback = OrderFeignApiFallback.class)
public interface OrderFeignApi {
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderById (@PathVariable Integer id);
}
