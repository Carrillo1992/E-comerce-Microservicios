package com.dcarrillo.ecomerce.orderservice.service;

import com.dcarrillo.ecomerce.orderservice.config.RabbitConfig;
import com.dcarrillo.ecomerce.orderservice.dto.ProductForOrderDTO;
import com.dcarrillo.ecomerce.orderservice.dto.request.CreateOrderRequestDTO;
import com.dcarrillo.ecomerce.orderservice.dto.request.ItemOrderRequestDTO;
import com.dcarrillo.ecomerce.orderservice.dto.response.OrderResponseDTO;
import com.dcarrillo.ecomerce.orderservice.entity.Order;
import com.dcarrillo.ecomerce.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    private CreateOrderRequestDTO requestDTO;
    private ProductForOrderDTO productInfo;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(orderService, "URL", "http://fake-product-service/api/v1/products");

        ItemOrderRequestDTO itemRequest = new ItemOrderRequestDTO();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        requestDTO = new CreateOrderRequestDTO();
        requestDTO.setShippingAddress("Calle Falsa 123");
        requestDTO.setItems(List.of(itemRequest));

        productInfo = new ProductForOrderDTO();
        productInfo.setId(1L);
        productInfo.setName("Test Product");
        productInfo.setPrice(new BigDecimal("10.00"));
        productInfo.setStock(10);
    }

    @Test
    @DisplayName("Debería crear un pedido, guardar en BD y enviar evento cuando el producto existe y hay stock")
    void testCreateOrder_Success() {
        String productUrl = "http://fake-product-service/api/v1/products/1";
        given(restTemplate.getForObject(eq(productUrl), eq(ProductForOrderDTO.class))).willReturn(productInfo);

        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order orderToSave = invocation.getArgument(0);
            orderToSave.setId(1L);
            return orderToSave;
        });

        OrderResponseDTO createdOrder = orderService.createOrder(requestDTO, 100L, "test@user.com");

        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getUserId()).isEqualTo(100L);
        assertThat(createdOrder.getTotal()).isEqualTo(new BigDecimal("20.00"));
        assertThat(createdOrder.getItems()).hasSize(1);
        assertThat(createdOrder.getItems().get(0).getProductName()).isEqualTo("Test Product");

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.EXCHANGE_NAME),
                eq(RabbitConfig.ROUTING_KEY_ORDER_CREATED),
                any(Object.class)
        );
    }

    @Test
    @DisplayName("Debería lanzar una excepción al crear un pedido si no hay suficiente stock")
    void testCreateOrder_InsufficientStock_ThrowsException() {
        productInfo.setStock(1);
        String productUrl = "http://fake-product-service/api/v1/products/1";
        given(restTemplate.getForObject(eq(productUrl), eq(ProductForOrderDTO.class))).willReturn(productInfo);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(requestDTO, 100L, "test@user.com");
        });

        assertThat(exception.getMessage()).contains("Stock insuficiente");
    }
}

