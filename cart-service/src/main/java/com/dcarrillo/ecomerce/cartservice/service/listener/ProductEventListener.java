package com.dcarrillo.ecomerce.cartservice.service.listener;

import com.dcarrillo.ecomerce.cartservice.dto.event.DeletedProductDTO;
import com.dcarrillo.ecomerce.cartservice.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ProductEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ProductEventListener.class);
    private final CartService cartService;

    public ProductEventListener(CartService cartService) {
        this.cartService = cartService;
    }

    @RabbitListener(queues = "product.deleted.cart.queue")
    public void handleProductDeletedEvent(DeletedProductDTO event){
        logger.info("Evento ProductDeletedEvent recivido para el productId {}", event.getId());
        try {
            cartService.removeProductFromAllCarts(event.getId());
            logger.info("Items del producto {} eliminados de todos los carritos");
        }catch (Exception e){
            logger.error("Error al procesar el ProductDeletedEvent para el productId{}", event.getId());
        }
    }
}
