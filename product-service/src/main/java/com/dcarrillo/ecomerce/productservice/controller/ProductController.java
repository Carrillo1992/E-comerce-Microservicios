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
        Optional<ProductDTO> productDTOOptional = service.findProductById(id);
        return productDTOOptional
                .map(dto -> ResponseEntity.ok().body(dto))
                .orElseGet(()-> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductDTO createProductDTO){
        ProductDTO productDTO = service.createProducto(createProductDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(productDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody CreateProductDTO createProductDTO){
        ProductDTO productDTO = service.updateProduct(id, createProductDTO);
        return ResponseEntity.ok().body(productDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id){
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
