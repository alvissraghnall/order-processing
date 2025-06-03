package com.alviss.order_processing.order.config;

import com.alviss.order_processing.order.domain.Order;
import com.alviss.order_processing.order.model.OrderStatus;
import com.alviss.order_processing.order.repos.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class OrderDataLoader implements CommandLineRunner {

    private final OrderRepository orderRepository;

    public OrderDataLoader(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (orderRepository.count() == 0) {
            List<Order> initialOrders = Arrays.asList(
                new Order("Sam Smith", 1L, "Wireless Headphones", 2, 199.98, OrderStatus.CONFIRMED),
                new Order("Kris Jericho", 2L, "Smart Watch", 1, 249.99, OrderStatus.PENDING),
                new Order("John F. Kennedy", 3L, "Bluetooth Speaker", 1, 69.99, OrderStatus.SHIPPED),
                new Order("Thomas Edison", 1L, "Wireless Headphones", 1, 99.99, OrderStatus.DELIVERED),
                new Order("Bruno Mars", 4L, "USB-A Cable", 3, 29.97, OrderStatus.CONFIRMED)
            );

            orderRepository.saveAll(initialOrders);
            System.out.println("Loaded initial orders into database");
        }
    }
}
