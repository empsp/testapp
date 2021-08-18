package com.softserve.testapp.shared.infra.gatewaysendpoints;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GatewaysPropertiesImpl.class)
class GatewaysPropertiesConfiguration {
}
