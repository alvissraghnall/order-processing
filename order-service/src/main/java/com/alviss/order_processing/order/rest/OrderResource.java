package com.alviss.order_processing.order.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import com.alviss.order_processing.order.model.OrderStatus;
import com.alviss.order_processing.order.domain.Order;
import com.alviss.order_processing.order.service.OrderService;
import com.alviss.order_processing.proto_common.GetProductsResponse;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import com.alviss.order_processing.order.service.InventoryClientService;
import com.alviss.order_processing.order.mapper.ProductMapper;
import com.alviss.order_processing.order.dto.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/orders")
public class OrderResource {

	private final OrderService orderService;

    private final InventoryClientService inventoryClientService;

    public OrderResource (
        OrderService orderService,
        InventoryClientService inventoryClientService
    ) {
        this.inventoryClientService = inventoryClientService;
        this.orderService = orderService;
    }

	@PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        try {
            String customerName = (String) request.get("customerName");
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());

            Order order = orderService.createOrder(customerName, productId, quantity);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

	@GetMapping("/products")
	public ResponseEntity<?> getProducts() {
	    GetProductsResponse protoResponse = inventoryClientService.getProducts();
		GetProductsResponseDto dto = ProductMapper.mapProtoToDto(protoResponse);

	    if (dto.getError() != null) {
	        return ResponseEntity
	                .status(HttpStatus.BAD_REQUEST)
	                .body(dto.getError());
	    }

	    return ResponseEntity.ok(dto);
	}

}
