package com.softserve.testapp.foodimageretriever.infra.gateways.foodimage;

import com.softserve.testapp.shared.infra.gatewaysendpoints.GatewaysProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class FoodImageRetrieverConfiguration {
    @Bean
    FoodImageRetrieverGateway foodImageRetrieverGateway(WebClient.Builder builder, GatewaysProperties gatewaysProperties) {
        var gatewayEndpoints = gatewaysProperties.getProps("food-image-retriever");
        var webClient = builder.baseUrl(gatewayEndpoints.getBaseUrl()).build();
        return new FoodImageRetrieverGateway(webClient, gatewayEndpoints);
    }
}
