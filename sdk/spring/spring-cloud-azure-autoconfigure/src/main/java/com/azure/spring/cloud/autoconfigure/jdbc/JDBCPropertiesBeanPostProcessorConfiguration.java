// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "spring.datasource.url", matchIfMissing = true)
public class JDBCPropertiesBeanPostProcessorConfiguration {

    @Bean
    static JDBCPropertiesBeanPostProcessor jdbcConfigurationPropertiesBeanPostProcessor(
           AzureGlobalProperties azureGlobalProperties) {
        return new JDBCPropertiesBeanPostProcessor(azureGlobalProperties);
    }
}
