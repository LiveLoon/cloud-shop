package edu.huhst.feign;


import edu.huhst.domain.Product;
import edu.huhst.dto.Result;
import org.springframework.stereotype.Component;

@Component
public class ProductFeignFallback implements ProductFeignApi{
    @Override
    public Result<Product> getProductById(Integer id) {
        return null;
    }
}
