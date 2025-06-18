package com.dcarrillo.ecomerce.cartservice.controller;

import com.dcarrillo.ecomerce.cartservice.dto.AddItemToCartRequestDTO;
import com.dcarrillo.ecomerce.cartservice.dto.ShoppingCartResponseDTO;
import com.dcarrillo.ecomerce.cartservice.security.UserPrincipal;
import com.dcarrillo.ecomerce.cartservice.service.CartServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartServiceImpl service;

    public CartController(CartServiceImpl service) {
        this.service = service;
    }

    @GetMapping()
    public ResponseEntity<?> getCart(Authentication authentication){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        ShoppingCartResponseDTO shoppingCartResponseDTO;
        try{
            shoppingCartResponseDTO = service.getCartForUser(userId);
        }catch (HttpClientErrorException.Forbidden e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (RestClientException e){
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
        }catch (NullPointerException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return ResponseEntity.ok().body(shoppingCartResponseDTO);
    }

    @PostMapping("/items")
    public ResponseEntity<?> addItem (@Valid @RequestBody AddItemToCartRequestDTO itemDTO, Authentication authentication){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        ShoppingCartResponseDTO shoppingCartResponseDTO;
        try{
            shoppingCartResponseDTO= service.addItemToCart(userId, itemDTO);
        }catch (HttpClientErrorException.Forbidden e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (RestClientException e){
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
        }catch (NullPointerException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(shoppingCartResponseDTO);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<?> deleteItem (@Valid @PathVariable Long productId, Authentication authentication){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        ShoppingCartResponseDTO shoppingCartResponseDTO;
        try {
            shoppingCartResponseDTO = service.removeItemtoCart(userId,productId);
        }catch (HttpClientErrorException.Forbidden e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (RestClientException e){
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
        }catch (NullPointerException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return ResponseEntity.ok().body(shoppingCartResponseDTO);
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteAll (Authentication authentication){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        try {
            service.clearCart(userId);
        }catch (HttpClientErrorException.Forbidden e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (RestClientException e){
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
        }catch (NullPointerException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("items/checkout")
    public ResponseEntity<?> checkout(Authentication authentication){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        ShoppingCartResponseDTO shoppingCartResponseDTO;
        try {
            service.processCheckout(userId);
        }catch (HttpClientErrorException.Forbidden e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (RestClientException e){
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getMessage());
        }catch (NullPointerException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return ResponseEntity.accepted().build();
    }


}
