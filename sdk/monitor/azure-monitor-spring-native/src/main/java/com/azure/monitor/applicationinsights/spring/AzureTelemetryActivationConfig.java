// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AzureTelemetryActivationConfig {

    @Bean
    public AzureTelemetryActivation azureTelemetryActivation() {
        return new AzureTelemetryActivation();
    }


}
