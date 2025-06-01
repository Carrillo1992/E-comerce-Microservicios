package com.dcarrillo.ecomerce.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateOrderRequestDTO {
    @NotBlank
    private String shippingAddress;

    @Valid
    private List<ItemOrderRequestDTO> items;
}
