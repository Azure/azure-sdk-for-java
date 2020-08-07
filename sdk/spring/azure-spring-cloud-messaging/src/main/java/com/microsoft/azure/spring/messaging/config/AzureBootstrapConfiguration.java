// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.messaging.config;

import com.microsoft.azure.spring.messaging.annotation.AzureMessageListener;
import com.microsoft.azure.spring.messaging.annotation.EnableAzureMessaging;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * {@code @Configuration} class that registers a {@link AzureListenerAnnotationBeanPostProcessor}
 * bean capable of processing Spring's @{@link AzureMessageListener} annotation. Also register
 * a default {@link AzureListenerEndpointRegistry}.
 *
 * <p>This configuration class is automatically imported when using the @{@link EnableAzureMessaging}
 * annotation. See the {@link EnableAzureMessaging} javadocs for complete usage details.
 *
 * @author Warren Zhu
 * @see AzureListenerAnnotationBeanPostProcessor
 * @see AzureListenerEndpointRegistry
 * @see EnableAzureMessaging
 */
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AzureBootstrapConfiguration {

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    public AzureListenerAnnotationBeanPostProcessor azureListenerAnnotationProcessor() {
        return new AzureListenerAnnotationBeanPostProcessor();
    }

    @Bean(name = AzureListenerAnnotationBeanPostProcessor.DEFAULT_AZURE_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME)
    public AzureListenerEndpointRegistry azureListenerEndpointRegistry() {
        return new AzureListenerEndpointRegistry();
    }

}
