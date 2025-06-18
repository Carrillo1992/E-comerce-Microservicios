package com.dcarrillo.ecomerce.userservice.dto;

import com.dcarrillo.ecomerce.userservice.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {

    private long id;
    private String name;
    private String email;
    private List<Address> addresses;
    private List<String> roles;

}
