package com.softserve.testapp.shared.infra.gatewaysendpoints;

public interface GatewayProperties {
    String getBaseUrl();
    String getEndpointUri(String endpointKey);
}
