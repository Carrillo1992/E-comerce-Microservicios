package com.dcarrillo.ecomerce.productservice.service;

import com.dcarrillo.ecomerce.productservice.dto.CreateProductDTO;
import com.dcarrillo.ecomerce.productservice.dto.ProductDTO;
import com.dcarrillo.ecomerce.productservice.entity.Category;
import com.dcarrillo.ecomerce.productservice.entity.Product;
import com.dcarrillo.ecomerce.productservice.repository.CategoryRepository;
import com.dcarrillo.ecomerce.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class) // Habilita Mockito para JUnit 5
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;


    private Category category;
    private Product product;
    private CreateProductDTO createProductDTO;


    @BeforeEach
    void setup() {
        category = new Category();
        category.setId(1L);
        category.setName("Electrónica");

        createProductDTO = new CreateProductDTO();
        createProductDTO.setName("Laptop Pro");
        createProductDTO.setDescription("Una laptop potente"); // Asumiendo que se llama así en tu DTO
        createProductDTO.setPrice(new BigDecimal("1200.00"));
        createProductDTO.setStock(50);
        createProductDTO.setCategoryId(1L);

        product = new Product();
        product.setId(1L);
        product.setName(createProductDTO.getName());
        product.setDescription(createProductDTO.getDescription());
        product.setPrice(createProductDTO.getPrice());
        product.setStock(createProductDTO.getStock());
        product.setCategory(category);
    }

    @Test
    @DisplayName("Debería crear un producto exitosamente cuando la categoría existe")
    void testCreateProduct_Success() {
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productRepository.save(any(Product.class))).willReturn(product);

        ProductDTO savedProductDTO = productService.createProduct(createProductDTO);

        assertThat(savedProductDTO).isNotNull();
        assertThat(savedProductDTO.getName()).isEqualTo("Laptop Pro");
        assertThat(savedProductDTO.getCategoryName()).isEqualTo("Electrónica");

        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Debería lanzar una excepción al crear un producto si la categoría no existe")
    void testCreateProduct_CategoryNotFound_ThrowsException() {
        given(categoryRepository.findById(1L)).willReturn(Optional.empty());

        RuntimeException exception = assertThrows(HttpClientErrorException.class, () -> {
            productService.createProduct(createProductDTO);
        });

        assertThat(exception.getMessage()).isEqualTo("404 Categoria no encontrada");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Debería encontrar un producto por ID si existe")
    void testFindProductById_Exists() {
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        Optional<ProductDTO> foundProductDTO = productService.findProductById(1L);

        assertThat(foundProductDTO).isPresent();
        assertThat(foundProductDTO.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Debería devolver un Optional vacío si el producto por ID no existe")
    void testFindProductById_NotExists() {
        given(productRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            productService.findProductById(1L);
        });
    }

    @Test
    @DisplayName("Debería devolver una página de productos")
    void testFindAll_ReturnsPage() {
        Page<Product> productPage = new PageImpl<>(List.of(product));
        Pageable pageable = Pageable.unpaged();
        given(productRepository.findAll(pageable)).willReturn(productPage);

        Page<ProductDTO> resultPage = productService.findAll(pageable);

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().get(0).getName()).isEqualTo("Laptop Pro");
    }
}
