package edu.huhst.controller;

import edu.huhst.dto.RegisterRequestDTO;
import edu.huhst.dto.Result;
import edu.huhst.domain.User;
import edu.huhst.dto.LoginRequestDTO;
import edu.huhst.dto.UserInfoDto;
import edu.huhst.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * PUT /api/user/updateScore/{id}/{score}
     * Update user's score to the given target value.
     */

    @PutMapping("/updateScore/{id}/{score}")
    public Result<Void> updateScore (@PathVariable Integer id,@PathVariable Integer score){
        boolean success = userService.updateScore(id, score);
        if (!success) {
            return Result.error("用户不存在，更新积分失败");
        }
        return Result.success(null);
    }

    /**
     * POST /api/user/register
     * Register a new user with username and plain password.
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequestDTO registerRequestDTO){
        String username = registerRequestDTO.getUsername();
        String password = registerRequestDTO.getPassword();

        // 参数校验
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return Result.error("username or password can be void.");
        }
       try {
           User newUser = userService.register(registerRequestDTO);
           return Result.success(newUser);
       }catch (RuntimeException e){
           return Result.error(e.getMessage());
       }
    }

    /**
     * POST /api/user/login
     * Login with username and plain password.
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody LoginRequestDTO loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return Result.error("用户名或密码不能为空");
        }

        User user = userService.login(username, password);
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        return Result.success(user);
    }

    @GetMapping("/getUserById/{id}")
    public Result<User> getUserById(@PathVariable Integer id){
        try {
            User user = userService.getUserById(id);
            return Result.success(user);
        }catch (Exception e){
            return Result.error(404,e.getMessage());
        }
    }




    /**
     * 分页查询用户列表
     * GET /api/user/list?page=0&size=10&keyword=
     */
    @GetMapping("/list")
    public Result<Page<User>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "") String keyword) {

        // 参数二次校验
        if (page < 0) page = 0;
        if (size < 1) size = 10;

        Page<User> userPage = userService.getUsersPage(page, size, keyword);

        // 清除密码字段（避免敏感信息泄露）
        userPage.getContent().forEach(user -> user.setPassword(null));

        return Result.success(userPage);
    }


    /**
     * PUT /api/user/update
     * 更新用户信息（用户名、真实姓名、密码）
     */
    @PutMapping("/update")
    public Result<UserInfoDto> updateUserInfo(@RequestBody UserInfoDto userInfoDto){
        try {
            UserInfoDto dto = userService.updateUserInfo(userInfoDto);
            return Result.success(dto);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
