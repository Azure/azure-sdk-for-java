// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.config;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.Utils;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureServiceBusResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusMessagingAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.resourcemanager.provisioning.servicebus.ServiceBusProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.ServiceBusMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelResourceManagerProvisioner;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.CONFIGURATION_BUILDER_FOR_INTEGRATION_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(Binder.class)
@Import({
    AzureGlobalPropertiesAutoConfiguration.class,
    AzureResourceManagerAutoConfiguration.class,
    AzureServiceBusResourceManagerAutoConfiguration.class,
    AzureServiceBusMessagingAutoConfiguration.class,
    ServiceBusBinderHealthIndicatorConfiguration.class
})
@EnableConfigurationProperties(ServiceBusExtendedBindingProperties.class)
public class ServiceBusBinderConfiguration {

    private final Environment env;
    ServiceBusBinderConfiguration(Environment env) {
        this.env = env;
    }


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
    public ServiceBusChannelProvisioner serviceBusChannelArmProvisioner(AzureServiceBusProperties serviceBusProperties,
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
     * @param messageConverter the message Converter
     * @return ServiceBusMessageChannelBinder bean the Service Bus Message Channel Binder bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceBusMessageChannelBinder serviceBusBinder(ServiceBusChannelProvisioner channelProvisioner,
                                                           ServiceBusExtendedBindingProperties bindingProperties,
                                                           @Nullable ServiceBusMessageConverter messageConverter,
                                                           @Qualifier(CONFIGURATION_BUILDER_FOR_INTEGRATION_BEAN_NAME) ConfigurationBuilder configurationBuilder,
                                                           @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
                                                           Optional<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder>> senderBuilderCustomizer,
                                                           Optional<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder>> processorBuilderCustomizer) {

        Supplier<ServiceBusClientBuilder.ServiceBusSenderClientBuilder> senderBuilderSupplier = () -> {
            com.azure.core.util.Configuration section = null;
            // there maybe some room for improvement on SDK configuration here, but this is also not bad
            if (env.containsProperty("spring.cloud.azure.servicebus.producer.entity-name")) {
                section = configurationBuilder.buildSection("servicebus.producer");
            } else {
                section = configurationBuilder.buildSection("servicebus");
            }

            return Utils.configureBuilder(
                tempConfigSbSender(section, new ServiceBusClientBuilder()),
                section,
                defaultTokenCredential,
                senderBuilderCustomizer);
        };

        Supplier<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder> processorBuilderSupplier = () -> {
            com.azure.core.util.Configuration section = null;
            // there maybe some room for improvement on SDK configuration here, but this is also not bad
            if (env.containsProperty("spring.cloud.azure.servicebus.processor.entity-name")) {
                section = configurationBuilder.buildSection("servicebus.processor");
            } else {
                section = configurationBuilder.buildSection("servicebus");
            }

            return Utils.configureBuilder(
                tempConfigSbProcessor(section, new ServiceBusClientBuilder()),
                section,
                defaultTokenCredential,
                processorBuilderCustomizer);
        };

        ServiceBusMessageChannelBinder binder = new ServiceBusMessageChannelBinder(null, channelProvisioner, senderBuilderSupplier, processorBuilderSupplier);
        binder.setBindingProperties(bindingProperties);
        binder.setMessageConverter(messageConverter);
        return binder;
    }

    // TODO this and other properties will be done by ServiceBusClientBuilder, keeping it here for simplicity
    private ServiceBusClientBuilder.ServiceBusProcessorClientBuilder tempConfigSbProcessor(com.azure.core.util.Configuration configuration, ServiceBusClientBuilder builder) {
        builder.connectionString(configuration.get("connection-string"));
        builder.clientOptions(new ClientOptions().setApplicationId(configuration.get("amqp.client.application-id")));

        ServiceBusClientBuilder.ServiceBusProcessorClientBuilder proc = builder.processor();
        String entityType = configuration.get("entity-type");
        if (entityType == null) {
            // log error
            return proc;
        }

        if ("queue".equals(entityType)) {
            return proc.queueName(configuration.get("queue-name"));
        } else {
            proc.topicName(configuration.get("topic-name"));
            String subscName = configuration.get("subscription-name");
            if (subscName != null) {
                proc.subscriptionName(subscName);
            }
        }

        return proc;
    }

    private ServiceBusClientBuilder.ServiceBusSenderClientBuilder tempConfigSbSender(com.azure.core.util.Configuration configuration, ServiceBusClientBuilder builder) {
        builder.connectionString(configuration.get("connection-string"));
        builder.clientOptions(new ClientOptions().setApplicationId(configuration.get("amqp.client.application-id")));
        ServiceBusClientBuilder.ServiceBusSenderClientBuilder sender = builder.sender();

        String entityType = configuration.get("entity-type");
        if (entityType == null) {
            // log error
            return sender;
        }

        if ("queue".equals(entityType)) {
            return sender.queueName(configuration.get("queue-name"));
        }

        return sender.topicName(configuration.get("topic-name"));
    }
}
