package com.dcarrillo.ecomerce.productservice.service;

import com.dcarrillo.ecomerce.productservice.config.RabbitMQProducerConfig;
import com.dcarrillo.ecomerce.productservice.dto.CreateProductDTO;
import com.dcarrillo.ecomerce.productservice.dto.ProductDTO;
import com.dcarrillo.ecomerce.productservice.dto.event.DeletedProductDTO;
import com.dcarrillo.ecomerce.productservice.entity.Product;
import com.dcarrillo.ecomerce.productservice.repository.CategoryRepository;
import com.dcarrillo.ecomerce.productservice.repository.ProductRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.data.domain.Pageable;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {


    private final RabbitTemplate rabbitTemplate;
    ProductRepository productRepository;
    CategoryRepository categoryRepository;


    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, RabbitTemplate rabbitTemplate) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    @Override
    public ProductDTO createProduct(CreateProductDTO createProductDTO) {

        Product product = new Product();
        product.setName(createProductDTO.getName());
        product.setPrice(createProductDTO.getPrice());
        product.setStock(createProductDTO.getStock());
        product.setDescription(createProductDTO.getDescription());
        product.setCategory(categoryRepository.findById(createProductDTO.getCategoryId())
                .orElseThrow(()-> new HttpClientErrorException(HttpStatus.NOT_FOUND,"Categoria no encontrada")));
        productRepository.save(product);

        return getProductDTO(product);
    }

    @Transactional
    @Override
    public List<ProductDTO> createMultipleProducts(List<CreateProductDTO> createProductDTOs) {
        List<ProductDTO> productDTOS =new ArrayList<>();
        createProductDTOs.forEach(product ->{
            productDTOS.add(createProduct(product));
        });
        return productDTOS;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ProductDTO> findProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(()-> new HttpClientErrorException(HttpStatus.NOT_FOUND,"Producto "+ id + " no encontrado"));
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
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND,"Categoria no encontrada");
        }
        Page<Product> productPage = productRepository.findByCategoryId(categoryId, pageable);
        return productPage.map(this::getProductDTO);
    }

    @Transactional
    @Override
    public ProductDTO updateProduct(Long id, CreateProductDTO createProductDTO) {
        Product product = productRepository.findById(id).orElseThrow(()-> new HttpClientErrorException(HttpStatus.NOT_FOUND,"Producto no encontrado"));

        product.setName(createProductDTO.getName());
        product.setPrice(createProductDTO.getPrice());
        product.setDescription(createProductDTO.getDescription());
        product.setStock(createProductDTO.getStock());
        product.setCategory(categoryRepository.findById(createProductDTO.getCategoryId())
                .orElseThrow(()-> new HttpClientErrorException(HttpStatus.NOT_FOUND,"Categoria no encontrada")));
        productRepository.save(product);
        return getProductDTO(product);

    }

    @Transactional
    @Override
    public void deleteProduct(Long id) {
        if (productRepository.findById(id).isEmpty()){
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND,"Producto no encontrado");
        }
        productRepository.deleteById(id);
        DeletedProductDTO deleted = new DeletedProductDTO();
        deleted.setId(id);
        rabbitTemplate.convertAndSend(RabbitMQProducerConfig.EXCHANGE_NAME,
                RabbitMQProducerConfig.ROUTING_KEY_PRODUCT_DELETED,
                deleted);


    }


    @Override
    public void updateStock(Long id, Integer quantity) {
        Optional<Product> productOptional = productRepository.findById(id);

        Product product = productOptional.orElseThrow(()-> new HttpClientErrorException(HttpStatus.NOT_FOUND,"Producto no encontrado"));
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }


    private ProductDTO getProductDTO(Product product) {
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
