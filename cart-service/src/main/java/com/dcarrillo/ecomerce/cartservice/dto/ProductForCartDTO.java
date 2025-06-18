package com.dcarrillo.ecomerce.cartservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductForCartDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
}
