package com.dcarrillo.ecomerce.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemOrderRequestDTO {

    @NotNull
    private Long productId;
    @NotNull
    @Min(1)
    private Integer quantity;

}
