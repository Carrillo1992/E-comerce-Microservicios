package com.dcarrillo.ecomerce.orderservice.repository;

import com.dcarrillo.ecomerce.orderservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);

    Optional<Order> findByIdAndUserId(Long id , Long userId);

    List<Order> findByUserId(Long userId);
}
