package com.dcarrillo.ecomerce.productservice.controller;

import com.dcarrillo.ecomerce.productservice.dto.CreateProductDTO;
import com.dcarrillo.ecomerce.productservice.dto.ProductDTO;
import com.dcarrillo.ecomerce.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService productService) {
        this.service = productService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> productList(Pageable pageable){
        Page<ProductDTO> productDTOS = service.findAll(pageable);
        return ResponseEntity.ok(productDTOS);
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> productById(@PathVariable Long id ){
        Optional<ProductDTO> productDTOOptional = null;
        try {
            productDTOOptional = service.findProductById(id);
        }catch (HttpClientErrorException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return  ResponseEntity.ok().body(productDTOOptional);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductDTO createProductDTO){
        ProductDTO productDTO = service.createProduct(createProductDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(productDTO);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductDTO>> createMultipleProducts(@Valid @RequestBody List<CreateProductDTO> createProductDTOs){
        List<ProductDTO> productDTOs = service.createMultipleProducts(createProductDTOs);
        return ResponseEntity.status(HttpStatus.CREATED).body(productDTOs);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody CreateProductDTO createProductDTO){
        ProductDTO productDTO = null;
        try {
            productDTO = service.updateProduct(id, createProductDTO);
        }catch (HttpClientErrorException e){
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return ResponseEntity.ok().body(productDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id){
        try {
            service.deleteProduct(id);
        }catch (HttpClientErrorException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        return ResponseEntity.noContent().build();
    }
}
