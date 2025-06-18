package com.dcarrillo.ecomerce.userservice.service;

import com.dcarrillo.ecomerce.userservice.dto.UserRegisterDTO;
import com.dcarrillo.ecomerce.userservice.entity.Address;
import com.dcarrillo.ecomerce.userservice.entity.Role;
import com.dcarrillo.ecomerce.userservice.entity.User;
import com.dcarrillo.ecomerce.userservice.repository.AddressRepository;
import com.dcarrillo.ecomerce.userservice.repository.RoleRepository;
import com.dcarrillo.ecomerce.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository, AddressRepository addressRepository,
                           @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User userRegister(UserRegisterDTO userRegisterDTO) {
        if (userRepository.existsByEmail(userRegisterDTO.getEmail())){
            throw new RuntimeException("Error: Email ya registrado.");
        }
        String hashedPassword = passwordEncoder.encode(userRegisterDTO.getPassword());

        User newUser = new User();
        newUser.setName(userRegisterDTO.getName());
        newUser.setEmail(userRegisterDTO.getEmail());
        newUser.setPasswordHash(hashedPassword);

        Role userRole = roleRepository.findByName(("ROLE_USER"))
                .orElseThrow(()->new RuntimeException("Error: Rol por defecto ROLE_USER no encontrado"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        newUser.setRoles(roles);

        return userRepository.save(newUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> findALl() {
        return userRepository.findAll();
    }

    @Override
    public List<Address> getUserAddresses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UsernameNotFoundException("Usuario no encontrado"));
        return user.getAddresses();
    }

    @Override
    public Address addAddressToUser(String email, Address newAddress) {
        User user = userRepository.findByEmail(email).
                orElseThrow(()-> new UsernameNotFoundException("Usuario no encontrado"));

        newAddress.setUser(user);
        user.getAddresses().add(newAddress);
        userRepository.save(user);

        return newAddress;
    }

    @Override
    public void deleteUserAddress(Long addressId) {
        if (addressRepository.findById(addressId).isEmpty()){
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND,"Producto no encontrado");
        }
        addressRepository.deleteById(addressId);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).
                orElseThrow(()->
                        new UsernameNotFoundException("Usuario no encontrado: " + email));

        Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );
    }
}