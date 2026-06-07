package edu.huhst.repository;

import edu.huhst.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // 方法1：使用 JPA 方法命名规则（推荐）
    User findByUsername(String username);

    // 方法2：保留原方法名 selectByUsername，使用 @Query 实现
    @Query("SELECT u FROM User u WHERE u.username = :username")
    User selectByUsername(@Param("username") String username);

    // 新增：根据关键字（用户名或真实姓名）分页模糊查询
    Page<User> findByUsernameContainingOrRealNameContaining(String usernameKeyword, String realNameKeyword, Pageable pageable);
}