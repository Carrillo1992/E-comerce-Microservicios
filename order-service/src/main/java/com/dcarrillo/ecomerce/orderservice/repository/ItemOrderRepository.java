package com.dcarrillo.ecomerce.orderservice.repository;

import com.dcarrillo.ecomerce.orderservice.entity.ItemOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemOrderRepository extends JpaRepository<ItemOrder, Long> {
}
