package com.alviss.order_processing.order.service;

import com.alviss.order_processing.order.domain.Order;
import com.alviss.order_processing.order.model.OrderStatus;
import com.alviss.order_processing.order.repos.OrderRepository;
import com.alviss.order_processing.proto_common.*;
import com.alviss.order_processing.proto_common.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClientService inventoryClientService;

    @InjectMocks
    private OrderService orderService;

    private final Long productId = 123L;
    private final Integer quantity = 2;
    private final String customerName = "John Doe";
    private final String productName = "Test Product";
    private final double productPrice = 10.99;

    @Test
    void createOrder_Successful_ReturnsOrder() {
        Product product = Product.newBuilder()
                .setId(productId)
                .setName(productName)
                .setPrice(productPrice)
                .build();

        CheckStockResponse.SuccessResponse checkSuccess = CheckStockResponse.SuccessResponse.newBuilder()
                .setAvailable(true)
                .setCurrentStock(10)
                .setProduct(product)
                .build();

        CheckStockResponse checkResponse = CheckStockResponse.newBuilder()
                .setSuccess(checkSuccess)
                .build();

        UpdateStockResponse.SuccessResponse updateSuccess = UpdateStockResponse.SuccessResponse.newBuilder()
                .setSuccess(true)
                .setNewStockQuantity(8)
                .build();

        UpdateStockResponse updateResponse = UpdateStockResponse.newBuilder()
                .setSuccess(updateSuccess)
                .build();

        Order expectedOrder = new Order(
                customerName,
                productId,
                productName,
                quantity,
                productPrice * quantity,
                OrderStatus.CONFIRMED
        );

        when(inventoryClientService.checkStock(productId, quantity)).thenReturn(checkResponse);
        when(inventoryClientService.updateStock(productId, -quantity)).thenReturn(updateResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        Order result = orderService.createOrder(customerName, productId, quantity);

        assertNotNull(result);
        assertEquals(customerName, result.getCustomerName());
        assertEquals(productId, result.getProductId());
        assertEquals(productName, result.getProductName());
        assertEquals(quantity, result.getQuantity());
        assertEquals(productPrice * quantity, result.getPrice());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());

        verify(inventoryClientService).checkStock(productId, quantity);
        verify(inventoryClientService).updateStock(productId, -quantity);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_CheckStockError_ThrowsException() {
        com.alviss.order_processing.proto_common.Error error = com.alviss.order_processing.proto_common.Error.newBuilder()
                .setCode("NOT_FOUND")
                .setMessage("Product not found")
                .build();

        CheckStockResponse errorResponse = CheckStockResponse.newBuilder()
                .setError(error)
                .build();

        when(inventoryClientService.checkStock(productId, quantity)).thenReturn(errorResponse);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(customerName, productId, quantity);
        });

        assertEquals("Error checking stock: Product not found", exception.getMessage());
        verify(inventoryClientService).checkStock(productId, quantity);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void createOrder_InsufficientStock_ThrowsException() {
        Product product = Product.newBuilder()
                .setId(productId)
                .setName(productName)
                .setPrice(productPrice)
                .build();

        CheckStockResponse.SuccessResponse checkSuccess = CheckStockResponse.SuccessResponse.newBuilder()
                .setAvailable(false)
                .setCurrentStock(1)
                .setProduct(product)
                .build();

        CheckStockResponse checkResponse = CheckStockResponse.newBuilder()
                .setSuccess(checkSuccess)
                .build();

        when(inventoryClientService.checkStock(productId, quantity)).thenReturn(checkResponse);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(customerName, productId, quantity);
        });

        assertEquals("Insufficient stock: Product not available.", exception.getMessage());
        verify(inventoryClientService).checkStock(productId, quantity);
        verifyNoInteractions(orderRepository);
		verify(inventoryClientService, never()).updateStock(any(), any());
    }

    @Test
    void createOrder_UpdateStockError_ThrowsException() {
        Product product = Product.newBuilder()
                .setId(productId)
                .setName(productName)
                .setPrice(productPrice)
                .build();

        CheckStockResponse.SuccessResponse checkSuccess = CheckStockResponse.SuccessResponse.newBuilder()
                .setAvailable(true)
                .setCurrentStock(10)
                .setProduct(product)
                .build();

        CheckStockResponse checkResponse = CheckStockResponse.newBuilder()
                .setSuccess(checkSuccess)
                .build();

        com.alviss.order_processing.proto_common.Error error = com.alviss.order_processing.proto_common.Error.newBuilder()
                .setCode("NOT_FOUND")
                .setMessage("Invalid quantity")
                .build();

        UpdateStockResponse updateResponse = UpdateStockResponse.newBuilder()
                .setError(error)
                .build();

        when(inventoryClientService.checkStock(productId, quantity)).thenReturn(checkResponse);
        when(inventoryClientService.updateStock(productId, -quantity)).thenReturn(updateResponse);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(customerName, productId, quantity);
        });

        assertEquals("Failed to update stock: Invalid quantity", exception.getMessage());
        verify(inventoryClientService).checkStock(productId, quantity);
        verify(inventoryClientService).updateStock(productId, -quantity);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void getAllOrders_ReturnsOrderList() {
        Order order1 = new Order("Customer1", 1L, "Product1", 1, 10.0, OrderStatus.CONFIRMED);
        Order order2 = new Order("Customer2", 2L, "Product2", 2, 20.0, OrderStatus.CONFIRMED);
        List<Order> expectedOrders = Arrays.asList(order1, order2);

        when(orderRepository.findAll()).thenReturn(expectedOrders);

        List<Order> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void getAllOrders_EmptyList_ReturnsEmptyList() {
        when(orderRepository.findAll()).thenReturn(List.of());

        List<Order> result = orderService.getAllOrders();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findAll();
    }
}
