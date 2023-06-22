// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration(proxyBeanMethods = false)
public class JvmMetricsConfig {

    /**
     *
     * @return return
     */
    // @Bean
    public JvmMetricsPostProcessor jvmMetricsPostProcessor() {
        return new JvmMetricsPostProcessor();
    }

}
