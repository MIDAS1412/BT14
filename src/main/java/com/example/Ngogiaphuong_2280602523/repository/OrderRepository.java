package com.example.Ngogiaphuong_2280602523.repository;

import com.example.Ngogiaphuong_2280602523.model.Order;
import com.example.Ngogiaphuong_2280602523.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByCustomerEmail(String email);
}
