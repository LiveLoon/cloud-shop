package edu.huhst.feign;

import edu.huhst.domain.Product;
import edu.huhst.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "product-service",path = "/api/product",fallback = ProductFeignFallback.class)
public interface ProductFeignApi {
    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Integer id);
}
