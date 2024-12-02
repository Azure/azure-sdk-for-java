// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;


/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Identify JDBC support.
 * Provide Azure AD based authentication with Azure managed MySql and Postgresql services.
 *
 * @since 4.5.0
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ConditionalOnBean(DataSourceProperties.class)
@ConditionalOnClass(AzureAuthenticationTemplate.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class AzureJdbcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static JdbcPropertiesBeanPostProcessor jdbcConfigurationPropertiesBeanPostProcessor() {
        return new JdbcPropertiesBeanPostProcessor();
    }
}
