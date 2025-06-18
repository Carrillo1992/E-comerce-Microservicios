package com.dcarrillo.ecomerce.userservice.service;


import com.dcarrillo.ecomerce.userservice.dto.UserRegisterDTO;
import com.dcarrillo.ecomerce.userservice.entity.Address;
import com.dcarrillo.ecomerce.userservice.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User userRegister(UserRegisterDTO userRegisterDTO);

    Optional<User> findByEmail(String email);

    List<User> findALl();

    List<Address> getUserAddresses(Long userId);

    Address addAddressToUser(String email, Address newAddress);

    void deleteUserAddress(Long addressId);
}
