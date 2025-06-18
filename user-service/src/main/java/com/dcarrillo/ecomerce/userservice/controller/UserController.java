package com.dcarrillo.ecomerce.userservice.controller;

import com.dcarrillo.ecomerce.userservice.dto.UserProfileDTO;
import com.dcarrillo.ecomerce.userservice.entity.Address;
import com.dcarrillo.ecomerce.userservice.entity.Role;
import com.dcarrillo.ecomerce.userservice.entity.User;
import com.dcarrillo.ecomerce.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication){
        if (authentication == null ||!authentication.isAuthenticated()){
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado");
        }
        String userEmail = authentication.getName();
        User user = userService.findByEmail(userEmail)
                .orElseThrow(()-> new UsernameNotFoundException("Usuario autenticado no encontrado"));

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        UserProfileDTO userProfileDTO = new UserProfileDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAddresses(),
                roles
        );
        return ResponseEntity.ok(userProfileDTO);
    }

    @GetMapping("/me/addresses")
    public ResponseEntity<?> getUserAddresses(Authentication authentication){
        if (authentication == null ||!authentication.isAuthenticated()){
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado");
        }
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(()-> new UsernameNotFoundException("Usuario autenticado no encontrado"));

        return  ResponseEntity.ok().body(user.getAddresses());
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<?> addUserAddress(@RequestBody @Valid Address newAddress, Authentication authentication){
        if (authentication == null ||!authentication.isAuthenticated()){
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado");
        }
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(()-> new UsernameNotFoundException("Usuario autenticado no encontrado"));
        userService.addAddressToUser(user.getEmail(), newAddress);
        return ResponseEntity.ok().body(user.getAddresses());
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId, Authentication authentication ){
        try {
            userService.deleteUserAddress(addressId);
        }catch (HttpClientErrorException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return ResponseEntity.noContent().build();
    }

}
