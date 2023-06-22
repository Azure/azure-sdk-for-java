// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration(proxyBeanMethods = false)
public class OtelGlobalRegistrationConfig {

    /**
     *
     * @return return
     */
    @Bean
    public OtelGlobalRegistrationPostProcessor otelGlobalRegistrationPostProcessor() {
        return new OtelGlobalRegistrationPostProcessor();
    }

}
