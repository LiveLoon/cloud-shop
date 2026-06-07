package edu.huhst.service;

import edu.huhst.domain.User;
import edu.huhst.dto.RegisterRequestDTO;
import edu.huhst.dto.Result;
import edu.huhst.dto.UserInfoDto;
import edu.huhst.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserRepository userRepository;  // 改名以符合 JPA 风格

    @Override
    @Transactional
    public boolean updateScore(Integer userId, Integer targetScore) {
        Optional<User> optional = userRepository.findById(userId);
        if (!optional.isPresent()) {
            return false;
        }
        User user = optional.get();
        user.setScore(targetScore);
        userRepository.save(user);   // JPA 的保存方法会自动判断是插入还是更新
        return true;
    }

    @Override
    public User login(String username, String plainPassword) {
        User user = userRepository.findByUsername(username);  // 使用 JPA 方法命名规则
        if (user == null) {
            return null; // 用户不存在
        }
        // 明文密码比较（按原始需求）
        if (!plainPassword.equals(user.getPassword())) {
            return null; // 密码错误
        }
        // 返回用户前清除密码字段（敏感信息）
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public User getUserById(Integer id) {
        Optional<User> user  = userRepository.findById(id);
        return user.get();
    }

    @Override
    @Transactional
    public User register(RegisterRequestDTO registerRequestDTO) {
        User user = new User();
        user.setUsername(registerRequestDTO.getUsername());
        user.setPassword(registerRequestDTO.getPassword());
        user.setScore(0);

        User byUsername = userRepository.findByUsername(registerRequestDTO.getUsername());
        if (byUsername!=null){
            throw new RuntimeException("User already exists");
        }
        User save = userRepository.save(user);
        return save;
    }


    @Override
    public Page<User> getUsersPage(int page, int size, String keyword) {
        // 参数校验
        if (page < 0) page = 0;
        if (size < 1) size = 10;

        Pageable pageable = PageRequest.of(page, size);

        // 关键词为空时查询全部，否则模糊搜索（用户名或真实姓名）
        if (!StringUtils.hasText(keyword)) {
            return userRepository.findAll(pageable);
        } else {
            return userRepository.findByUsernameContainingOrRealNameContaining(keyword, keyword, pageable);
        }
    }

    @Override
    @Transactional
    public UserInfoDto updateUserInfo(UserInfoDto userInfoDto) {
        // 1. 校验 ID 是否存在
        if (userInfoDto.getId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        User user = userRepository.findById(userInfoDto.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + userInfoDto.getId()));

        // 2. 选择性更新用户名（需检查唯一性）
        if (userInfoDto.getUsername() != null && !userInfoDto.getUsername().trim().isEmpty()) {
            User existing = userRepository.findByUsername(userInfoDto.getUsername());
            if (existing != null && !existing.getId().equals(user.getId())) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(userInfoDto.getUsername());
        }

        // 3. 更新真实姓名
        if (userInfoDto.getRealName() != null) {
            user.setRealName(userInfoDto.getRealName());
        }

        // 4. 更新密码（若提供）
        if (userInfoDto.getPassword() != null && !userInfoDto.getPassword().trim().isEmpty()) {
            user.setPassword(userInfoDto.getPassword());
        }

        // 注意：积分（score）单独通过 /updateScore 接口更新，此处忽略请求中的 score 字段

        // 5. 保存并返回
        User savedUser = userRepository.save(user);

        UserInfoDto resultDto = new UserInfoDto();
        resultDto.setId(savedUser.getId());
        resultDto.setUsername(savedUser.getUsername());
        resultDto.setRealName(savedUser.getRealName());
        resultDto.setScore(savedUser.getScore());
        // 密码不返回
        return resultDto;
    }
}