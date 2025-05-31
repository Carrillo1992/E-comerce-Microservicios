package com.dcarrillo.ecomerce.productservice.service;

import com.dcarrillo.ecomerce.productservice.dto.CreateProductDTO;
import com.dcarrillo.ecomerce.productservice.dto.ProductDTO;
import com.dcarrillo.ecomerce.productservice.entity.Product;
import com.dcarrillo.ecomerce.productservice.repository.CategoryRepository;
import com.dcarrillo.ecomerce.productservice.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {


    ProductRepository productRepository;
    CategoryRepository categoryRepository;


    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    @Override
    public ProductDTO createProducto(CreateProductDTO createProductDTO) {

        Product product = new Product();
        product.setName(createProductDTO.getName());
        product.setPrice(createProductDTO.getPrice());
        product.setStock(createProductDTO.getStock());
        product.setDescription(createProductDTO.getDescription());
        product.setCategory(categoryRepository.findById(createProductDTO.getCategoryId())
                .orElseThrow(()-> new RuntimeException("Categoria no encontrada")));
        productRepository.save(product);

        return getProductDTO(product);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ProductDTO> findProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(()-> new RuntimeException("Producto no encotrado"));
        return Optional.of(getProductDTO(product));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductDTO> findAll(Pageable pageable) {
        Page<Product> productPage =   productRepository.findAll(pageable);
        return productPage.map(product -> getProductDTO(product));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductDTO> findProductByCategory(Long categoryId, Pageable pageable) {
        if (categoryRepository.findById(categoryId).isEmpty()){
            throw new RuntimeException("Categoria no encontrada");
        }
        Page<Product> productPage = productRepository.findByCategoryId(categoryId, pageable);
        return productPage.map(product -> getProductDTO(product));
    }

    @Transactional
    @Override
    public ProductDTO updateProduct(Long id, CreateProductDTO createProductDTO) {
        Product product = productRepository.findById(id).orElseThrow(()-> new RuntimeException("Producto no encontrado"));

        product.setName(createProductDTO.getName());
        product.setPrice(createProductDTO.getPrice());
        product.setDescription(createProductDTO.getDescription());
        product.setStock(createProductDTO.getStock());
        product.setCategory(categoryRepository.findById(createProductDTO.getCategoryId())
                .orElseThrow(()-> new RuntimeException("Categoria no encontrada")));
        productRepository.save(product);
        return getProductDTO(product);

    }

    @Transactional
    @Override
    public void deleteProduct(Long id) {
        if (productRepository.findById(id).isEmpty()){
            throw new RuntimeException("Producto no encontrado");
        }
        productRepository.deleteById(id);

    }

    private static ProductDTO getProductDTO(Product product) {
        ProductDTO productDTO =  new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setPrice(product.getPrice());
        productDTO.setStock(product.getStock());
        productDTO.setDescription(product.getDescription());
        productDTO.setCategoryName(product.getCategory().getName());
        return productDTO;
    }
}
