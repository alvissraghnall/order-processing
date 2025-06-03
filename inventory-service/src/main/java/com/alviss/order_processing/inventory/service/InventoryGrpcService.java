package com.alviss.order_processing.inventory.service;

import com.alviss.order_processing.inventory.model.Product;
import com.alviss.order_processing.inventory.repository.ProductRepository;
import com.alviss.order_processing.proto_common.InventoryProto.*;
import com.alviss.order_processing.proto_common.InventoryServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryGrpcService extends InventoryServiceGrpc.InventoryServiceImplBase {

  @Autowired
  private ProductRepository productRepository;

  @Override
  public void checkStock(CheckStockRequest request, StreamObserver<CheckStockResponse> responseObserver) {
    try {
      Optional<Product> productOpt = productRepository.findById(request.getProductId());

      CheckStockResponse.Builder responseBuilder = CheckStockResponse.newBuilder();

      if (productOpt.isPresent()) {
        Product product = productOpt.get();
        boolean available = product.getStockQuantity() >= request.getRequestedQuantity();

        responseBuilder
            .setAvailable(available)
            .setCurrentStock(product.getStockQuantity())
            .setProduct(com.alviss.order_processing.proto_common.InventoryProto.Product.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setPrice(product.getPrice())
                .setStockQuantity(product.getStockQuantity())
                .build())
            .setMessage(available ? "Stock available" : "Insufficient stock");
      } else {
        responseBuilder
            .setAvailable(false)
            .setCurrentStock(0)
            .setMessage("Product not found");
      }

      responseObserver.onNext(responseBuilder.build());
      responseObserver.onCompleted();

    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void updateStock(UpdateStockRequest request, StreamObserver<UpdateStockResponse> responseObserver) {
    try {
      Optional<Product> productOpt = productRepository.findById(request.getProductId());

      UpdateStockResponse.Builder responseBuilder = UpdateStockResponse.newBuilder();

      if (productOpt.isPresent()) {
        Product product = productOpt.get();
        int newStock = product.getStockQuantity() + request.getQuantityChange();

        if (newStock >= 0) {
          product.setStockQuantity(newStock);
          productRepository.save(product);

          responseBuilder
              .setSuccess(true)
              .setNewStock(newStock)
              .setMessage("Stock updated successfully");
        } else {
          responseBuilder
              .setSuccess(false)
              .setNewStock(product.getStockQuantity())
              .setMessage("Cannot reduce stock below zero");
        }
      } else {
        responseBuilder
            .setSuccess(false)
            .setNewStock(0)
            .setMessage("Product not found");
      }

      responseObserver.onNext(responseBuilder.build());
      responseObserver.onCompleted();

    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void getProducts(GetProductsRequest request, StreamObserver<GetProductsResponse> responseObserver) {
    try {
      List<Product> products = productRepository.findAll();

      GetProductsResponse.Builder responseBuilder = GetProductsResponse.newBuilder();

      for (Product product : products) {
        responseBuilder.addProducts(
            com.alviss.order_processing.proto_common.InventoryProto.Product.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setPrice(product.getPrice())
                .setStockQuantity(product.getStockQuantity())
                .build());
      }

      responseObserver.onNext(responseBuilder.build());
      responseObserver.onCompleted();

    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }
}
