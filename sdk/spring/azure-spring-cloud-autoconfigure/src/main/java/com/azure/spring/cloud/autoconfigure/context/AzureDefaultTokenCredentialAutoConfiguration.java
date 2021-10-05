// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.factory.AbstractAzureServiceClientBuilderFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 * Auto-configuration for Azure Spring default token credential.
 */
@Configuration
public class AzureDefaultTokenCredentialAutoConfiguration {

    private final AzureGlobalProperties azureGlobalProperties;

    public AzureDefaultTokenCredentialAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        this.azureGlobalProperties = azureGlobalProperties;
    }

    @Bean(name = DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)
    @ConditionalOnMissingBean(name = DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)
    @Order
    public TokenCredential azureTokenCredential(AzureCredentialBuilderFactory<DefaultAzureCredentialBuilder> factory) {
        return factory.build().build();
    }

    @Bean
    public AzureCredentialBuilderFactory<DefaultAzureCredentialBuilder> defaultAzureCredentialBuilderFactory() {
        return new AzureCredentialBuilderFactory<>(azureGlobalProperties, new DefaultAzureCredentialBuilder());
    }

    @Bean
    public AzureServiceClientBuilderFactoryPostProcessor builderFactoryBeanPostProcessor() {
        return new AzureServiceClientBuilderFactoryPostProcessor();
    }

    static class AzureServiceClientBuilderFactoryPostProcessor implements BeanPostProcessor {

        @Autowired
        @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)
        private TokenCredential tokenCredential;

        @SuppressWarnings("rawtypes")
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof AbstractAzureServiceClientBuilderFactory) {
                ((AbstractAzureServiceClientBuilderFactory) bean).setDefaultTokenCredential(tokenCredential);
            }
            return bean;
        }
    }
    
}
