package com.dcarrillo.ecomerce.productservice.controller; // Asegúrate de que coincida con tu paquete

import com.dcarrillo.ecomerce.productservice.dto.CreateProductDTO;
import com.dcarrillo.ecomerce.productservice.entity.Category;
import com.dcarrillo.ecomerce.productservice.repository.CategoryRepository;
import com.dcarrillo.ecomerce.productservice.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // Para simular usuarios
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Category savedCategory;
    private CreateProductDTO createProductDTO;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = new Category();
        category.setName("Electrónica para Tests");
        savedCategory = categoryRepository.save(category);

        createProductDTO = new CreateProductDTO();
        createProductDTO.setName("Teclado Mecánico de Test");
        createProductDTO.setDescription("Un teclado para probar");
        createProductDTO.setPrice(new BigDecimal("99.99"));
        createProductDTO.setStock(100);
        createProductDTO.setCategoryId(savedCategory.getId());
    }

    @Test
    @DisplayName("GET /api/v1/products - Debería retornar una página de productos")
    void testListarProductos_RetornaPaginaDeProductos() throws Exception {

        ResultActions response = mockMvc.perform(get("/api/v1/products?page=0&size=5"));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/products - Debería permitir crear un producto a un usuario ADMIN")
    @WithMockUser(roles = "ADMIN")
    void testCrearProducto_ConRolAdmin_RetornaCreated() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createProductDTO)));

        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(createProductDTO.getName()))
                .andExpect(jsonPath("$.categoryName").value(savedCategory.getName()));
    }

    @Test
    @DisplayName("POST /api/v1/products - Debería denegar crear un producto a un usuario USER")
    @WithMockUser(roles = "USER")
    void testCrearProducto_ConRolUser_RetornaForbidden() throws Exception {

        ResultActions response = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createProductDTO)));

        response.andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/products - Debería denegar crear un producto a un usuario no autenticado")
    void testCrearProducto_SinAuth_RetornaUnauthorized() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createProductDTO)));

        response.andDo(print())
                .andExpect(status().isForbidden());
    }
}

