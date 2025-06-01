package com.dcarrillo.ecomerce.orderservice.controller;


import com.dcarrillo.ecomerce.orderservice.dto.CreateOrderRequestDTO;
import com.dcarrillo.ecomerce.orderservice.dto.OrderResponseDTO;
import com.dcarrillo.ecomerce.orderservice.security.UserPrincipal;
import com.dcarrillo.ecomerce.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping()
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody CreateOrderRequestDTO requestDTO,
                                                        Authentication authentication){

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        String email = userPrincipal.getUsername();
        OrderResponseDTO order =  service.createOrder(requestDTO, userId, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> obtainMyOrders(Authentication authentication, Pageable pageable){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Page<OrderResponseDTO> dtoPage = service.obtainOrderByUser(userPrincipal.getUserId(), pageable);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("{id}")
    public ResponseEntity<OrderResponseDTO> orderById(Authentication authentication,@PathVariable Long orderId){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<OrderResponseDTO> dto= service.obtainOrderByIdForUser(orderId, userPrincipal.getUserId());
        return dto
                .map(ResponseEntity::ok)
                .orElseGet(()-> ResponseEntity.notFound().build());
    }




}
