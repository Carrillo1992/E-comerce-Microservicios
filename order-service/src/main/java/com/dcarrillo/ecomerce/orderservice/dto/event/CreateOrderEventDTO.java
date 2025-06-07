package com.dcarrillo.ecomerce.orderservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderEventDTO {
    private Long userid;
    private Long orderId;
    private List<ItemEventDTO> items;
}
