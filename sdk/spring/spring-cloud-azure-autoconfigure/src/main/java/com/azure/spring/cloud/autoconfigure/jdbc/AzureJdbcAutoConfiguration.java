// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.SpringTokenCredentialProviderContextProvider;
import com.azure.spring.cloud.service.implementation.identity.AzureAuthenticationTemplate;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean(DataSourceProperties.class)
@ConditionalOnClass(AzureAuthenticationTemplate.class)
//TODO (zhihaoguo): Add test cases.
public class AzureJdbcAutoConfiguration {

    @Bean
    static JdbcPropertiesBeanPostProcessor jdbcConfigurationPropertiesBeanPostProcessor(
        AzureGlobalProperties azureGlobalProperties) {
        return new JdbcPropertiesBeanPostProcessor(azureGlobalProperties);
    }

    @Bean
    SpringTokenCredentialProviderContextProvider springTokenCredentialProviderContextProvider() {
        return new SpringTokenCredentialProviderContextProvider();
    }

}
