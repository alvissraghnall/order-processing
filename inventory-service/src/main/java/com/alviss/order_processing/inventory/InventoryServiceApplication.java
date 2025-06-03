package com.alviss.order_processing.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;
import org.springframework.context.annotation.Bean;

import io.grpc.Status;

@SpringBootApplication
public class InventoryServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(InventoryServiceApplication.class, args);
  }

  @Bean
	public GrpcExceptionHandler globalInterceptor() {
		return exception -> {
			if (exception instanceof IllegalArgumentException) {
				return Status.INVALID_ARGUMENT.withDescription(exception.getMessage()).asException();
			}
			return null;
		};
	}

}
