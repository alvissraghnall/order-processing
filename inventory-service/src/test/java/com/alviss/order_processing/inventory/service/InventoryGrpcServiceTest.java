package com.alviss.order_processing.inventory.service;

import com.alviss.order_processing.inventory.model.Product;
import com.alviss.order_processing.inventory.repository.ProductRepository;
import com.alviss.order_processing.proto_common.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryGrpcServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StreamObserver<CheckStockResponse> checkStockResponseObserver;

    @Mock
    private StreamObserver<UpdateStockResponse> updateStockResponseObserver;

    @Mock
    private StreamObserver<GetProductsResponse> getProductsResponseObserver;

    @InjectMocks
    private InventoryGrpcService inventoryGrpcService;

    private Product testProduct;
    private com.alviss.order_processing.proto_common.Product protoProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(29.99);
        testProduct.setStockQuantity(100);

        protoProduct = com.alviss.order_processing.proto_common.Product.newBuilder()
                .setId(1L)
                .setName("Test Product")
                .setPrice(29.99)
                .setStockQuantity(100)
                .build();
		reset(productRepository);
    }

    @Test
    void checkStock_ProductExistsAndSufficientStock_ReturnsAvailableTrue() {
        
        CheckStockRequest request = CheckStockRequest.newBuilder()
                .setProductId(1L)
                .setRequestedQuantity(50)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        
        inventoryGrpcService.checkStock(request, checkStockResponseObserver);

        
        ArgumentCaptor<CheckStockResponse> responseCaptor = ArgumentCaptor.forClass(CheckStockResponse.class);
        verify(checkStockResponseObserver).onNext(responseCaptor.capture());
        verify(checkStockResponseObserver).onCompleted();

        CheckStockResponse response = responseCaptor.getValue();
        assertTrue(response.hasSuccess());
        assertTrue(response.getSuccess().getAvailable());
        assertEquals(100, response.getSuccess().getCurrentStock());
        assertEquals(protoProduct, response.getSuccess().getProduct());
    }

    @Test
    void checkStock_ProductExistsButInsufficientStock_ReturnsAvailableFalse() {
        
        CheckStockRequest request = CheckStockRequest.newBuilder()
                .setProductId(1L)
                .setRequestedQuantity(150)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        
        inventoryGrpcService.checkStock(request, checkStockResponseObserver);

        
        ArgumentCaptor<CheckStockResponse> responseCaptor = ArgumentCaptor.forClass(CheckStockResponse.class);
        verify(checkStockResponseObserver).onNext(responseCaptor.capture());
        verify(checkStockResponseObserver).onCompleted();

        CheckStockResponse response = responseCaptor.getValue();
        assertTrue(response.hasSuccess());
        assertFalse(response.getSuccess().getAvailable());
        assertEquals(100, response.getSuccess().getCurrentStock());
    }

    @Test
    void checkStock_ProductExistsWithExactQuantity_ReturnsAvailableTrue() {
        
        CheckStockRequest request = CheckStockRequest.newBuilder()
                .setProductId(1L)
                .setRequestedQuantity(100)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        
        inventoryGrpcService.checkStock(request, checkStockResponseObserver);

        
        ArgumentCaptor<CheckStockResponse> responseCaptor = ArgumentCaptor.forClass(CheckStockResponse.class);
        verify(checkStockResponseObserver).onNext(responseCaptor.capture());

        CheckStockResponse response = responseCaptor.getValue();
        assertTrue(response.getSuccess().getAvailable());
    }

    @Test
    void checkStock_ProductNotFound_ReturnsError() {
        
        CheckStockRequest request = CheckStockRequest.newBuilder()
                .setProductId(999L)
                .setRequestedQuantity(50)
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        
        inventoryGrpcService.checkStock(request, checkStockResponseObserver);

        
        ArgumentCaptor<CheckStockResponse> responseCaptor = ArgumentCaptor.forClass(CheckStockResponse.class);
        verify(checkStockResponseObserver).onNext(responseCaptor.capture());
        verify(checkStockResponseObserver).onCompleted();

        CheckStockResponse response = responseCaptor.getValue();
        assertTrue(response.hasError());
        assertEquals("PRODUCT_NOT_FOUND", response.getError().getCode());
        assertEquals("Product not found", response.getError().getMessage());
        assertTrue(response.getError().getDetailsMap().containsKey("product_id"));
        assertEquals("999", response.getError().getDetailsMap().get("product_id"));
    }

    @Test
    void checkStock_RepositoryThrowsException_CallsOnError() {
        
        CheckStockRequest request = CheckStockRequest.newBuilder()
                .setProductId(1L)
                .setRequestedQuantity(50)
                .build();

        RuntimeException exception = new RuntimeException("Database error");
        when(productRepository.findById(1L)).thenThrow(exception);

        
        inventoryGrpcService.checkStock(request, checkStockResponseObserver);

        
        verify(checkStockResponseObserver).onError(exception);
        verify(checkStockResponseObserver, never()).onNext(any());
        verify(checkStockResponseObserver, never()).onCompleted();
    }

    @Test
    void updateStock_ProductExistsAndValidUpdate_ReturnsSuccess() {
        
        UpdateStockRequest request = UpdateStockRequest.newBuilder()
                .setProductId(1L)
                .setQuantityChange(25)
                .build();

        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setStockQuantity(125);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        
        inventoryGrpcService.updateStock(request, updateStockResponseObserver);

        
        ArgumentCaptor<UpdateStockResponse> responseCaptor = ArgumentCaptor.forClass(UpdateStockResponse.class);
        verify(checkStockResponseObserver, never()).onError(any());
        verify(updateStockResponseObserver).onNext(responseCaptor.capture());
        verify(updateStockResponseObserver).onCompleted();

        UpdateStockResponse response = responseCaptor.getValue();
        assertTrue(response.hasSuccess());
        assertTrue(response.getSuccess().getSuccess());
        assertEquals(125, response.getSuccess().getNewStockQuantity());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertEquals(125, productCaptor.getValue().getStockQuantity());
    }

    @Test
    void updateStock_ProductExistsAndNegativeUpdate_ReturnsSuccess() {
        
        UpdateStockRequest request = UpdateStockRequest.newBuilder()
                .setProductId(1L)
                .setQuantityChange(-25)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        
        inventoryGrpcService.updateStock(request, updateStockResponseObserver);

        
        ArgumentCaptor<UpdateStockResponse> responseCaptor = ArgumentCaptor.forClass(UpdateStockResponse.class);
        verify(updateStockResponseObserver).onNext(responseCaptor.capture());

        UpdateStockResponse response = responseCaptor.getValue();
        assertTrue(response.hasSuccess());
        assertEquals(75, response.getSuccess().getNewStockQuantity());
    }

    @Test
    void updateStock_ProductExistsButUpdateWouldMakeStockNegative_ReturnsError() {
        
        UpdateStockRequest request = UpdateStockRequest.newBuilder()
                .setProductId(1L)
                .setQuantityChange(-150)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        
        inventoryGrpcService.updateStock(request, updateStockResponseObserver);

        
        ArgumentCaptor<UpdateStockResponse> responseCaptor = ArgumentCaptor.forClass(UpdateStockResponse.class);
        verify(updateStockResponseObserver).onNext(responseCaptor.capture());
        verify(updateStockResponseObserver).onCompleted();

        UpdateStockResponse response = responseCaptor.getValue();
        assertTrue(response.hasError());
        assertEquals("INVALID_STOCK_UPDATE", response.getError().getCode());
        assertEquals("Cannot reduce stock below zero", response.getError().getMessage());
        
        assertEquals("1", response.getError().getDetailsMap().get("product_id"));
        assertEquals("100", response.getError().getDetailsMap().get("current_stock"));
        assertEquals("-150", response.getError().getDetailsMap().get("attempted_change"));

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateStock_ProductExistsAndUpdateToExactlyZero_ReturnsSuccess() {
        
        UpdateStockRequest request = UpdateStockRequest.newBuilder()
                .setProductId(1L)
                .setQuantityChange(-100)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        
        inventoryGrpcService.updateStock(request, updateStockResponseObserver);

        
        ArgumentCaptor<UpdateStockResponse> responseCaptor = ArgumentCaptor.forClass(UpdateStockResponse.class);
        verify(updateStockResponseObserver).onNext(responseCaptor.capture());

        UpdateStockResponse response = responseCaptor.getValue();
        assertTrue(response.hasSuccess());
        assertEquals(0, response.getSuccess().getNewStockQuantity());
    }

    @Test
    void updateStock_ProductNotFound_ReturnsError() {
        
        UpdateStockRequest request = UpdateStockRequest.newBuilder()
                .setProductId(999L)
                .setQuantityChange(25)
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        
        inventoryGrpcService.updateStock(request, updateStockResponseObserver);

        
        ArgumentCaptor<UpdateStockResponse> responseCaptor = ArgumentCaptor.forClass(UpdateStockResponse.class);
        verify(updateStockResponseObserver).onNext(responseCaptor.capture());
        verify(updateStockResponseObserver).onCompleted();

        UpdateStockResponse response = responseCaptor.getValue();
        assertTrue(response.hasError());
        assertEquals("PRODUCT_NOT_FOUND", response.getError().getCode());
        assertEquals("Product not found", response.getError().getMessage());
        assertEquals("999", response.getError().getDetailsMap().get("product_id"));
    }

    @Test
    void updateStock_RepositoryThrowsException_CallsOnError() {
        
        UpdateStockRequest request = UpdateStockRequest.newBuilder()
                .setProductId(1L)
                .setQuantityChange(25)
                .build();

        RuntimeException exception = new RuntimeException("Database error");
        when(productRepository.findById(1L)).thenThrow(exception);

        
        inventoryGrpcService.updateStock(request, updateStockResponseObserver);

        
        verify(updateStockResponseObserver).onError(exception);
        verify(updateStockResponseObserver, never()).onNext(any());
        verify(updateStockResponseObserver, never()).onCompleted();
    }

    @Test
    void getProducts_WithValidPagination_ReturnsPagedResults() {
        
        GetProductsRequest request = GetProductsRequest.newBuilder()
                .setPageSize(2)
                .setPageNumber(1)
                .build();

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(39.99);
        product2.setStockQuantity(50);

        Product product3 = new Product();
        product3.setId(3L);
        product3.setName("Product 3");
        product3.setPrice(49.99);
        product3.setStockQuantity(25);

        List<Product> allProducts = Arrays.asList(testProduct, product2, product3);
        when(productRepository.findAll()).thenReturn(allProducts);

        
        inventoryGrpcService.getProducts(request, getProductsResponseObserver);

        
        ArgumentCaptor<GetProductsResponse> responseCaptor = ArgumentCaptor.forClass(GetProductsResponse.class);
        verify(getProductsResponseObserver).onNext(responseCaptor.capture());
        verify(getProductsResponseObserver).onCompleted();

        GetProductsResponse response = responseCaptor.getValue();
        assertTrue(response.hasSuccess());
        assertEquals(1, response.getSuccess().getCurrentPage());
        assertEquals(2, response.getSuccess().getTotalPages());
        assertEquals(2, response.getSuccess().getProductsCount());

        assertEquals(1L, response.getSuccess().getProducts(0).getId());
        assertEquals(2L, response.getSuccess().getProducts(1).getId());
    }

    @Test
    void getProducts_WithSecondPage_ReturnsCorrectResults() {
        
        GetProductsRequest request = GetProductsRequest.newBuilder()
                .setPageSize(2)
                .setPageNumber(2)
                .build();

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(39.99);
        product2.setStockQuantity(50);

        Product product3 = new Product();
        product3.setId(3L);
        product3.setName("Product 3");
        product3.setPrice(49.99);
        product3.setStockQuantity(25);

        List<Product> allProducts = Arrays.asList(testProduct, product2, product3);
        when(productRepository.findAll()).thenReturn(allProducts);

        
        inventoryGrpcService.getProducts(request, getProductsResponseObserver);

        
        ArgumentCaptor<GetProductsResponse> responseCaptor = ArgumentCaptor.forClass(GetProductsResponse.class);
        verify(getProductsResponseObserver).onNext(responseCaptor.capture());

        GetProductsResponse response = responseCaptor.getValue();
        assertTrue(response.hasSuccess());
        assertEquals(2, response.getSuccess().getCurrentPage());
        assertEquals(2, response.getSuccess().getTotalPages());
        assertEquals(1, response.getSuccess().getProductsCount());

        assertEquals(3L, response.getSuccess().getProducts(0).getId());
    }

	@Test
	void getProducts_WithPageBeyondAvailable_ReturnsErrorInResponse() {
	    GetProductsRequest request = GetProductsRequest.newBuilder()
	            .setPageSize(10)
	            .setPageNumber(2)
	            .build();

	    List<Product> allProducts = Arrays.asList(testProduct);
	    when(productRepository.findAll()).thenReturn(allProducts);

	    inventoryGrpcService.getProducts(request, getProductsResponseObserver);

	    ArgumentCaptor<GetProductsResponse> responseCaptor = ArgumentCaptor.forClass(GetProductsResponse.class);
	    verify(getProductsResponseObserver).onNext(responseCaptor.capture());
	    verify(getProductsResponseObserver).onCompleted();

	    GetProductsResponse response = responseCaptor.getValue();

	    assertTrue(response.hasError());
	    assertEquals("PAGE_OUT_OF_RANGE", response.getError().getCode());
	    assertEquals("Requested page number exceeds available data", response.getError().getMessage());
	}

    @Test
    void getProducts_WithEmptyRepository_ReturnsEmptyResults() {
        
        GetProductsRequest request = GetProductsRequest.newBuilder()
                .setPageSize(10)
                .setPageNumber(1)
                .build();

        when(productRepository.findAll()).thenReturn(Arrays.asList());

        
        inventoryGrpcService.getProducts(request, getProductsResponseObserver);

        
        ArgumentCaptor<GetProductsResponse> responseCaptor = ArgumentCaptor.forClass(GetProductsResponse.class);
        verify(getProductsResponseObserver).onNext(responseCaptor.capture());

        GetProductsResponse response = responseCaptor.getValue();
        assertTrue(response.hasSuccess());
        assertEquals(1, response.getSuccess().getCurrentPage());
        assertEquals(0, response.getSuccess().getTotalPages());
        assertEquals(0, response.getSuccess().getProductsCount());
    }

    @Test
    void getProducts_RepositoryThrowsException_ReturnsErrorResponse() {
        
        GetProductsRequest request = GetProductsRequest.newBuilder()
                .setPageSize(10)
                .setPageNumber(1)
                .build();

        RuntimeException exception = new RuntimeException("Database connection failed");
        when(productRepository.findAll()).thenThrow(exception);

        
        inventoryGrpcService.getProducts(request, getProductsResponseObserver);

        
        ArgumentCaptor<GetProductsResponse> responseCaptor = ArgumentCaptor.forClass(GetProductsResponse.class);
        verify(getProductsResponseObserver).onNext(responseCaptor.capture());

        GetProductsResponse response = responseCaptor.getValue();
        assertTrue(response.hasError());
        assertEquals("INTERNAL_ERROR", response.getError().getCode());
        assertEquals("Database connection failed", response.getError().getMessage());
        assertTrue(response.getError().getDetailsMap().isEmpty());
    }

    @Test
    void getProducts_WithLargePageSize_HandlesCorrectly() {
        
        GetProductsRequest request = GetProductsRequest.newBuilder()
                .setPageSize(1000)
                .setPageNumber(1)
                .build();

        List<Product> allProducts = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(allProducts);

        
        inventoryGrpcService.getProducts(request, getProductsResponseObserver);

        
        ArgumentCaptor<GetProductsResponse> responseCaptor = ArgumentCaptor.forClass(GetProductsResponse.class);
        verify(getProductsResponseObserver).onNext(responseCaptor.capture());

        GetProductsResponse response = responseCaptor.getValue();
        assertTrue(response.hasSuccess());
        assertEquals(1, response.getSuccess().getCurrentPage());
        assertEquals(1, response.getSuccess().getTotalPages());
        assertEquals(1, response.getSuccess().getProductsCount());
    }

    @Test
    void buildError_CreatesErrorWithAllFields() {
        CheckStockRequest request = CheckStockRequest.newBuilder()
                .setProductId(999L)
                .setRequestedQuantity(50)
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        inventoryGrpcService.checkStock(request, checkStockResponseObserver);

        ArgumentCaptor<CheckStockResponse> responseCaptor = ArgumentCaptor.forClass(CheckStockResponse.class);
        verify(checkStockResponseObserver).onNext(responseCaptor.capture());

        CheckStockResponse response = responseCaptor.getValue();
        assertNotNull(response.getError().getTimestamp());
        assertTrue(response.getError().getTimestamp().getSeconds() > 0);
    }
}
