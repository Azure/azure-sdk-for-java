// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Config for AzureTelemetryActivation
 */
@Configuration(proxyBeanMethods = false)
public class AzureTelemetryActivationConfig {

    /**
     * Declare an AzureTelemetryActivation bean
     * @return AzureTelemetryActivation
     */
    @Bean
    public AzureTelemetryActivation azureTelemetryActivation() {
        return new AzureTelemetryActivation();
    }


}
