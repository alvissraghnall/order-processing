package com.alviss.order_processing.inventory.config;

import com.alviss.order_processing.inventory.model.Product;
import com.alviss.order_processing.inventory.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.context.SpringBootTest.UseMainMethod;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
	properties = { "spring.grpc.server.port=0",
			"spring.grpc.client.default-channel.address=0.0.0.0:${local.grpc.port}" },
	useMainMethod = UseMainMethod.ALWAYS
)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class DataLoaderIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DataLoader dataLoader;

    @Test
    void testRun_IntegrationTest_ShouldPersistProductsToDatabase() throws Exception {
        long initialCount = productRepository.count();

        dataLoader.run();

        assertEquals(initialCount + 5, productRepository.count());

        assertTrue(productRepository.findAll().stream()
            .anyMatch(p -> "Chair".equals(p.getName()) && p.getPrice() == 27.34 && p.getStockQuantity() == 50));
        
        assertTrue(productRepository.findAll().stream()
            .anyMatch(p -> "Flask".equals(p.getName()) && p.getPrice() == 1041.03 && p.getStockQuantity() == 930));
    }
}
