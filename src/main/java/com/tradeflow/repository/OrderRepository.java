package com.tradeflow.repository;

import com.tradeflow.entity.Order;
import com.tradeflow.entity.OrderStatus;
import com.tradeflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserOrderByIdDesc(User user, Pageable pageable);

    List<Order> findByUserAndStatus(User user, OrderStatus status);

    Page<Order> findByUserAndStatusOrderByIdDesc(User user, OrderStatus status, Pageable pageable);
}