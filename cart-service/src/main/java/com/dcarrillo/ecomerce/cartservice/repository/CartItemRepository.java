package com.dcarrillo.ecomerce.cartservice.repository;

import com.dcarrillo.ecomerce.cartservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {


    Optional<CartItem> findByShoppingCartUserIdAndProductId(Long userId, Long productId);

    void deleteAllByProductId(Long productId);
}
