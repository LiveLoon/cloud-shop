package edu.huhst.repository;

import edu.huhst.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    // 方法一：使用 JPA 方法命名规则（推荐）
    List<OrderItem> findByOrderId(Integer orderId);

    // 方法二：若需保留原方法名 selectByOrderId，可使用 @Query
    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId")
    List<OrderItem> selectByOrderId(@Param("orderId") Integer orderId);

    void deleteByOrderId(Integer orderId);
}