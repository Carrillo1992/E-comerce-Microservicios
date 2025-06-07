package com.dcarrillo.ecomerce.orderservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemEventDTO {
    private Long productId;
    private Integer quantity;
}
