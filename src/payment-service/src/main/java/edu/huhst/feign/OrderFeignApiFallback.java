package edu.huhst.feign;

import edu.huhst.dto.Result;
import edu.huhst.vo.OrderVO;
import org.springframework.stereotype.Component;

@Component
public class OrderFeignApiFallback implements OrderFeignApi{
    @Override
    public Result<OrderVO> getOrderById(Integer id) {
        return null;
    }
}
