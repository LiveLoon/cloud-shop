package edu.huhst.feign;

import edu.huhst.domain.User;
import edu.huhst.dto.Result;
import org.springframework.stereotype.Component;

@Component
public class UserFeignFallback implements UserFeignApi{
    @Override
    public Result<User> getUserById(Integer id) {

        User user = new User();
        return Result.success(user);
    }

    @Override
    public Result<Void> updateScore(Integer id, Integer score) {
        return null;
    }
}
