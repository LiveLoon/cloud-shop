package edu.huhst.service;

import edu.huhst.domain.User;
import edu.huhst.dto.RegisterRequestDTO;
import edu.huhst.dto.UserInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    /**
     * Update user's score to target value.
     * @param userId user ID
     * @param targetScore new score value
     * @return true if update successful, false if user not found
     */
    boolean updateScore(Integer userId, Integer targetScore);

    /**
     * Login by username and plain password.
     * @param username login account
     * @param plainPassword plain text password
     * @return User object if credentials valid, null otherwise
     */
    User login(String username, String plainPassword);

    User getUserById(Integer id);

    User register(RegisterRequestDTO registerRequestDTO);

    UserInfoDto updateUserInfo(UserInfoDto userInfoDto);

    // 新增：分页查询用户（支持关键字模糊搜索）
    Page<User> getUsersPage(int page, int size, String keyword);
}
