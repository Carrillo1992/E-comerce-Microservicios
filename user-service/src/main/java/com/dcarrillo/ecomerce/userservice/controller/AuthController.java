package com.dcarrillo.ecomerce.userservice.controller;

import com.dcarrillo.ecomerce.userservice.dto.JwtResponseDTO;
import com.dcarrillo.ecomerce.userservice.dto.LoginUserDTO;
import com.dcarrillo.ecomerce.userservice.dto.UserRegisterDTO;
import com.dcarrillo.ecomerce.userservice.entity.Role;
import com.dcarrillo.ecomerce.userservice.entity.User;
import com.dcarrillo.ecomerce.userservice.security.JwtUtils;
import com.dcarrillo.ecomerce.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    UserService userService;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterDTO userRegisterDTO){
        try {
            userService.userRegister(userRegisterDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente!");
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticatedUser(@Valid @RequestBody LoginUserDTO loginUserDTO){
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginUserDTO.getEmail(), loginUserDTO.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails);
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Error al encontrar el usuario"));
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        Long userId = user.getId();
        JwtResponseDTO jwtResponseDTO = new JwtResponseDTO(jwt , userId, userDetails.getUsername(), roles);

        return ResponseEntity.ok(jwtResponseDTO);
    }

    @GetMapping("/list")
    public List<User> list(Long id){
        return userService.findALl();
    }
}
