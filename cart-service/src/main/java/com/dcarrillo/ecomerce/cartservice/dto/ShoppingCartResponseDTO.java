package com.dcarrillo.ecomerce.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class ShoppingCartResponseDTO {
    private Long userId;
    private List<CartItemResponseDTO> items;
    private BigDecimal total;
}
