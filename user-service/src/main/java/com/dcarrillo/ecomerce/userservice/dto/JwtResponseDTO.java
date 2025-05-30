package com.dcarrillo.ecomerce.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponseDTO {

    private String accessToken;
    private final String tokenType = "Bearer";
    private Long userId;
    private String email;
    private List<String> roles;

}
