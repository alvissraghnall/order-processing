package com.alviss.order_processing.inventory_service.repository;

import com.alviss.order_processing.inventory_service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
