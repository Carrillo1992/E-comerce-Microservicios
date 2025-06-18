package com.dcarrillo.ecomerce.cartservice.service;

import com.dcarrillo.ecomerce.cartservice.dto.*;
import com.dcarrillo.ecomerce.cartservice.entity.CartItem;
import com.dcarrillo.ecomerce.cartservice.entity.ShoppingCart;
import com.dcarrillo.ecomerce.cartservice.repository.CartItemRepository;
import com.dcarrillo.ecomerce.cartservice.repository.ShoppingCartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final RestTemplate restTemplate;

    private final RabbitTemplate rabbitTemplate;

    @Value("${product.service.url}")
    private String URL;

    public CartServiceImpl(ShoppingCartRepository shoppingCartRepository, CartItemRepository cartItemRepository, RestTemplate restTemplate, RabbitTemplate rabbitTemplate) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.cartItemRepository = cartItemRepository;
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public ShoppingCartResponseDTO getCartForUser(Long userId) {
        Optional<ShoppingCart> itemsOptional = shoppingCartRepository.getShoppingCartByUserId(userId);
        if(itemsOptional.isEmpty()){
            return new ShoppingCartResponseDTO(userId, new ArrayList<>() , BigDecimal.ZERO);
        }
        ShoppingCart shoppingCart = itemsOptional.get();
        BigDecimal total = BigDecimal.ZERO;
        List<CartItemResponseDTO> responseItems= new ArrayList<>();

        for (CartItem item : shoppingCart.getItems()){
            try {
                ProductForCartDTO productInfo = getProductInfo(item.getProductId());

                BigDecimal subtotal = productInfo.getPrice().multiply(new BigDecimal( item.getQuantity()));
                total = total.add(subtotal);

                CartItemResponseDTO itemResponseDTO = new CartItemResponseDTO(
                        item.getProductId(),
                        productInfo.getName(),
                        item.getQuantity(),
                        productInfo.getPrice(),
                        subtotal
                );
                responseItems.add(itemResponseDTO);
            }catch (RuntimeException e){
                logger.warn("no se pudo obtener informacion del producto {}", item.getProductId());
            }

        }
        return new ShoppingCartResponseDTO(userId, responseItems, total);
    }

    @Override
    @Transactional
    public ShoppingCartResponseDTO addItemToCart(Long userId, AddItemToCartRequestDTO itemDTO) {

        ShoppingCart shoppingCart= shoppingCartRepository.getShoppingCartByUserId(userId).orElse(new ShoppingCart(userId));

        getProductInfo(itemDTO.getProductId());

        Optional<CartItem> itemOptional = shoppingCart.getItems().stream()
                .filter(item ->item.getProductId().equals(itemDTO.getProductId()))
                .findFirst();

        if (itemOptional.isPresent()){
            CartItem item = itemOptional.get();
            item.setQuantity(itemOptional.get().getQuantity() + itemDTO.getQuantity());
        }else {
            CartItem newItem = new CartItem();
            newItem.setProductId(itemDTO.getProductId());
            newItem.setQuantity(itemDTO.getQuantity());
            newItem.setShoppingCart(shoppingCart);
            shoppingCart.getItems().add(newItem);
        }
        shoppingCart.setUpdateAt(LocalDateTime.now());

        shoppingCartRepository.save(shoppingCart);

        return getCartForUser(userId);
    }

    @Override
    @Transactional
    public ShoppingCartResponseDTO removeItemtoCart(Long userId, Long productId) {

        ShoppingCart shoppingCart = shoppingCartRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("Carrito para el usuario con ID " + userId + " no encontrado."));

            CartItem removeItem = shoppingCart.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Producto con Id " + productId + " no encontrado"));
        shoppingCart.getItems().remove(removeItem);
        if (shoppingCart.getItems().isEmpty()){
            shoppingCartRepository.delete(shoppingCart);
            return new ShoppingCartResponseDTO(userId, new ArrayList<>(), BigDecimal.ZERO);
        }else {
            shoppingCart.setUpdateAt(LocalDateTime.now());
            shoppingCartRepository.save(shoppingCart);
        }
        return getCartForUser(userId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        shoppingCartRepository.findById(userId).ifPresent(shoppingCartRepository::delete);
    }

    @Transactional
    public void processCheckout(Long userId) {
        ShoppingCart shoppingCart = shoppingCartRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("Carrito para el usuario con ID " + userId + " no encontrado."));
        OrderRequestDTO orderRequest = new OrderRequestDTO();
        orderRequest.setUserId(shoppingCart.getUserId());
        shoppingCart.getItems()
                .forEach(cartItem -> {
                    CartItemDTO cartItemDTO = new CartItemDTO();
                    cartItemDTO.setProductId(cartItem.getProductId());
                    cartItemDTO.setQuantity(cartItem.getQuantity());
                    orderRequest.getItems().add(cartItemDTO);
                });

        clearCart(userId);
    }

    @Override
    public void removeProductFromAllCarts(Long productId) {
        if (productId == null){
            return;
        }
        cartItemRepository.deleteAllByProductId(productId);
    }

    private ProductForCartDTO getProductInfo(Long productId) {
        String urlProduct = URL + "/" + productId;
        ProductForCartDTO productInfo;
        try {
            productInfo = restTemplate.getForObject(urlProduct, ProductForCartDTO.class);
        }catch (HttpClientErrorException.Forbidden e) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Producto "+ productId +" no encontrado");
        }catch (RestClientException e) {
            throw new RestClientException("Error al comunicar el servicio. " + e);
        }catch (NullPointerException e){
            throw new RuntimeException("No se pudo obtener la informacion para el producto.");
        }
        return productInfo;
    }
}