package com.dcarrillo.ecomerce.productservice.service;

import com.dcarrillo.ecomerce.productservice.dto.CreateProductDTO;
import com.dcarrillo.ecomerce.productservice.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductService {

    ProductDTO createProduct(CreateProductDTO CreateProductoDTO);

    Optional<ProductDTO> findProductById(Long id);

    Page<ProductDTO> findAll(Pageable pageable);

    Page<ProductDTO> findProductByCategory(Long categoryId, Pageable pageable);

    ProductDTO updateProduct (Long id , CreateProductDTO CreateProductoDTO);


    void updateStock(Long id, Integer quantity);

    void deleteProduct(Long id);
}
