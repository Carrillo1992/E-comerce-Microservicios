package com.dcarrillo.ecomerce.orderservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequestDTO {
    @NotBlank
    private String shippingAddress;

    @Valid
    private List<ItemOrderRequestDTO> items;
}
