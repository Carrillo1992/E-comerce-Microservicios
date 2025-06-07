package com.dcarrillo.ecomerce.productservice.service.listener;

import com.dcarrillo.ecomerce.productservice.dto.event.CreateOrderEventDTO;
import com.dcarrillo.ecomerce.productservice.dto.event.ItemEventDTO;
import com.dcarrillo.ecomerce.productservice.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);
    private final ProductService productService;

    public OrderEventListener(ProductService productService) {
        this.productService = productService;
    }

    @RabbitListener(queues = "orders.created.events.queue")
    public void handleOrderCreatedEvent(CreateOrderEventDTO event){
        logger.info("Evento CreateOrderEvent recibido para orderId: {}",event.getOrderId());
        try {
                for (ItemEventDTO item : event.getItems()){
                    logger.info("Procesando item: productoId = {}, quantity = {}", item.getProductId(), item.getQuantity());
                    productService.updateStock(item.getProductId(), item.getQuantity());
                }
                logger.info("Stock actualizado para los items del pedido {}",event.getOrderId());
        }catch (Exception e){
            logger.error("Error al procesar el pedido para el orderId {}: {}",event.getOrderId(), e.getMessage());
        }
    }
}