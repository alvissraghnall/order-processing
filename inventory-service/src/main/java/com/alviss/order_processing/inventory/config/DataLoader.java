package com.alviss.order_processing.inventory.config;

import com.alviss.order_processing.inventory.model.Product;
import com.alviss.order_processing.inventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

  private final ProductRepository productRepository;

  @Autowired
  public DataLoader(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public void run(String... args) throws Exception {
    // Initialize sample products
    productRepository.save(new Product("Chair", 27.34, 50));
    productRepository.save(new Product("Table", 83.12, 36));
    productRepository.save(new Product("Canopy", 44.00, 118));
    productRepository.save(new Product("Flask", 1041.03, 930));
    productRepository.save(new Product("Pot", 2.99, 61));
  }
}
