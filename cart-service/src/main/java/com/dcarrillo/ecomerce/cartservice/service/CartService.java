package com.dcarrillo.ecomerce.cartservice.service;

import com.dcarrillo.ecomerce.cartservice.dto.AddItemToCartRequestDTO;
import com.dcarrillo.ecomerce.cartservice.dto.ShoppingCartResponseDTO;

public interface CartService {

    ShoppingCartResponseDTO getCartForUser(Long userId);

    ShoppingCartResponseDTO addItemToCart(Long userId, AddItemToCartRequestDTO itemDTO);

    ShoppingCartResponseDTO removeItemtoCart(Long userId, Long productId);

    void clearCart(Long userId);

    void processCheckout(Long userId);

    void removeProductFromAllCarts(Long productId);
}
