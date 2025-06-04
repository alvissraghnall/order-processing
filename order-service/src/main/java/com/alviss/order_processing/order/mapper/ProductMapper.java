package com.alviss.order_processing.order.mapper;

import com.alviss.order_processing.order.dto.*;
import com.alviss.order_processing.proto_common.GetProductsResponse;
import com.alviss.order_processing.proto_common.Product;

import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.util.Map;

import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {

    public static GetProductsResponseDto mapProtoToDto(GetProductsResponse protoResponse) {
        GetProductsResponseDto dto = new GetProductsResponseDto();

        if (protoResponse.hasSuccess()) {
            GetProductsResponse.SuccessResponse success = protoResponse.getSuccess();
            dto.setProducts(success.getProductsList().stream()
                    .map(ProductMapper::mapProduct)
                    .collect(Collectors.toList()));
            dto.setTotalPages(success.getTotalPages());
            dto.setCurrentPage(success.getCurrentPage());
        } else if (protoResponse.hasError()) {
            dto.setError(mapError(protoResponse.getError()));
        }

        return dto;
    }

    private static ProductDto mapProduct(Product protoProduct) {
        return new ProductDto(
                protoProduct.getId(),
                protoProduct.getName(),
                protoProduct.getPrice(),
                protoProduct.getStockQuantity()
        );
    }


	private static ErrorDto mapError(com.alviss.order_processing.proto_common.Error protoError) {
	    Timestamp timestamp = protoError.getTimestamp();
	    Instant instant = timestamp != null
	            ? Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos())
	            : null;

	    return new ErrorDto(
	            protoError.getCode(),
	            protoError.getMessage(),
	            protoError.getDetailsMap(),
	            instant
	    );
	}
}
