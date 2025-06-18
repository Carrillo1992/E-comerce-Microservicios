package com.dcarrillo.ecomerce.cartservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddItemToCartRequestDTO {

    private Long productId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
