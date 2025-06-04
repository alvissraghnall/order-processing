package com.alviss.order_processing.order.service;

import com.alviss.order_processing.proto_common.*;
import com.alviss.order_processing.proto_common.InventoryServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryClientServiceTest {

    @Mock
    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryServiceStub;

    private InventoryClientService inventoryClientService;

    @BeforeEach
    void setUp() {
        inventoryClientService = new InventoryClientService(inventoryServiceStub);
    }

    @Test
    void checkStock_SuccessfulResponse_ReturnsSuccessResponse() {
        Long productId = 123L;
        Integer quantity = 5;
        Product product = Product.newBuilder().setId(productId).setName("Test Product").build();
        CheckStockResponse.SuccessResponse successResponse = CheckStockResponse.SuccessResponse.newBuilder()
                .setAvailable(true)
                .setCurrentStock(10)
                .setProduct(product)
                .build();
        CheckStockResponse expectedResponse = CheckStockResponse.newBuilder()
                .setSuccess(successResponse)
                .build();

        when(inventoryServiceStub.checkStock(any())).thenReturn(expectedResponse);

        CheckStockResponse actualResponse = inventoryClientService.checkStock(productId, quantity);

        assertNotNull(actualResponse);
        assertTrue(actualResponse.hasSuccess());
        assertEquals(10, actualResponse.getSuccess().getCurrentStock());
        verify(inventoryServiceStub).checkStock(argThat(request ->
                request.getProductId() == productId &&
                request.getRequestedQuantity() == quantity));
    }

    @Test
    void updateStock_SuccessfulResponse_ReturnsSuccessResponse() {
        Long productId = 123L;
        Integer quantityChange = -2;
        UpdateStockResponse.SuccessResponse successResponse = UpdateStockResponse.SuccessResponse.newBuilder()
                .setSuccess(true)
                .setNewStockQuantity(8)
                .build();
        UpdateStockResponse expectedResponse = UpdateStockResponse.newBuilder()
                .setSuccess(successResponse)
                .build();

        when(inventoryServiceStub.updateStock(any())).thenReturn(expectedResponse);

        UpdateStockResponse actualResponse = inventoryClientService.updateStock(productId, quantityChange);

        assertNotNull(actualResponse);
        assertTrue(actualResponse.hasSuccess());
        assertEquals(8, actualResponse.getSuccess().getNewStockQuantity());
        verify(inventoryServiceStub).updateStock(argThat(request ->
                request.getProductId() == productId &&
                request.getQuantityChange() == quantityChange));
    }

    @Test
    void getProducts_SuccessfulResponse_ReturnsSuccessResponse() {
        Product product1 = Product.newBuilder().setId(1L).setName("Product 1").build();
        Product product2 = Product.newBuilder().setId(2L).setName("Product 2").build();
        GetProductsResponse.SuccessResponse successResponse = GetProductsResponse.SuccessResponse.newBuilder()
                .addProducts(product1)
                .addProducts(product2)
                .setTotalPages(1)
                .setCurrentPage(0)
                .build();
        GetProductsResponse expectedResponse = GetProductsResponse.newBuilder()
                .setSuccess(successResponse)
                .build();

        when(inventoryServiceStub.getProducts(any())).thenReturn(expectedResponse);

        GetProductsResponse actualResponse = inventoryClientService.getProducts();

        assertNotNull(actualResponse);
        assertTrue(actualResponse.hasSuccess());
        assertEquals(2, actualResponse.getSuccess().getProductsCount());
        assertEquals(1, actualResponse.getSuccess().getTotalPages());
        verify(inventoryServiceStub).getProducts(any());
    }

    @Test
    void getProducts_EmptySuccessResponse_ReturnsEmptyResponse() {
        GetProductsResponse.SuccessResponse successResponse = GetProductsResponse.SuccessResponse.newBuilder()
                .setTotalPages(0)
                .setCurrentPage(0)
                .build();
        GetProductsResponse expectedResponse = GetProductsResponse.newBuilder()
                .setSuccess(successResponse)
                .build();

        when(inventoryServiceStub.getProducts(any())).thenReturn(expectedResponse);

        GetProductsResponse actualResponse = inventoryClientService.getProducts();

        assertNotNull(actualResponse);
        assertTrue(actualResponse.hasSuccess());
        assertEquals(0, actualResponse.getSuccess().getProductsCount());
    }
}
