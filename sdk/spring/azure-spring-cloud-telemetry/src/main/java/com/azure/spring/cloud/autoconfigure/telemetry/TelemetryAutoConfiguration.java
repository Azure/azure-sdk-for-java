// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.telemetry;

import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.azure.spring.cloud.telemetry.TelemetryCollector;
import com.azure.spring.cloud.telemetry.TelemetryProperties;
import com.azure.spring.cloud.telemetry.TelemetrySender;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:telemetry.config")
@EnableConfigurationProperties(TelemetryProperties.class)
@ConditionalOnProperty(name = "spring.cloud.azure.telemetry.enabled", matchIfMissing = true)
@ConditionalOnExpression("'${telemetry.instrumentationKey}' != '@telemetry.instrumentationKey@'")
public class TelemetryAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetryAutoConfiguration.class);

    @Autowired(required = false)
    private AzureTokenCredentials credentials;

    @Bean
    public TelemetrySender telemetrySender(TelemetryProperties telemetryProperties) {
        try {
            return new TelemetrySender(telemetryProperties.getInstrumentationKey(), TelemetryCollector.getInstance());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid argument to build telemetry tracker");
            return null;
        }
    }

    @Bean
    public TelemetryCollector telemetryCollector() {
        return TelemetryCollector.getInstance();
    }

    @PostConstruct
    private void initSubscription() {
        if (credentials != null) {
            this.telemetryCollector().setSubscription(credentials.defaultSubscriptionId());
        }
    }
}
