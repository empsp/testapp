package com.softserve.testapp.shared.infra.gatewaysendpoints;

import static java.util.Optional.ofNullable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("testapp")
class GatewaysPropertiesImpl implements GatewaysProperties {
    @Setter
    private Map<String, GatewayPropertiesImpl> gateways = new HashMap<>();

    @Override
    public GatewayProperties getProps(String gatewayKey) {
        return ofNullable(gateways.get(gatewayKey)).orElseThrow(() -> new RuntimeException("Missing gateway configuration for key: " + gatewayKey));
    }

    @NoArgsConstructor
    public static class GatewayPropertiesImpl implements GatewayProperties {
        @Setter
        @Getter
        String baseUrl;

        @Setter
        Map<String, String> endpoints;

        public String getEndpointUri(String endpointKey) {
            return ofNullable(endpoints.get(endpointKey)).orElseThrow(() -> new RuntimeException(String.format("Gateway baseUrl: %s, missing endpoint configuration for key: %s", baseUrl, endpointKey)));
        }
    }
}
