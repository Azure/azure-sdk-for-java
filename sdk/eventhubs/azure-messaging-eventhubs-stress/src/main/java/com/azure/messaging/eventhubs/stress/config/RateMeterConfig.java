// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.config;

import com.azure.messaging.eventhubs.stress.util.ScenarioOptions;
import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for RateMeter
 */
@Configuration
public class RateMeterConfig {
    @Autowired
    private TelemetryClient telemetryClient;

    @Autowired
    private ScenarioOptions options;

    @Bean
    public RateMeter rateMeter() {
        return new RateMeter(telemetryClient, Duration.ofSeconds(
            options.getMetricIntervalSec())
        );
    }
}
