// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.service.implementation.identity.impl.credential.provider.SpringTokenCredentialProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Identify JDBC support.
 * Provide Azure AD based authentication with Azure managed MySql and Postgresql services.
 *
 * @since 4.4.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(DataSourceProperties.class)
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

    @Bean
    SpringTokenCredentialProviderContextProvider springTokenCredentialProviderContextProvider() {
        return new SpringTokenCredentialProviderContextProvider();
    }

}
