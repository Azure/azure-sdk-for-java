// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.context;

import com.azure.spring.cloud.core.implementation.factory.AbstractAzureServiceClientBuilderFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * {@code @Configuration} class that registers a {@link AzureServiceClientBuilderFactoryPostProcessor}
 * bean capable of processing the {@link AbstractAzureServiceClientBuilderFactory } bean.
 *
 * @since 5.19.0
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
class AzureServiceClientBuilderFactoryConfiguration {

    /**
     * The BeanPostProcessor to apply the default token credential and resolver to all service client builder factories.
     * @return the BPP.
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static AzureServiceClientBuilderFactoryPostProcessor builderFactoryBeanPostProcessor() {
        return new AzureServiceClientBuilderFactoryPostProcessor();
    }
}
