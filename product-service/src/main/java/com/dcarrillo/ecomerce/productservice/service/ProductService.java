package com.dcarrillo.ecomerce.productservice.service;

import com.dcarrillo.ecomerce.productservice.dto.CreateProductDTO;
import com.dcarrillo.ecomerce.productservice.dto.ProductDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    ProductDTO createProduct(CreateProductDTO CreateProductoDTO);

    List<ProductDTO> createMultipleProducts(@Valid List<CreateProductDTO> createProductDTOs);

    Optional<ProductDTO> findProductById(Long id);

    Page<ProductDTO> findAll(Pageable pageable);

    Page<ProductDTO> findProductByCategory(Long categoryId, Pageable pageable);

    ProductDTO updateProduct (Long id , CreateProductDTO CreateProductoDTO);


    void updateStock(Long id, Integer quantity);

    void deleteProduct(Long id);
}
