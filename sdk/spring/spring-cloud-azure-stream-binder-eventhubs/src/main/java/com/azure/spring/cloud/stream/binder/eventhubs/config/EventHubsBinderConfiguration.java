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
import com.azure.spring.messaging.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.EventHubsProducerFactory;
import com.azure.spring.messaging.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.messaging.eventhubs.implementation.core.DefaultEventHubsNamespaceProcessorFactory;
import com.azure.spring.messaging.eventhubs.implementation.core.DefaultEventHubsNamespaceProducerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.stream.Collectors;

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
     * Declare Event Hubs Channel Provisioner bean.
     *
     * @param eventHubsProperties the event Hubs Properties
     * @param eventHubsProvisioner the event Hubs Provisioner
     * @return EventHubsChannelProvisioner bean the Event Hubs Channel Provisioner bean
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ EventHubsProvisioner.class, AzureEventHubsProperties.class })
    EventHubsChannelProvisioner eventHubChannelArmProvisioner(
        AzureEventHubsProperties eventHubsProperties, EventHubsProvisioner eventHubsProvisioner) {

        return new EventHubsChannelResourceManagerProvisioner(eventHubsProperties.getNamespace(),
                                                             eventHubsProvisioner);
    }

    /**
     * Declare Event Hubs Channel Provisioner bean.
     *
     * @return EventHubsChannelProvisioner bean the Event Hubs Channel Provisioner bean
     */
    @Bean
    @ConditionalOnMissingBean({ EventHubsProvisioner.class, EventHubsChannelProvisioner.class })
    public EventHubsChannelProvisioner eventHubChannelProvisioner() {
        return new EventHubsChannelProvisioner();
    }

    /**
     * Declare Event Hubs Message Channel Binder bean.
     *
     * @param channelProvisioner the channel Provisioner
     * @param bindingProperties the binding Properties
     * @param namespaceProperties the namespace Properties
     * @param checkpointStores the checkpoint Stores
     * @param customizers customizers to customize client factories
     * @return EventHubsMessageChannelBinder bean the Event Hubs Message Channel Binder bean
     */
    @Bean
    @ConditionalOnMissingBean
    public EventHubsMessageChannelBinder eventHubBinder(EventHubsChannelProvisioner channelProvisioner,
                                                        EventHubsExtendedBindingProperties bindingProperties,
                                                        ObjectProvider<NamespaceProperties> namespaceProperties,
                                                        ObjectProvider<CheckpointStore> checkpointStores,
                                                        ObjectProvider<ClientFactoryCustomizer> customizers) {
        EventHubsMessageChannelBinder binder = new EventHubsMessageChannelBinder(null, channelProvisioner);
        binder.setBindingProperties(bindingProperties);
        binder.setNamespaceProperties(namespaceProperties.getIfAvailable());
        checkpointStores.ifAvailable(binder::setCheckpointStore);
        binder.setClientFactoryCustomizers(customizers.orderedStream().collect(Collectors.toList()));
        return binder;
    }

    @Bean
    @ConditionalOnMissingBean
    ClientFactoryCustomizer defaultClientFactoryCustomizer(
        AzureTokenCredentialResolver azureTokenCredentialResolver,
        @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultAzureCredential,
        ObjectProvider<AzureServiceClientBuilderCustomizer<EventHubClientBuilder>> clientBuilderCustomizers,
        ObjectProvider<AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder>> processorClientBuilderCustomizers) {

        return new DefaultClientFactoryCustomizer(defaultAzureCredential, azureTokenCredentialResolver,
            clientBuilderCustomizers, processorClientBuilderCustomizers);
    }

    /**
     * The {@link ClientFactoryCustomizer} to configure the credential related properties.
     */
    static class DefaultClientFactoryCustomizer implements ClientFactoryCustomizer {

        private final TokenCredential defaultAzureCredential;
        private final AzureTokenCredentialResolver tokenCredentialResolver;
        private final ObjectProvider<AzureServiceClientBuilderCustomizer<EventHubClientBuilder>> clientBuilderCustomizers;
        private final ObjectProvider<AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder>> processorClientBuilderCustomizers;

        DefaultClientFactoryCustomizer(TokenCredential defaultAzureCredential,
                                       AzureTokenCredentialResolver azureTokenCredentialResolver,
                                       ObjectProvider<AzureServiceClientBuilderCustomizer<EventHubClientBuilder>> clientBuilderCustomizers,
                                       ObjectProvider<AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder>> processorClientBuilderCustomizers) {
            this.defaultAzureCredential = defaultAzureCredential;
            this.tokenCredentialResolver = azureTokenCredentialResolver;
            this.clientBuilderCustomizers = clientBuilderCustomizers;
            this.processorClientBuilderCustomizers = processorClientBuilderCustomizers;
        }

        @Override
        public void customize(EventHubsProducerFactory factory) {
            if (factory instanceof DefaultEventHubsNamespaceProducerFactory) {
                DefaultEventHubsNamespaceProducerFactory defaultFactory =
                    (DefaultEventHubsNamespaceProducerFactory) factory;

                defaultFactory.setDefaultAzureCredential(defaultAzureCredential);
                defaultFactory.setTokenCredentialResolver(tokenCredentialResolver);
                clientBuilderCustomizers.orderedStream().forEach(defaultFactory::addBuilderCustomizer);
            }
        }

        @Override
        public void customize(EventHubsProcessorFactory factory) {
            if (factory instanceof DefaultEventHubsNamespaceProcessorFactory) {
                DefaultEventHubsNamespaceProcessorFactory defaultFactory =
                    (DefaultEventHubsNamespaceProcessorFactory) factory;

                defaultFactory.setDefaultAzureCredential(defaultAzureCredential);
                defaultFactory.setTokenCredentialResolver(tokenCredentialResolver);
                processorClientBuilderCustomizers.orderedStream().forEach(defaultFactory::addBuilderCustomizer);
            }
        }

        ObjectProvider<AzureServiceClientBuilderCustomizer<EventHubClientBuilder>> getClientBuilderCustomizers() {
            return clientBuilderCustomizers;
        }

        ObjectProvider<AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder>> getProcessorClientBuilderCustomizers() {
            return processorClientBuilderCustomizers;
        }
    }

}
