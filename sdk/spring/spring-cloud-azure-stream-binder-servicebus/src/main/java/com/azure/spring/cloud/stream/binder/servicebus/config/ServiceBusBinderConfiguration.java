// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.config;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureServiceBusResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusMessagingAutoConfiguration;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.resourcemanager.provisioning.ServiceBusProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.ServiceBusMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelResourceManagerProvisioner;
import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusProducerFactory;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.implementation.core.DefaultServiceBusNamespaceProcessorFactory;
import com.azure.spring.messaging.servicebus.implementation.core.DefaultServiceBusNamespaceProducerFactory;
import com.azure.spring.messaging.servicebus.support.converter.ServiceBusMessageConverter;
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
    AzureServiceBusResourceManagerAutoConfiguration.class,
    AzureServiceBusAutoConfiguration.class,
    AzureServiceBusMessagingAutoConfiguration.class,
    ServiceBusBinderHealthIndicatorConfiguration.class
})
@EnableConfigurationProperties(ServiceBusExtendedBindingProperties.class)
public class ServiceBusBinderConfiguration {

    /**
     * Declare Service Bus Channel Provisioner bean.
     *
     * @param serviceBusProperties the service bus properties
     * @param serviceBusProvisioner the service bus provisioner
     * @return ServiceBusChannelProvisioner bean the Service Bus Channel Provisioner bean
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
     * Declare Service Bus Channel Provisioner bean.
     *
     * @return ServiceBusChannelProvisioner bean the Service Bus Channel Provisioner bean
     */
    @Bean
    @ConditionalOnMissingBean({ServiceBusProvisioner.class, ServiceBusChannelProvisioner.class})
    public ServiceBusChannelProvisioner serviceBusChannelProvisioner() {
        return new ServiceBusChannelProvisioner();
    }

    /**
     * Declare Service Bus Message Channel Binder bean.
     *
     * @param channelProvisioner the channel Provisioner
     * @param bindingProperties the binding Properties
     * @param namespaceProperties the namespace Properties
     * @param messageConverter the message Converter
     * @param customizers the client factory customizers
     * @return ServiceBusMessageChannelBinder bean the Service Bus Message Channel Binder bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceBusMessageChannelBinder serviceBusBinder(ServiceBusChannelProvisioner channelProvisioner,
                                                           ServiceBusExtendedBindingProperties bindingProperties,
                                                           ObjectProvider<NamespaceProperties> namespaceProperties,
                                                           ObjectProvider<ServiceBusMessageConverter> messageConverter,
                                                           ObjectProvider<ClientFactoryCustomizer> customizers) {

        ServiceBusMessageChannelBinder binder = new ServiceBusMessageChannelBinder(null, channelProvisioner);
        binder.setBindingProperties(bindingProperties);
        binder.setNamespaceProperties(namespaceProperties.getIfAvailable());
        binder.setMessageConverter(messageConverter.getIfAvailable());
        binder.setClientFactoryCustomizers(customizers.orderedStream().collect(Collectors.toList()));
        return binder;
    }

    @Bean
    @ConditionalOnMissingBean
    ClientFactoryCustomizer defaultClientFactoryCustomizer(
        AzureTokenCredentialResolver azureTokenCredentialResolver,
        @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultAzureCredential,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder>> senderClientBuilderCustomizers,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder>> processorClientBuilderCustomizers,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder>> sessionProcessorClientBuilderCustomizers) {

        return new DefaultClientFactoryCustomizer(defaultAzureCredential, azureTokenCredentialResolver,
            senderClientBuilderCustomizers,
            processorClientBuilderCustomizers,
            sessionProcessorClientBuilderCustomizers);
    }

    /**
     * The {@link ClientFactoryCustomizer} to configure the credential related properties and client builder customizers.
     */
    static class DefaultClientFactoryCustomizer implements ClientFactoryCustomizer {

        private final TokenCredential defaultAzureCredential;
        private final AzureTokenCredentialResolver tokenCredentialResolver;
        private final ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder>> senderClientBuilderCustomizers;
        private final ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder>> processorClientBuilderCustomizers;
        private final ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder>> sessionProcessorClientBuilderCustomizers;

        DefaultClientFactoryCustomizer(TokenCredential defaultAzureCredential,
                                       AzureTokenCredentialResolver azureTokenCredentialResolver,
                                       ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder>> senderClientBuilderCustomizers,
                                       ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder>> processorClientBuilderCustomizers,
                                       ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder>> sessionProcessorClientBuilderCustomizers) {
            this.defaultAzureCredential = defaultAzureCredential;
            this.tokenCredentialResolver = azureTokenCredentialResolver;
            this.senderClientBuilderCustomizers = senderClientBuilderCustomizers;
            this.processorClientBuilderCustomizers = processorClientBuilderCustomizers;
            this.sessionProcessorClientBuilderCustomizers = sessionProcessorClientBuilderCustomizers;
        }

        @Override
        public void customize(ServiceBusProducerFactory factory) {
            if (factory instanceof DefaultServiceBusNamespaceProducerFactory) {
                DefaultServiceBusNamespaceProducerFactory defaultFactory =
                    (DefaultServiceBusNamespaceProducerFactory) factory;

                defaultFactory.setDefaultAzureCredential(defaultAzureCredential);
                defaultFactory.setTokenCredentialResolver(tokenCredentialResolver);
                senderClientBuilderCustomizers.orderedStream().forEach(defaultFactory::addBuilderCustomizer);
            }
        }

        @Override
        public void customize(ServiceBusProcessorFactory factory) {
            if (factory instanceof DefaultServiceBusNamespaceProcessorFactory) {
                DefaultServiceBusNamespaceProcessorFactory defaultFactory =
                    (DefaultServiceBusNamespaceProcessorFactory) factory;

                defaultFactory.setDefaultAzureCredential(defaultAzureCredential);
                defaultFactory.setTokenCredentialResolver(tokenCredentialResolver);
                processorClientBuilderCustomizers
                    .orderedStream()
                    .map(c -> new DefaultServiceBusNamespaceProcessorFactory.ServiceBusProcessClientBuilderCustomizer(c, null))
                    .forEach(defaultFactory::addBuilderCustomizer);
                sessionProcessorClientBuilderCustomizers
                    .orderedStream()
                    .map(c -> new DefaultServiceBusNamespaceProcessorFactory.ServiceBusProcessClientBuilderCustomizer(null, c))
                    .forEach(defaultFactory::addBuilderCustomizer);
            }

        }

        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder>> getSenderClientBuilderCustomizers() {
            return senderClientBuilderCustomizers;
        }

        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder>> getProcessorClientBuilderCustomizers() {
            return processorClientBuilderCustomizers;
        }

        ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder>> getSessionProcessorClientBuilderCustomizers() {
            return sessionProcessorClientBuilderCustomizers;
        }
    }


}
