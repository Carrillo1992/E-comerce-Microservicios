package com.dcarrillo.ecomerce.productservice.repository;


import com.dcarrillo.ecomerce.productservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

}
