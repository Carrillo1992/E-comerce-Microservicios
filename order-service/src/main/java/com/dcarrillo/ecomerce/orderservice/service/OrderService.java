package com.dcarrillo.ecomerce.orderservice.service;


import com.dcarrillo.ecomerce.orderservice.dto.CreateOrderRequestDTO;
import com.dcarrillo.ecomerce.orderservice.dto.OrderResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderService {
    OrderResponseDTO createOrder(CreateOrderRequestDTO createOrderRequestDTO,
                                 long userId, String userEmail);
    Page<OrderResponseDTO> obtainOrderByUser(Long userId, Pageable pageable);

    Optional<OrderResponseDTO> obtainOrderByIdForUser(Long orderId, Long userId);
}
