// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for TelemetryClient
 */
@Configuration
public class TelemetryConfig {
    @Bean
    public TelemetryClient telemetryClient() {
        return new TelemetryClient();
    }

}
