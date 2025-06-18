package com.dcarrillo.ecomerce.cartservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {
    private Long userId;
    private List<CartItemDTO> items;
    private String shippingAddress;
}
