package com.alviss.order_processing.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.context.annotation.Configuration;
import com.alviss.order_processing.proto_common.InventoryServiceGrpc;
import io.grpc.Channel;

@Configuration
public class GrpcClientConfig {

    @Bean
    public InventoryServiceGrpc.InventoryServiceBlockingStub inventoryServiceStub(GrpcChannelFactory channelFactory) {
        Channel channel = channelFactory.createChannel("inventory-service");
        return InventoryServiceGrpc.newBlockingStub(channel);
    }
}
