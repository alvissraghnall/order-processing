package com.alviss.order_processing.inventory.config;

import com.alviss.order_processing.inventory.model.Product;
import com.alviss.order_processing.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataLoaderTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DataLoader dataLoader;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testRun_ShouldSaveAllProducts() throws Exception {
        when(productRepository.save(any(Product.class))).thenReturn(new Product());

        dataLoader.run();

        verify(productRepository, times(5)).save(any(Product.class));
    }

    @Test
    void testRun_ShouldSaveProductsWithCorrectData() throws Exception {
        when(productRepository.save(any(Product.class))).thenReturn(new Product());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

        dataLoader.run();

        verify(productRepository, times(5)).save(captor.capture());

        List<Product> saved = captor.getAllValues();

        Product chair = saved.get(0);
        assertEquals("Chair", chair.getName());
        assertEquals(27.34, chair.getPrice(), 0.01);
        assertEquals(50, chair.getStockQuantity());

        Product table = saved.get(1);
        assertEquals("Table", table.getName());
        assertEquals(83.12, table.getPrice(), 0.01);
        assertEquals(36, table.getStockQuantity());

        Product canopy = saved.get(2);
        assertEquals("Canopy", canopy.getName());
        assertEquals(44.00, canopy.getPrice(), 0.01);
        assertEquals(118, canopy.getStockQuantity());

        Product flask = saved.get(3);
        assertEquals("Flask", flask.getName());
        assertEquals(1041.03, flask.getPrice(), 0.01);
        assertEquals(930, flask.getStockQuantity());

        Product pot = saved.get(4);
        assertEquals("Pot", pot.getName());
        assertEquals(2.99, pot.getPrice(), 0.01);
        assertEquals(61, pot.getStockQuantity());
    }

    @Test
    void testRun_WithCommandLineArguments_ShouldIgnoreArguments() throws Exception {
        when(productRepository.save(any(Product.class))).thenReturn(new Product());

        dataLoader.run("arg1", "arg2");

        verify(productRepository, times(5)).save(any(Product.class));
    }

    @Test
    void testRun_WhenRepositoryThrowsException_ShouldPropagateException() {
        when(productRepository.save(any(Product.class)))
            .thenThrow(new RuntimeException("Database error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> dataLoader.run());
        assertEquals("Database error", ex.getMessage());
    }

    @Test
    void testRun_ShouldSaveProductsInCorrectOrder() throws Exception {
        when(productRepository.save(any(Product.class))).thenReturn(new Product());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

        dataLoader.run();

        verify(productRepository, times(5)).save(captor.capture());

        List<Product> saved = captor.getAllValues();

        assertEquals("Chair", saved.get(0).getName());
        assertEquals("Table", saved.get(1).getName());
        assertEquals("Canopy", saved.get(2).getName());
        assertEquals("Flask", saved.get(3).getName());
        assertEquals("Pot", saved.get(4).getName());
    }

    @Test
    void testRun_WithEmptyArgs_ShouldStillSaveProducts() throws Exception {
        when(productRepository.save(any(Product.class))).thenReturn(new Product());

        dataLoader.run(new String[]{});

        verify(productRepository, times(5)).save(any(Product.class));
    }

    @Test
    void testRun_WithNullArgs_ShouldStillSaveProducts() throws Exception {
        when(productRepository.save(any(Product.class))).thenReturn(new Product());

        dataLoader.run((String[]) null);

        verify(productRepository, times(5)).save(any(Product.class));
    }
}
