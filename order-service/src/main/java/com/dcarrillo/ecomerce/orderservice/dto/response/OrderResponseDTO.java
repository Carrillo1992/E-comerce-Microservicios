package com.dcarrillo.ecomerce.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private Long userId;
    private LocalDateTime dateOrder;
    private String orderStatus;
    private String shippingAddress;
    private BigDecimal total;
    private List<ItemOrderResponseDTO> items;
}
