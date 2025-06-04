package com.alviss.order_processing.order.service;

import com.alviss.order_processing.proto_common.*;
import com.alviss.order_processing.proto_common.InventoryServiceGrpc;
import org.springframework.stereotype.Service;

@Service
public class InventoryClientService {
    
    private final InventoryServiceGrpc.InventoryServiceBlockingStub inventoryServiceStub;

	private static final int DEFAULT_PAGE_SIZE = 10;
	private static final int DEFAULT_PAGE_NUMBER = 1;

	public InventoryClientService (InventoryServiceGrpc.InventoryServiceBlockingStub inventoryServiceStub) {
        this.inventoryServiceStub = inventoryServiceStub;
    }
    
    public CheckStockResponse checkStock(Long productId, Integer quantity) {
        CheckStockRequest request = CheckStockRequest.newBuilder()
            .setProductId(productId)
            .setRequestedQuantity(quantity)
            .build();
            
        return inventoryServiceStub.checkStock(request);
    }
    
    public UpdateStockResponse updateStock(Long productId, Integer quantityChange) {
        UpdateStockRequest request = UpdateStockRequest.newBuilder()
            .setProductId(productId)
            .setQuantityChange(quantityChange)
            .build();
            
        return inventoryServiceStub.updateStock(request);
    }

	public GetProductsResponse getProducts() {
	    GetProductsRequest request = GetProductsRequest.newBuilder()
	        .setPageSize(DEFAULT_PAGE_SIZE)
	        .setPageNumber(DEFAULT_PAGE_NUMBER)
	        .build();
	    return inventoryServiceStub.getProducts(request);
	}
}
