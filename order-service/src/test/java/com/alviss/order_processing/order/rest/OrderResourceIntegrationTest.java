package com.alviss.order_processing.order.rest;

import com.alviss.order_processing.order.domain.Order;
import com.alviss.order_processing.order.model.OrderStatus;
import com.alviss.order_processing.order.repos.OrderRepository;
import com.alviss.order_processing.order.service.InventoryClientService;
import com.alviss.order_processing.proto_common.CheckStockResponse;
import com.alviss.order_processing.proto_common.GetProductsResponse;
import com.alviss.order_processing.proto_common.UpdateStockResponse;
import com.alviss.order_processing.proto_common.Product;
import com.alviss.order_processing.proto_common.Error;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderResourceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private InventoryClientService inventoryClientService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.build();

        objectMapper = Jackson2ObjectMapperBuilder.json()
        	    .modules(new ProtobufModule(), new JavaTimeModule(), new Jdk8Module())
            	.build();

        orderRepository.deleteAll();
    }

    @Test
    void createOrder_WithValidRequest_ShouldCreateOrderSuccessfully() throws Exception {
        
        Map<String, Object> request = new HashMap<>();
        request.put("customerName", "John Doe");
        request.put("productId", 1L);
        request.put("quantity", 2);

        Product mockProduct = Product.newBuilder()
                .setId(1L)
                .setName("Test Product")
                .setPrice(100.0)
                .build();

        CheckStockResponse.SuccessResponse successResponse = CheckStockResponse.SuccessResponse.newBuilder()
                .setAvailable(true)
                .setProduct(mockProduct)
                .build();

        CheckStockResponse stockResponse = CheckStockResponse.newBuilder()
                .setSuccess(successResponse)
                .build();

        UpdateStockResponse updateResponse = UpdateStockResponse.newBuilder()
                .setSuccess(UpdateStockResponse.SuccessResponse.newBuilder().build())
                .build();

        when(inventoryClientService.checkStock(1L, 2)).thenReturn(stockResponse);
        when(inventoryClientService.updateStock(1L, -2)).thenReturn(updateResponse);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName").value("Test Product"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.price").value(200.0))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.id").exists());

        verify(inventoryClientService).checkStock(1L, 2);
        verify(inventoryClientService).updateStock(1L, -2);

        List<Order> orders = orderRepository.findAll();
        assert orders.size() == 1;
        assert orders.get(0).getCustomerName().equals("John Doe");
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldReturnBadRequest() throws Exception {
        
        Map<String, Object> request = new HashMap<>();
        request.put("customerName", "John Doe");
        request.put("productId", 1L);
        request.put("quantity", 5);

        Product mockProduct = Product.newBuilder()
                .setId(1L)
                .setName("Test Product")
                .setPrice(100.0)
                .build();

        CheckStockResponse.SuccessResponse successResponse = CheckStockResponse.SuccessResponse.newBuilder()
                .setAvailable(false)
                .setProduct(mockProduct)
                .build();

        CheckStockResponse stockResponse = CheckStockResponse.newBuilder()
                .setSuccess(successResponse)
                .build();

        when(inventoryClientService.checkStock(1L, 5)).thenReturn(stockResponse);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient stock: Product not available."));

        verify(inventoryClientService).checkStock(1L, 5);
        verify(inventoryClientService, never()).updateStock(any(), any());

        List<Order> orders = orderRepository.findAll();
        assert orders.isEmpty();
    }

    @Test
    void createOrder_WithStockCheckError_ShouldReturnBadRequest() throws Exception {
        
        Map<String, Object> request = new HashMap<>();
        request.put("customerName", "John Doe");
        request.put("productId", 1L);
        request.put("quantity", 2);

        com.alviss.order_processing.proto_common.Error errorResponse = com.alviss.order_processing.proto_common.Error.newBuilder()
                .setMessage("Product not found")
                .build();

        CheckStockResponse stockResponse = CheckStockResponse.newBuilder()
                .setError(errorResponse)
                .build();

        when(inventoryClientService.checkStock(1L, 2)).thenReturn(stockResponse);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error checking stock: Product not found"));

        verify(inventoryClientService).checkStock(1L, 2);
        verify(inventoryClientService, never()).updateStock(any(), any());
    }

    @Test
    void createOrder_WithStockUpdateError_ShouldReturnBadRequest() throws Exception {
        
        Map<String, Object> request = new HashMap<>();
        request.put("customerName", "Jane Smith");
        request.put("productId", 2L);
        request.put("quantity", 1);

        Product mockProduct = Product.newBuilder()
                .setId(2L)
                .setName("Another Product")
                .setPrice(50.0)
                .build();

        CheckStockResponse.SuccessResponse successResponse = CheckStockResponse.SuccessResponse.newBuilder()
                .setAvailable(true)
                .setProduct(mockProduct)
                .build();

        CheckStockResponse stockResponse = CheckStockResponse.newBuilder()
                .setSuccess(successResponse)
                .build();

        com.alviss.order_processing.proto_common.Error updateErrorResponse = com.alviss.order_processing.proto_common.Error.newBuilder()
                .setMessage("Database update failed")
                .build();

        UpdateStockResponse updateResponse = UpdateStockResponse.newBuilder()
                .setError(updateErrorResponse)
                .build();

        when(inventoryClientService.checkStock(2L, 1)).thenReturn(stockResponse);
        when(inventoryClientService.updateStock(2L, -1)).thenReturn(updateResponse);

         
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Failed to update stock: Database update failed"));

        verify(inventoryClientService).checkStock(2L, 1);
        verify(inventoryClientService).updateStock(2L, -1);
    }

    @Test
    void createOrder_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("customerName", "John Doe");

         
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(inventoryClientService, never()).checkStock(any(), any());
        verify(inventoryClientService, never()).updateStock(any(), any());
    }

    @Test
    void getAllOrders_WithExistingOrders_ShouldReturnAllOrders() throws Exception {
        Order order1 = new Order("Customer 1", 1L, "Product 1", 2, 200.0, OrderStatus.CONFIRMED);
        Order order2 = new Order("Customer 2", 2L, "Product 2", 1, 100.0, OrderStatus.CONFIRMED);
        orderRepository.save(order1);
        orderRepository.save(order2);

         
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].customerName").value("Customer 1"))
                .andExpect(jsonPath("$[0].productName").value("Product 1"))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].price").value(200.0))
                .andExpect(jsonPath("$[1].customerName").value("Customer 2"))
                .andExpect(jsonPath("$[1].productName").value("Product 2"))
                .andExpect(jsonPath("$[1].quantity").value(1))
                .andExpect(jsonPath("$[1].price").value(100.0));
    }

    @Test
    void getAllOrders_WithNoOrders_ShouldReturnEmptyList() throws Exception {
         
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getProducts_ShouldReturnProductsFromInventoryService() throws Exception {
        
        Product product1 = Product.newBuilder()
                .setId(1L)
                .setName("Product 1")
                .setPrice(100.0)
                .build();

        Product product2 = Product.newBuilder()
                .setId(2L)
                .setName("Product 2")
                .setPrice(200.0)
                .build();

		GetProductsResponse.SuccessResponse successRes = GetProductsResponse.SuccessResponse.newBuilder()
				.addProducts(product1)
				.addProducts(product2)
				.build();
		

		GetProductsResponse productsResponse = GetProductsResponse.newBuilder()
                .setSuccess(successRes)
                .build();

        when(inventoryClientService.getProducts()).thenReturn(productsResponse);

		mockMvc.perform(get("/api/orders/products"))
			    .andExpect(status().isOk())
			    .andExpect(jsonPath("$.products", hasSize(2)))
			    .andExpect(jsonPath("$.products[0].id").value(1))
			    .andExpect(jsonPath("$.products[0].name").value("Product 1"))
			    .andExpect(jsonPath("$.products[0].price").value(100.0))
			    .andExpect(jsonPath("$.products[1].id").value(2))
			    .andExpect(jsonPath("$.products[1].name").value("Product 2"))
			    .andExpect(jsonPath("$.products[1].price").value(200.0));

        verify(inventoryClientService).getProducts();
    }

    @Test
    void createOrder_WithLargeQuantity_ShouldHandleCorrectly() throws Exception {
        
        Map<String, Object> request = new HashMap<>();
        request.put("customerName", "Bulk Customer");
        request.put("productId", 1L);
        request.put("quantity", 100);

        Product mockProduct = Product.newBuilder()
                .setId(1L)
                .setName("Bulk Product")
                .setPrice(10.0)
                .build();

        CheckStockResponse.SuccessResponse successResponse = CheckStockResponse.SuccessResponse.newBuilder()
                .setAvailable(true)
                .setProduct(mockProduct)
                .build();

        CheckStockResponse stockResponse = CheckStockResponse.newBuilder()
                .setSuccess(successResponse)
                .build();

        UpdateStockResponse updateResponse = UpdateStockResponse.newBuilder()
                .setSuccess(UpdateStockResponse.SuccessResponse.newBuilder().build())
                .build();

        when(inventoryClientService.checkStock(1L, 100)).thenReturn(stockResponse);
        when(inventoryClientService.updateStock(1L, -100)).thenReturn(updateResponse);

         
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Bulk Customer"))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.price").value(1000.0));

        verify(inventoryClientService).checkStock(1L, 100);
        verify(inventoryClientService).updateStock(1L, -100);
    }

    @Test
    void createOrder_IntegrationFlow_ShouldPersistCorrectly() throws Exception {
        
        Map<String, Object> request = new HashMap<>();
        request.put("customerName", "Integration Test Customer");
        request.put("productId", 3L);
        request.put("quantity", 3);

        Product mockProduct = Product.newBuilder()
                .setId(3L)
                .setName("Integration Product")
                .setPrice(75.0)
                .build();

        CheckStockResponse.SuccessResponse successResponse = CheckStockResponse.SuccessResponse.newBuilder()
                .setAvailable(true)
                .setProduct(mockProduct)
                .build();

        CheckStockResponse stockResponse = CheckStockResponse.newBuilder()
                .setSuccess(successResponse)
                .build();

        UpdateStockResponse updateResponse = UpdateStockResponse.newBuilder()
                .setSuccess(UpdateStockResponse.SuccessResponse.newBuilder().build())
                .build();

        when(inventoryClientService.checkStock(3L, 3)).thenReturn(stockResponse);
        when(inventoryClientService.updateStock(3L, -3)).thenReturn(updateResponse);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerName").value("Integration Test Customer"))
                .andExpect(jsonPath("$[0].productId").value(3))
                .andExpect(jsonPath("$[0].productName").value("Integration Product"))
                .andExpect(jsonPath("$[0].quantity").value(3))
                .andExpect(jsonPath("$[0].price").value(225.0))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }
}
