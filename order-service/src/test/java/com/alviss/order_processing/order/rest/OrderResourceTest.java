package com.alviss.order_processing.order.rest;

import com.alviss.order_processing.order.domain.Order;
import com.alviss.order_processing.order.model.OrderStatus;
import com.alviss.order_processing.order.service.InventoryClientService;
import com.alviss.order_processing.order.service.OrderService;
import com.alviss.order_processing.proto_common.GetProductsResponse;
import com.alviss.order_processing.proto_common.Product;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.boot.test.mock.mockito.MockBean;
import com.alviss.order_processing.order.dto.GetProductsResponseDto;
import com.alviss.order_processing.order.dto.ErrorDto;
import org.springframework.context.annotation.Import;
import com.alviss.order_processing.order.config.CorsConfig;
import com.alviss.order_processing.order.config.DomainConfig;
import com.alviss.order_processing.order.config.GrpcClientConfig;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.context.SpringBootTest.UseMainMethod;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;

@Transactional
@Rollback
@SpringBootTest(
    properties = { "spring.grpc.server.port=0",
            "spring.grpc.client.default-channel.address=0.0.0.0:${local.grpc.port}" },
    useMainMethod = UseMainMethod.ALWAYS
)
@Import({
    DomainConfig.class,
    GrpcClientConfig.class,
    CorsConfig.class
})
@TestPropertySource(properties = {
    "cors.allowed-origins=http://localhost:3000",
    "cors.allowed-methods=GET,POST,PUT,DELETE",
    "cors.allowed-headers=*",
    "cors.allow-credentials=true"
})
class OrderResourceTest {

    @MockBean
    private OrderService orderService;

    @MockBean
    private InventoryClientService inventoryClientService;

    @Autowired
    private CorsConfig corsConfig;

    @Autowired
    private DomainConfig domainConfig;

    @InjectMocks
    @Autowired
    private OrderResource orderResource;

	@BeforeEach
    void setUp() {
        reset(orderService, inventoryClientService);
    }

    @Test
    void contextLoads() {
        assertNotNull(corsConfig);
        assertNotNull(domainConfig);
        assertNotNull(orderResource);
    }

    @Test
    void createOrder_Success_ReturnsOrder() {
        Order mockOrder = new Order("John Doe", 1L, "Test Product", 2, 20.0, OrderStatus.CONFIRMED);
        when(orderService.createOrder(anyString(), anyLong(), any())).thenReturn(mockOrder);

        Map<String, Object> request = Map.of(
            "customerName", "John Doe",
            "productId", "1",
            "quantity", "2"
        );

        ResponseEntity<?> response = orderResource.createOrder(request);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Order);
        Order returnedOrder = (Order) response.getBody();
        assertEquals("John Doe", returnedOrder.getCustomerName());
    }

    @Test
    void createOrder_InvalidRequest_ReturnsBadRequest() {
        Map<String, Object> request = Map.of(
            "customerName", "John Doe",
            "productId", "invalid",
            "quantity", "2"
        );

        ResponseEntity<?> response = orderResource.createOrder(request);

        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
    }

    @Test
    void createOrder_ServiceThrowsException_ReturnsBadRequest() {
        when(orderService.createOrder(anyString(), anyLong(), any()))
            .thenThrow(new RuntimeException("Insufficient stock"));

        Map<String, Object> request = Map.of(
            "customerName", "John Doe",
            "productId", "1",
            "quantity", "2"
        );

        ResponseEntity<?> response = orderResource.createOrder(request);

        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Insufficient stock", ((Map<?, ?>) response.getBody()).get("error"));
    }

    @Test
    void getAllOrders_Success_ReturnsOrderList() {
        Order mockOrder = new Order("John Doe", 1L, "Test Product", 2, 20.0, OrderStatus.CONFIRMED);
        List<Order> mockOrders = Collections.singletonList(mockOrder);
        when(orderService.getAllOrders()).thenReturn(mockOrders);

        ResponseEntity<List<Order>> response = orderResource.getAllOrders();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("John Doe", response.getBody().get(0).getCustomerName());
    }

    @Test
    void getAllOrders_Empty_ReturnsEmptyList() {
        when(orderService.getAllOrders()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Order>> response = orderResource.getAllOrders();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getProducts_Success_ReturnsProductsResponse() {
        GetProductsResponse mockResponse = GetProductsResponse.newBuilder()
            .setSuccess(GetProductsResponse.SuccessResponse.newBuilder()
                .addProducts(Product.newBuilder()
                    .setId(1L)
                    .setName("Test Product")
                    .build())
                .build())
            .build();

        when(inventoryClientService.getProducts()).thenReturn(mockResponse);

        ResponseEntity<?> response = orderResource.getProducts();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
		
		GetProductsResponseDto productResponse = (GetProductsResponseDto) response.getBody();
	
		assertEquals(1, productResponse.getProducts().size());
		assertEquals("Test Product", productResponse.getProducts().get(0).getName());
    }

    @Test
    void getProducts_Error_ReturnsErrorResponse() {
        GetProductsResponse mockResponse = GetProductsResponse.newBuilder()
            .setError(com.alviss.order_processing.proto_common.Error.newBuilder()
                .setCode("INTERNAL_ERROR")
                .setMessage("Internal error")
                .build())
            .build();

        when(inventoryClientService.getProducts()).thenReturn(mockResponse);

        ResponseEntity<?> response = orderResource.getProducts();

        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
		ErrorDto productResponse = (ErrorDto) response.getBody();

        assertNotNull(productResponse.getCode());
        assertEquals("Internal error", productResponse.getMessage());
    }
}
