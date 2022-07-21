// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.config;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubsAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubsMessagingAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureEventHubsResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.resourcemanager.provisioning.EventHubsProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.EventHubsMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelResourceManagerProvisioner;
import com.azure.spring.messaging.eventhubs.core.DefaultEventHubsNamespaceProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.DefaultEventHubsNamespaceProducerFactory;
import com.azure.spring.messaging.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.EventHubsProducerFactory;
import com.azure.spring.messaging.eventhubs.core.properties.NamespaceProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(Binder.class)
@Import({
    AzureGlobalPropertiesAutoConfiguration.class,
    AzureTokenCredentialAutoConfiguration.class,
    AzureResourceManagerAutoConfiguration.class,
    AzureEventHubsResourceManagerAutoConfiguration.class,
    AzureEventHubsAutoConfiguration.class,
    AzureEventHubsMessagingAutoConfiguration.class,
    EventHubsBinderHealthIndicatorConfiguration.class
})
@EnableConfigurationProperties(EventHubsExtendedBindingProperties.class)
public class EventHubsBinderConfiguration {


    /**
     * Declare the ARM implementation of {@link EventHubsChannelProvisioner}.
     *
     * @param eventHubsProperties the event Hubs Properties.
     * @param eventHubsProvisioner the event Hubs Provisioner.
     *
     * @return the {@link EventHubsChannelResourceManagerProvisioner}.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ EventHubsProvisioner.class, AzureEventHubsProperties.class })
    EventHubsChannelProvisioner eventHubChannelArmProvisioner(
        AzureEventHubsProperties eventHubsProperties, EventHubsProvisioner eventHubsProvisioner) {

        return new EventHubsChannelResourceManagerProvisioner(eventHubsProperties.getNamespace(), eventHubsProvisioner);
    }

    /**
     * Declare the {@link EventHubsChannelProvisioner} bean.
     *
     * @return the {@link EventHubsChannelProvisioner} bean.
     */
    @Bean
    @ConditionalOnMissingBean({ EventHubsProvisioner.class, EventHubsChannelProvisioner.class })
    public EventHubsChannelProvisioner eventHubChannelProvisioner() {
        return new EventHubsChannelProvisioner();
    }

    /**
     * Declare the {@link EventHubsMessageChannelBinder} bean.
     *
     * @param channelProvisioner the channel provisioner.
     * @param bindingProperties the binding properties.
     * @param namespaceProperties the namespace properties.
     * @param checkpointStores the checkpoint stores.
     * @param producerFactoryCustomizers customizers to customize producer factories.
     * @param processorFactoryCustomizers customizers to customize processor factories.
     *
     * @return the {@link EventHubsMessageChannelBinder} bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public EventHubsMessageChannelBinder eventHubBinder(EventHubsChannelProvisioner channelProvisioner,
                                                        EventHubsExtendedBindingProperties bindingProperties,
                                                        ObjectProvider<NamespaceProperties> namespaceProperties,
                                                        ObjectProvider<CheckpointStore> checkpointStores,
                                                        ObjectProvider<EventHubsProducerFactoryCustomizer> producerFactoryCustomizers,
                                                        ObjectProvider<EventHubsProcessorFactoryCustomizer> processorFactoryCustomizers) {
        EventHubsMessageChannelBinder binder = new EventHubsMessageChannelBinder(null, channelProvisioner);
        binder.setBindingProperties(bindingProperties);
        binder.setNamespaceProperties(namespaceProperties.getIfAvailable());
        checkpointStores.ifAvailable(binder::setCheckpointStore);
        producerFactoryCustomizers.orderedStream().forEach(binder::addProducerFactoryCustomizer);
        processorFactoryCustomizers.orderedStream().forEach(binder::addProcessorFactoryCustomizer);
        return binder;
    }

    @Bean
    @ConditionalOnMissingBean
    EventHubsProducerFactoryCustomizer defaultEventHubsProducerFactoryCustomizer(
        AzureTokenCredentialResolver azureTokenCredentialResolver,
        @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultAzureCredential,
        ObjectProvider<AzureServiceClientBuilderCustomizer<EventHubClientBuilder>> clientBuilderCustomizers) {

        return new DefaultProducerFactoryCustomizer(defaultAzureCredential, azureTokenCredentialResolver, clientBuilderCustomizers);
    }

    @Bean
    @ConditionalOnMissingBean
    EventHubsProcessorFactoryCustomizer defaultEventHubsProcessorFactoryCustomizer(
        AzureTokenCredentialResolver azureTokenCredentialResolver,
        @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultCredential,
        ObjectProvider<AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder>> processorClientBuilderCustomizers) {

        return new DefaultProcessorFactoryCustomizer(defaultCredential, azureTokenCredentialResolver, processorClientBuilderCustomizers);
    }

    /**
     * The default {@link EventHubsProducerFactoryCustomizer} to configure the credential related properties and client builder customizers.
     */
    static class DefaultProducerFactoryCustomizer implements EventHubsProducerFactoryCustomizer {

        private final TokenCredential defaultCredential;
        private final AzureTokenCredentialResolver tokenCredentialResolver;
        private final ObjectProvider<AzureServiceClientBuilderCustomizer<EventHubClientBuilder>> clientBuilderCustomizers;

        DefaultProducerFactoryCustomizer(TokenCredential defaultCredential,
                                         AzureTokenCredentialResolver azureTokenCredentialResolver,
                                         ObjectProvider<AzureServiceClientBuilderCustomizer<EventHubClientBuilder>> clientBuilderCustomizers) {
            this.defaultCredential = defaultCredential;
            this.tokenCredentialResolver = azureTokenCredentialResolver;
            this.clientBuilderCustomizers = clientBuilderCustomizers;
        }

        @Override
        public void customize(EventHubsProducerFactory factory) {
            if (factory instanceof DefaultEventHubsNamespaceProducerFactory) {
                DefaultEventHubsNamespaceProducerFactory defaultFactory =
                    (DefaultEventHubsNamespaceProducerFactory) factory;

                defaultFactory.setDefaultCredential(defaultCredential);
                defaultFactory.setTokenCredentialResolver(tokenCredentialResolver);
                clientBuilderCustomizers.orderedStream().forEach(defaultFactory::addBuilderCustomizer);
            }
        }

        ObjectProvider<AzureServiceClientBuilderCustomizer<EventHubClientBuilder>> getClientBuilderCustomizers() {
            return clientBuilderCustomizers;
        }
    }

    /**
     * The default {@link EventHubsProcessorFactoryCustomizer} to configure the credential related properties and client builder customizers.
     */
    static class DefaultProcessorFactoryCustomizer implements EventHubsProcessorFactoryCustomizer {

        private final TokenCredential defaultCredential;
        private final AzureTokenCredentialResolver tokenCredentialResolver;
        private final ObjectProvider<AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder>> processorClientBuilderCustomizers;

        DefaultProcessorFactoryCustomizer(TokenCredential defaultCredential,
                                          AzureTokenCredentialResolver azureTokenCredentialResolver,
                                          ObjectProvider<AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder>> processorClientBuilderCustomizers) {
            this.defaultCredential = defaultCredential;
            this.tokenCredentialResolver = azureTokenCredentialResolver;
            this.processorClientBuilderCustomizers = processorClientBuilderCustomizers;
        }

        @Override
        public void customize(EventHubsProcessorFactory factory) {
            if (factory instanceof DefaultEventHubsNamespaceProcessorFactory) {
                DefaultEventHubsNamespaceProcessorFactory defaultFactory =
                    (DefaultEventHubsNamespaceProcessorFactory) factory;

                defaultFactory.setDefaultCredential(defaultCredential);
                defaultFactory.setTokenCredentialResolver(tokenCredentialResolver);
                processorClientBuilderCustomizers.orderedStream().forEach(defaultFactory::addBuilderCustomizer);
            }
        }

        ObjectProvider<AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder>> getProcessorClientBuilderCustomizers() {
            return processorClientBuilderCustomizers;
        }
    }

}
