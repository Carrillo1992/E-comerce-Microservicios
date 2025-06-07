package com.dcarrillo.ecomerce.orderservice.controller;


import aj.org.objectweb.asm.commons.TryCatchBlockSorter;
import com.dcarrillo.ecomerce.orderservice.dto.request.CreateOrderRequestDTO;
import com.dcarrillo.ecomerce.orderservice.dto.response.OrderResponseDTO;
import com.dcarrillo.ecomerce.orderservice.security.UserPrincipal;
import com.dcarrillo.ecomerce.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping()
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequestDTO requestDTO,
                                                        Authentication authentication){

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        String email = userPrincipal.getUsername();
        OrderResponseDTO order = null;
        try {
            order =  service.createOrder(requestDTO, userId, email);
        }catch (HttpClientErrorException.Forbidden e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RestClientException | NullPointerException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<?> obtainMyOrders(Authentication authentication, Pageable pageable){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Page<OrderResponseDTO> dtoPage = null;
        try {
            dtoPage = service.obtainOrderByUser(userPrincipal.getUserId(), pageable);
        }catch (HttpClientErrorException e){
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("{id}")
    public ResponseEntity<OrderResponseDTO> orderById(Authentication authentication,@PathVariable("id") Long orderId){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<OrderResponseDTO> dto= service.obtainOrderByIdForUser(orderId, userPrincipal.getUserId());
        return dto
                .map(ResponseEntity::ok)
                .orElseGet(()-> ResponseEntity.notFound().build());
    }
}
