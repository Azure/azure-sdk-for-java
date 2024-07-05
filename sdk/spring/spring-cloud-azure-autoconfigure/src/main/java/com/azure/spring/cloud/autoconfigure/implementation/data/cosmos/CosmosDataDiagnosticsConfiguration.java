// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.cosmos;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "spring.cloud.azure.cosmos.populate-query-metrics", havingValue = "true")
class CosmosDataDiagnosticsConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosDataDiagnosticsConfiguration.class);

    /**
     * Default implementation for the {@link ResponseDiagnosticsProcessor}
     * @return the response diagnostics logs
     */
    @Bean
    @ConditionalOnMissingBean
    ResponseDiagnosticsProcessor responseDiagnosticsProcessor() {
        return responseDiagnostics -> LOGGER.info("Response Diagnostics {}", responseDiagnostics);
    }
}
