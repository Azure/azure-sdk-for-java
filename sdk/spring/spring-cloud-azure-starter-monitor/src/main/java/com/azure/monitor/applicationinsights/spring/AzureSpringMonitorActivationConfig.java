// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Config for AzureSpringMonitorActivation
 */
@Configuration(proxyBeanMethods = false)
public class AzureSpringMonitorActivationConfig {

    /**
     * Declare an AzureSpringMonitorActivation bean
     * @return AzureSpringMonitorActivation
     */
    @Bean
    public AzureSpringMonitorActivation azureSpringMonitorActivation() {
        return new AzureSpringMonitorActivation();
    }


}
