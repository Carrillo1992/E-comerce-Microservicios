package com.dcarrillo.ecomerce.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProductForOrderDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
}
