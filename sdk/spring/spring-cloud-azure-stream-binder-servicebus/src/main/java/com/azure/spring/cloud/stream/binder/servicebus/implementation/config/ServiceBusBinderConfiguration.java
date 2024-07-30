// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.implementation.config;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.autoconfigure.implementation.resourcemanager.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.resourcemanager.AzureServiceBusResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.AzureServiceBusMessagingAutoConfiguration;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.ServiceBusProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.config.ServiceBusProcessorFactoryCustomizer;
import com.azure.spring.cloud.stream.binder.servicebus.config.ServiceBusProducerFactoryCustomizer;
import com.azure.spring.cloud.stream.binder.servicebus.implementation.ServiceBusMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.implementation.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.implementation.provisioning.ServiceBusChannelResourceManagerProvisioner;
import com.azure.spring.messaging.servicebus.core.DefaultServiceBusNamespaceProcessorFactory;
import com.azure.spring.messaging.servicebus.core.DefaultServiceBusNamespaceProducerFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusProducerFactory;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.implementation.support.converter.ServiceBusMessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(Binder.class)
@Import({
    AzureGlobalPropertiesAutoConfiguration.class,
    AzureTokenCredentialAutoConfiguration.class,
    AzureResourceManagerAutoConfiguration.class,
    AzureServiceBusResourceManagerAutoConfiguration.class,
    AzureServiceBusAutoConfiguration.class,
    AzureServiceBusMessagingAutoConfiguration.class,
    ServiceBusBinderHealthIndicatorConfiguration.class
})
@EnableConfigurationProperties(ServiceBusExtendedBindingProperties.class)
public class ServiceBusBinderConfiguration {

    /**
     * Declare the ARM implementation of {@link ServiceBusChannelProvisioner}.
     *
     * @param serviceBusProperties the service bus properties.
     * @param serviceBusProvisioner the service bus provisioner.
     *
     * @return the {@link ServiceBusChannelResourceManagerProvisioner} bean.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ ServiceBusProvisioner.class, AzureServiceBusProperties.class })
    ServiceBusChannelProvisioner serviceBusChannelArmProvisioner(AzureServiceBusProperties serviceBusProperties,
                                                                 ServiceBusProvisioner serviceBusProvisioner) {


        return new ServiceBusChannelResourceManagerProvisioner(serviceBusProperties.getNamespace(),
            serviceBusProvisioner);
    }

    /**
     * Declare {@link ServiceBusChannelProvisioner} bean.
     *
     * @return the {@link ServiceBusChannelProvisioner} bean.
     */
    @Bean
    @ConditionalOnMissingBean({ServiceBusProvisioner.class, ServiceBusChannelProvisioner.class})
    ServiceBusChannelProvisioner serviceBusChannelProvisioner() {
        return new ServiceBusChannelProvisioner();
    }

    /**
     * Declare the {@link ServiceBusMessageChannelBinder} bean.
     *
     * @param channelProvisioner the channel provisioner.
     * @param bindingProperties the binding properties.
     * @param namespaceProperties the namespace properties.
     * @param messageConverter the message converter.
     * @param producerFactoryCustomizers customizers to customize producer factories.
     * @param processorFactoryCustomizers customizers to customize processor factories.
     *
     * @return the {@link ServiceBusMessageChannelBinder} bean.
     */
    @Bean
    @ConditionalOnMissingBean
    ServiceBusMessageChannelBinder serviceBusBinder(ServiceBusChannelProvisioner channelProvisioner,
                                                           ServiceBusExtendedBindingProperties bindingProperties,
                                                           ObjectProvider<NamespaceProperties> namespaceProperties,
                                                           ObjectProvider<ServiceBusMessageConverter> messageConverter,
                                                           ObjectProvider<ServiceBusProducerFactoryCustomizer> producerFactoryCustomizers,
                                                           ObjectProvider<ServiceBusProcessorFactoryCustomizer> processorFactoryCustomizers) {

        ServiceBusMessageChannelBinder binder = new ServiceBusMessageChannelBinder(null, channelProvisioner);
        binder.setBindingProperties(bindingProperties);
        binder.setNamespaceProperties(namespaceProperties.getIfAvailable());
        binder.setMessageConverter(messageConverter.getIfAvailable());
        producerFactoryCustomizers.orderedStream().forEach(binder::addProducerFactoryCustomizer);
        processorFactoryCustomizers.orderedStream().forEach(binder::addProcessorFactoryCustomizer);
        return binder;
    }

    @Bean
    @ConditionalOnMissingBean
    ServiceBusProducerFactoryCustomizer defaultServiceBusProducerFactoryCustomizer(
        AzureTokenCredentialResolver azureTokenCredentialResolver,
        @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultAzureCredential,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder>> senderClientBuilderCustomizers) {

        return new DefaultProducerFactoryCustomizer(defaultAzureCredential, azureTokenCredentialResolver,
            senderClientBuilderCustomizers);
    }

    @Bean
    @ConditionalOnMissingBean
    ServiceBusProcessorFactoryCustomizer defaultServiceBusProcessorFactoryCustomizer(
        AzureTokenCredentialResolver azureTokenCredentialResolver,
        @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultAzureCredential,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> clientBuilderCustomizers,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder>> processorClientBuilderCustomizers,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder>> sessionProcessorClientBuilderCustomizers) {

        return new DefaultProcessorFactoryCustomizer(defaultAzureCredential, azureTokenCredentialResolver,
            clientBuilderCustomizers,
            processorClientBuilderCustomizers,
            sessionProcessorClientBuilderCustomizers);
    }

    /**
     * The default {@link ServiceBusProducerFactory} to configure the credential related properties and client builder customizers.
     */
    static class DefaultProducerFactoryCustomizer implements ServiceBusProducerFactoryCustomizer {

        private final TokenCredential defaultCredential;
        private final AzureTokenCredentialResolver tokenCredentialResolver;
        private final ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder>> senderClientBuilderCustomizers;

        DefaultProducerFactoryCustomizer(TokenCredential defaultCredential,
                                         AzureTokenCredentialResolver azureTokenCredentialResolver,
                                         ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder>> senderClientBuilderCustomizers) {
            this.defaultCredential = defaultCredential;
            this.tokenCredentialResolver = azureTokenCredentialResolver;
            this.senderClientBuilderCustomizers = senderClientBuilderCustomizers;
        }

        @Override
        public void customize(ServiceBusProducerFactory factory) {
            if (factory instanceof DefaultServiceBusNamespaceProducerFactory) {
                DefaultServiceBusNamespaceProducerFactory defaultFactory =
                    (DefaultServiceBusNamespaceProducerFactory) factory;

                defaultFactory.setDefaultCredential(defaultCredential);
                defaultFactory.setTokenCredentialResolver(tokenCredentialResolver);
                senderClientBuilderCustomizers.orderedStream().forEach(defaultFactory::addBuilderCustomizer);
            }
        }

        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder>> getSenderClientBuilderCustomizers() {
            return senderClientBuilderCustomizers;
        }

    }

    /**
     * The default {@link ServiceBusProducerFactory} to configure the credential related properties and client builder customizers.
     */
    static class DefaultProcessorFactoryCustomizer implements ServiceBusProcessorFactoryCustomizer {

        private final TokenCredential defaultCredential;
        private final AzureTokenCredentialResolver tokenCredentialResolver;
        private final ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> clientBuilderCustomizers;
        private final ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder>> processorClientBuilderCustomizers;
        private final ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder>> sessionProcessorClientBuilderCustomizers;

        DefaultProcessorFactoryCustomizer(TokenCredential defaultCredential,
                                          AzureTokenCredentialResolver azureTokenCredentialResolver,
                                          ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> clientBuilderCustomizers,
                                          ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder>> processorClientBuilderCustomizers,
                                          ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder>> sessionProcessorClientBuilderCustomizers) {
            this.defaultCredential = defaultCredential;
            this.tokenCredentialResolver = azureTokenCredentialResolver;
            this.clientBuilderCustomizers = clientBuilderCustomizers;
            this.processorClientBuilderCustomizers = processorClientBuilderCustomizers;
            this.sessionProcessorClientBuilderCustomizers = sessionProcessorClientBuilderCustomizers;
        }

        @Override
        public void customize(ServiceBusProcessorFactory factory) {
            if (factory instanceof DefaultServiceBusNamespaceProcessorFactory defaultFactory) {
                defaultFactory.setDefaultCredential(defaultCredential);
                defaultFactory.setTokenCredentialResolver(tokenCredentialResolver);
                clientBuilderCustomizers.orderedStream().forEach(defaultFactory::addServiceBusClientBuilderCustomizer);
                processorClientBuilderCustomizers.orderedStream().forEach(defaultFactory::addBuilderCustomizer);
                sessionProcessorClientBuilderCustomizers.orderedStream()
                                                        .forEach(defaultFactory::addSessionBuilderCustomizer);
            }

        }

        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> getClientBuilderCustomizers() {
            return clientBuilderCustomizers;
        }

        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder>> getProcessorClientBuilderCustomizers() {
            return processorClientBuilderCustomizers;
        }

        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder>> getSessionProcessorClientBuilderCustomizers() {
            return sessionProcessorClientBuilderCustomizers;
        }
    }


}
