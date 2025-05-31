package com.dcarrillo.ecomerce.productservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;
    @Positive
    private Integer stock;
    @NotNull
    private Long categoryId;
}
