// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.config;

import com.azure.spring.messaging.implementation.annotation.AzureListenerAnnotationBeanPostProcessorAdapter;
import com.azure.spring.messaging.implementation.annotation.EnableAzureMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

/**
 * {@code @Configuration} class that registers implementation classes of
 * {@link AzureListenerAnnotationBeanPostProcessorAdapter} bean capable of processing Spring's Azure Message Listener
 * annotation. Also register a default {@link AzureListenerEndpointRegistry}.
 *
 * <p>This configuration class is automatically imported when using the @{@link EnableAzureMessaging}
 * annotation. See the {@link EnableAzureMessaging} javadocs for complete usage details.
 *
 * @see AzureListenerAnnotationBeanPostProcessorAdapter
 * @see AzureListenerEndpointRegistry
 * @see EnableAzureMessaging
 */
public class AzureMessagingBootstrapConfiguration implements ImportBeanDefinitionRegistrar {

    public static final Logger LOGGER = LoggerFactory.getLogger(AzureMessagingBootstrapConfiguration.class);

    @Override
    @SuppressWarnings("rawtypes")
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        List<AzureListenerAnnotationBeanPostProcessorAdapter> bpps =
            SpringFactoriesLoader.loadFactories(AzureListenerAnnotationBeanPostProcessorAdapter.class,
                SpringFactoriesLoader.class.getClassLoader());


        for (AzureListenerAnnotationBeanPostProcessorAdapter bpp : bpps) {

            if (!registry.containsBeanDefinition(bpp.getDefaultAzureListenerAnnotationBeanPostProcessorBeanName())) {

                registry.registerBeanDefinition(bpp.getDefaultAzureListenerAnnotationBeanPostProcessorBeanName(),
                    new RootBeanDefinition(bpp.getClass()));
            }
        }

        if (!registry.containsBeanDefinition(AzureListenerAnnotationBeanPostProcessorAdapter.DEFAULT_AZURE_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME)) {
            registry.registerBeanDefinition(AzureListenerAnnotationBeanPostProcessorAdapter.DEFAULT_AZURE_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME,
                new RootBeanDefinition(AzureListenerEndpointRegistry.class));
        }
    }

}
