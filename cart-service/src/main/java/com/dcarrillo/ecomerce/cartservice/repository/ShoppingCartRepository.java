package com.dcarrillo.ecomerce.cartservice.repository;

import com.dcarrillo.ecomerce.cartservice.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart,Long> {
    Optional<ShoppingCart> getShoppingCartByUserId(Long userId);
}
