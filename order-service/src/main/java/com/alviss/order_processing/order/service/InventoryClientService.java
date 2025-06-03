package com.alviss.order_processing.order.service;

import com.alviss.order_processing.proto_common.*;
import com.alviss.order_processing.proto_common.InventoryServiceGrpc;
import org.springframework.stereotype.Service;

@Service
public class InventoryClientService {
    
    private final InventoryServiceGrpc.InventoryServiceBlockingStub inventoryServiceStub;

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
        GetProductsRequest request = GetProductsRequest.newBuilder().build();
        return inventoryServiceStub.getProducts(request);
    }
}
