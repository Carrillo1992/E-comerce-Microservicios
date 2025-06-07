package com.dcarrillo.ecomerce.orderservice.service;

import com.dcarrillo.ecomerce.orderservice.config.RabbitConfig;
import com.dcarrillo.ecomerce.orderservice.dto.*;
import com.dcarrillo.ecomerce.orderservice.dto.event.CreateOrderEventDTO;
import com.dcarrillo.ecomerce.orderservice.dto.event.ItemEventDTO;
import com.dcarrillo.ecomerce.orderservice.dto.request.CreateOrderRequestDTO;
import com.dcarrillo.ecomerce.orderservice.dto.request.ItemOrderRequestDTO;
import com.dcarrillo.ecomerce.orderservice.dto.response.ItemOrderResponseDTO;
import com.dcarrillo.ecomerce.orderservice.dto.response.OrderResponseDTO;
import com.dcarrillo.ecomerce.orderservice.entity.ItemOrder;
import com.dcarrillo.ecomerce.orderservice.entity.Order;
import com.dcarrillo.ecomerce.orderservice.entity.OrderStatus;
import com.dcarrillo.ecomerce.orderservice.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final String exhangeName = RabbitConfig.EXCHANGE_NAME;
    private final String routingKey = RabbitConfig.ROUTING_KEY_ORDER_CREATED;
    @Value("${product.service.url}")
    private String URL;

    private final OrderRepository repository;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;


    public OrderServiceImpl(OrderRepository repository, RestTemplate restTemplate, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public OrderResponseDTO createOrder(CreateOrderRequestDTO createOrderRequestDTO, long userId, String userEmail) {
        if (createOrderRequestDTO.getItems() == null || createOrderRequestDTO.getItems().isEmpty()){
            throw new IllegalArgumentException("La lista de items del pedido no puede estar vacia!");
        }

        List<ItemOrder> orderList = new ArrayList<>();
        BigDecimal totalOrder = BigDecimal.ZERO;
        for (ItemOrderRequestDTO itemRequest : createOrderRequestDTO.getItems()) {
            String urlProduct = URL + "/" + itemRequest.getProductId();
            ProductForOrderDTO productInfo;
            try {
                productInfo = restTemplate.getForObject(urlProduct, ProductForOrderDTO.class);
            } catch (HttpClientErrorException.Forbidden e) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Producto "+itemRequest.getProductId()+" no encontrado");
            } catch (RestClientException e) {
                throw new RestClientException("Error al comunicar el servicio. " + e);
            }catch (NullPointerException e){
                throw new RuntimeException("No se pudo obtener la informacion para el producto.");
            }
            if (itemRequest.getQuantity() > productInfo.getStock()) {
                throw new HttpClientErrorException(HttpStatus.INSUFFICIENT_STORAGE,"Stock insuficiente para el producto: " + productInfo.getName());
            }

            ItemOrder order = new ItemOrder();
            order.setIdProduct(itemRequest.getProductId());
            order.setProductName(productInfo.getName());
            order.setQuantity(itemRequest.getQuantity());
            order.setUnitPrice(productInfo.getPrice());
            BigDecimal subtotal = BigDecimal.ZERO;
            if (order.getUnitPrice() != null && order.getQuantity() != null){
                subtotal = order.getUnitPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
            }
            totalOrder = totalOrder.add(subtotal);
            order.setSubtotal(subtotal);
            orderList.add(order);
        }
        Order order = new Order();
        order.setUserId(userId);
        order.setShippingAddress(createOrderRequestDTO.getShippingAddress());
        order.setOrderStatus(OrderStatus.PENDIENTE);
        order.setTotalOrder(totalOrder);

        for (ItemOrder item : orderList){
            item.setOrder(order);
        }
        order.setItemOrder(orderList);

        Order orderSave = repository.save(order);
        try {
            CreateOrderEventDTO event = new CreateOrderEventDTO();
            event.setUserid(orderSave.getUserId());
            event.setOrderId(orderSave.getId());

            List<ItemEventDTO> itemEventDTOS = orderSave
                    .getItemOrder().stream()
                    .map(itemDb -> new ItemEventDTO(itemDb.getIdProduct(), itemDb.getQuantity()))
                    .collect(Collectors.toList());

            event.setItems(itemEventDTOS);
            this.rabbitTemplate.convertAndSend(exhangeName,routingKey,event);
            System.out.println("Pedido enviado correctamente para el orderId= " + event.getOrderId());
        }catch (Exception e){
            System.out.println("Error al enviar el pedido con orderId= "+ orderSave.getId() + e);
        }


        return convertOrderToDTO(orderSave) ;

    }

    @Override
    public Page<OrderResponseDTO> obtainOrderByUser(Long userId, Pageable pageable) {
        Page<Order> orderPage = repository.findByUserIdOrderByOrderDateDesc(userId, pageable);
        return orderPage.map(this::convertOrderToDTO);


    }

    @Override
    public Optional<OrderResponseDTO> obtainOrderByIdForUser(Long orderId, Long userId) {
        Order order = repository.findByIdAndUserId(orderId,userId).orElseThrow(()->new HttpClientErrorException(HttpStatus.NOT_FOUND,"Orden o Usuario no encontrado"));
        return Optional.of(convertOrderToDTO(order));
    }

    private  OrderResponseDTO convertOrderToDTO(Order order){
        List<ItemOrderResponseDTO> itemsDTO = order.getItemOrder().stream()
                .map(item -> new ItemOrderResponseDTO(
                        item.getIdProduct(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());
        return new OrderResponseDTO(
                order.getId(),
                order.getUserId(),
                order.getOrderDate(),
                order.getOrderStatus() != null ?order.getOrderStatus().name():null,
                order.getShippingAddress(),
                order.getTotalOrder(),
                itemsDTO
        );
    }
}
