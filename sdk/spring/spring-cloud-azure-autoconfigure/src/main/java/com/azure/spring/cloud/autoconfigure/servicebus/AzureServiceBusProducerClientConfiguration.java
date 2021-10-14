// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.ApplicationId;
import com.azure.spring.core.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_PRODUCER_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME;

/**
 * Configuration for a {@link ServiceBusReceiverClient} or a {@link ServiceBusReceiverAsyncClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus.producer", name = { "topic-name", "queue-name" })
@Import({
    AzureServiceBusProducerClientConfiguration.SharedProducerConnectionConfiguration.class,
    AzureServiceBusProducerClientConfiguration.DedicatedProducerConnectionConfiguration.class,
    AzureServiceBusProducerClientConfiguration.ProducerClientConfiguration.class,
})
class AzureServiceBusProducerClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusProducerClientConfiguration.class);

    @ConditionalOnMissingProperty(prefix = "spring.cloud.azure.servicebus.producer", name = { "connection-string", "namespace" })
    @ConditionalOnBean(ServiceBusClientBuilder.class)
    @Configuration(proxyBeanMethods = false)
    static class SharedProducerConnectionConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusClientBuilder.ServiceBusSenderClientBuilder serviceBusSenderClientBuilder(
            AzureServiceBusProperties serviceBusProperties, ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildServiceBusSenderClientBuilder(serviceBusProperties.getProducer(), serviceBusClientBuilder);
        }

    }

    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus.producer", name = { "connection-string", "namespace" })
    @Configuration(proxyBeanMethods = false)
    static class DedicatedProducerConnectionConfiguration {

        private final AzureServiceBusProperties.Producer producerProperties;

        DedicatedProducerConnectionConfiguration(AzureServiceBusProperties serviceBusProperties) {
            this.producerProperties = serviceBusProperties.buildProducerProperties();
        }

        @Bean(SERVICE_BUS_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        @ConditionalOnMissingBean(name = SERVICE_BUS_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        public ServiceBusClientBuilderFactory serviceBusClientBuilderFactoryForProducer() {
            final ServiceBusClientBuilderFactory builderFactory = new ServiceBusClientBuilderFactory(this.producerProperties);

            builderFactory.setConnectionStringProvider(new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS,
                this.producerProperties.getConnectionString()));
            builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_SERVICE_BUS);

            return builderFactory;
        }

        @Bean(SERVICE_BUS_PRODUCER_CLIENT_BUILDER_BEAN_NAME)
        @ConditionalOnMissingBean(name = SERVICE_BUS_PRODUCER_CLIENT_BUILDER_BEAN_NAME)
        public ServiceBusClientBuilder serviceBusClientBuilderForProducer(
            @Qualifier(SERVICE_BUS_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME) ServiceBusClientBuilderFactory clientBuilderFactory) {

            return clientBuilderFactory.build();
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusClientBuilder.ServiceBusSenderClientBuilder serviceBusSenderClientBuilderForProducer(
            @Qualifier(SERVICE_BUS_PRODUCER_CLIENT_BUILDER_BEAN_NAME) ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildServiceBusSenderClientBuilder(this.producerProperties, serviceBusClientBuilder);
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class ProducerClientConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public ServiceBusSenderAsyncClient serviceBusSenderAsyncClient(ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderClientBuilder) {
            return senderClientBuilder.buildAsyncClient();
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusSenderClient serviceBusSenderClient(ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderClientBuilder) {
            return senderClientBuilder.buildClient();
        }
    }

    private static ServiceBusClientBuilder.ServiceBusSenderClientBuilder buildServiceBusSenderClientBuilder(
        AzureServiceBusProperties.Producer producerProperties, ServiceBusClientBuilder builder) {

        final PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        final ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderClientBuilder = builder.sender();

        propertyMapper.from(producerProperties.getQueueName()).to(senderClientBuilder::queueName);
        propertyMapper.from(producerProperties.getTopicName()).to(senderClientBuilder::topicName);

        if (StringUtils.hasText(producerProperties.getQueueName()) && StringUtils.hasText(producerProperties.getTopicName())) {
            LOGGER.warn("Both queue and topic name configured for a service bus sender, but only the queue name will take effective");
        }

        return senderClientBuilder;
    }

}
