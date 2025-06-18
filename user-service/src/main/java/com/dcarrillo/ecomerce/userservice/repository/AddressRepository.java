package com.dcarrillo.ecomerce.userservice.repository;

import com.dcarrillo.ecomerce.userservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findById(Long addressId);

    void removeAddressById(Long id);
}
