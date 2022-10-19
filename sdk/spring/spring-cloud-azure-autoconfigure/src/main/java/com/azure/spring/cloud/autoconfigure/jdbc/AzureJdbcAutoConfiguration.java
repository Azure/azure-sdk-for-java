// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.identity.providers.jdbc.implementation.template.AzureAuthenticationTemplate;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.SpringTokenCredentialProviderContextProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;


/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Identify JDBC support.
 * Provide Azure AD based authentication with Azure managed MySql and Postgresql services.
 *
 * @since 4.4.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(DataSourceProperties.class)
@ConditionalOnClass(AzureAuthenticationTemplate.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class AzureJdbcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @DependsOn("springTokenCredentialProviderContextProvider")
    JdbcPropertiesBeanPostProcessor jdbcConfigurationPropertiesBeanPostProcessor() {
        return new JdbcPropertiesBeanPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    SpringTokenCredentialProviderContextProvider springTokenCredentialProviderContextProvider() {
        return new SpringTokenCredentialProviderContextProvider();
    }

}
