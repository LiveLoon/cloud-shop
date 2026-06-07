package edu.huhst.feign;

import edu.huhst.domain.User;
import edu.huhst.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(value = "user-service",path = "/api/user",fallback = UserFeignFallback.class)
public interface UserFeignApi {
    @GetMapping("/getUserById/{id}")
    public Result<User> getUserById(@PathVariable Integer id);
    @PutMapping("/updateScore/{id}/{score}")
    public Result<Void> updateScore (@PathVariable Integer id,@PathVariable Integer score);
}
