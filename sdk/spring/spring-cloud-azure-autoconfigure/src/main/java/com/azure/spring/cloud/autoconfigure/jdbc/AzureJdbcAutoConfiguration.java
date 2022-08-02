// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.service.implementation.identity.impl.credential.provider.SpringTokenCredentialProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AzureJdbcAutoConfiguration {

    @Bean
    static JdbcPropertiesBeanPostProcessor jdbcConfigurationPropertiesBeanPostProcessor(
           AzureGlobalProperties azureGlobalProperties) {
        return new JdbcPropertiesBeanPostProcessor(azureGlobalProperties);
    }

    @Bean
    SpringTokenCredentialProvider springTokenCredentialProvider() {
        return new SpringTokenCredentialProvider();
    }

}
