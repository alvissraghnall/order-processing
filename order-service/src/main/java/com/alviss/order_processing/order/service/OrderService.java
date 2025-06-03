package com.alviss.order_processing.order.service;

import com.alviss.order_processing.order.model.OrderStatus;
import com.alviss.order_processing.order.domain.Order;
import com.alviss.order_processing.order.repos.OrderRepository;
import com.alviss.order_processing.proto_common.CheckStockResponse;
import com.alviss.order_processing.proto_common.UpdateStockResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    
    private final InventoryClientService inventoryClientService;

    public OrderService(
        OrderRepository orderRepository,
        InventoryClientService inventoryClientService
    ) {
        this.inventoryClientService = inventoryClientService;
        this.orderRepository = orderRepository;
    }
    
    public Order createOrder(String customerName, Long productId, Integer quantity) {
        CheckStockResponse stockResponse = inventoryClientService.checkStock(productId, quantity);

        if (stockResponse.hasError()) {
            throw new RuntimeException("Error checking stock: " + stockResponse.getError().getMessage());
        }

        CheckStockResponse.SuccessResponse success = stockResponse.getSuccess();
        if (!success.getAvailable()) {
            throw new RuntimeException("Insufficient stock: Product not available.");
        }

		UpdateStockResponse updateResponse = inventoryClientService.updateStock(productId, -quantity);
        
        if (updateResponse.hasError()) {
            throw new RuntimeException("Failed to update stock: " + updateResponse.getError().getMessage());
        }
                
        Order order = new Order(
            customerName,
            productId,
            success.getProduct().getName(),
            quantity,
            success.getProduct().getPrice() * quantity,
            OrderStatus.CONFIRMED
        );
        
        return orderRepository.save(order);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
