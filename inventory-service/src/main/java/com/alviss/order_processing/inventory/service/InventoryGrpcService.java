package com.alviss.order_processing.inventory.service;

import com.alviss.order_processing.inventory.model.Product;
import com.alviss.order_processing.inventory.repository.ProductRepository;
import com.alviss.order_processing.proto_common.*;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InventoryGrpcService extends InventoryServiceGrpc.InventoryServiceImplBase {

  @Autowired
  private ProductRepository productRepository;

  @Override
  public void checkStock(CheckStockRequest request, StreamObserver<CheckStockResponse> responseObserver) {
    CheckStockResponse.Builder responseBuilder = CheckStockResponse.newBuilder();

    try {
      Optional<Product> productOpt = productRepository.findById(request.getProductId());

      if (productOpt.isPresent()) {
        Product product = productOpt.get();
        boolean available = product.getStockQuantity() >= request.getRequestedQuantity();

        CheckStockResponse.SuccessResponse successResponse = CheckStockResponse.SuccessResponse.newBuilder()
            .setAvailable(available)
            .setCurrentStock(product.getStockQuantity())
            .setProduct(
                com.alviss.order_processing.proto_common.Product.newBuilder()
                    .setId(product.getId())
                    .setName(product.getName())
                    .setPrice(product.getPrice())
                    .setStockQuantity(product.getStockQuantity())
                    .build())
            .build();

        responseBuilder.setSuccess(successResponse);
      } else {

        Map<String, String> details = Map.of("product_id", String.valueOf(request.getProductId()));
        responseBuilder.setError(buildError("PRODUCT_NOT_FOUND", "Product not found", details));
      }

      responseObserver.onNext(responseBuilder.build());
      responseObserver.onCompleted();

    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void updateStock(UpdateStockRequest request, StreamObserver<UpdateStockResponse> responseObserver) {
    UpdateStockResponse.Builder responseBuilder = UpdateStockResponse.newBuilder();

    try {
      Optional<Product> productOpt = productRepository.findById(request.getProductId());

      if (productOpt.isPresent()) {
        Product product = productOpt.get();
        int newStock = product.getStockQuantity() + request.getQuantityChange();

        if (newStock >= 0) {
          product.setStockQuantity(newStock);
          productRepository.save(product);

          UpdateStockResponse.SuccessResponse successResponse = UpdateStockResponse.SuccessResponse.newBuilder()
              .setSuccess(true)
              .setNewStockQuantity(newStock)
              .build();

          responseBuilder.setSuccess(successResponse);
        } else {

          Map<String, String> details = Map.of(
              "product_id", String.valueOf(request.getProductId()),
              "current_stock", productOpt.map(p -> String.valueOf(p.getStockQuantity())).orElse("N/A"),
              "attempted_change", String.valueOf(request.getQuantityChange())
          );
          responseBuilder.setError(buildError("INVALID_STOCK_UPDATE", "Cannot reduce stock below zero", details));
        }
      } else {

        Map<String, String> details = Map.of("product_id", String.valueOf(request.getProductId()));
        responseBuilder.setError(buildError("PRODUCT_NOT_FOUND", "Product not found", details));
      }

      responseObserver.onNext(responseBuilder.build());
      responseObserver.onCompleted();

    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void getProducts(GetProductsRequest request, StreamObserver<GetProductsResponse> responseObserver) {
    GetProductsResponse.Builder responseBuilder = GetProductsResponse.newBuilder();

    try {
      List<Product> allProducts = productRepository.findAll();
      int pageSize = request.getPageSize();
      int pageNumber = request.getPageNumber();

	  if (pageSize <= 0 || pageNumber <= 0) {
		  responseObserver.onNext(GetProductsResponse.newBuilder()
		      .setError(buildError("INVALID_ARGUMENT", "Page size and number must be greater than 0", Map.of()))
		      .build());
		  responseObserver.onCompleted();
		  return;
	  }

      int totalPages = (int) Math.ceil((double) allProducts.size() / pageSize);
	  int totalProducts = allProducts.size();

	  if ((pageNumber - 1) * pageSize >= totalProducts && totalProducts > 0) {
		  responseObserver.onNext(GetProductsResponse.newBuilder()
		      .setError(buildError("PAGE_OUT_OF_RANGE", "Requested page number exceeds available data", Map.of()))
		      .build());
		  responseObserver.onCompleted();
		  return;
	  }

      int startIndex = Math.min((pageNumber - 1) * pageSize, allProducts.size());
      int endIndex = Math.min(startIndex + pageSize, allProducts.size());

      List<Product> pagedProducts = allProducts.subList(startIndex, endIndex);

      GetProductsResponse.SuccessResponse.Builder successBuilder = GetProductsResponse.SuccessResponse.newBuilder()
          .setCurrentPage(pageNumber)
          .setTotalPages(totalPages);

      for (Product product : pagedProducts) {
        successBuilder.addProducts(
            com.alviss.order_processing.proto_common.Product.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setPrice(product.getPrice())
                .setStockQuantity(product.getStockQuantity())
                .build()
        );
      }

      responseBuilder.setSuccess(successBuilder.build());
      responseObserver.onNext(responseBuilder.build());
      responseObserver.onCompleted();

    } catch (Exception e) {
      responseObserver.onNext(GetProductsResponse.newBuilder()
        .setError(buildError("INTERNAL_ERROR", e.getMessage(), Map.of()))
        .build());
	  responseObserver.onCompleted();
    }
  }


  private com.alviss.order_processing.proto_common.Error buildError(String code, String message, Map<String, String> details) {
    return com.alviss.order_processing.proto_common.Error.newBuilder()
        .setCode(code)
        .setMessage(message)
        .putAllDetails(details)
        .setTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
        .build();
  }
}
