// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.core.ApplicationId;
import com.azure.spring.core.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_PRODUCER_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME;

/**
 * Configuration for a {@link ServiceBusReceiverClient} or a {@link ServiceBusReceiverAsyncClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.producer.queue-name:}') or "
                             + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.producer.topic-name:}')")
class AzureServiceBusProducerClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusProducerClientConfiguration.class);

    private final PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
    private final AzureServiceBusProperties.Producer producerProperties;

    AzureServiceBusProducerClientConfiguration(AzureServiceBusProperties serviceBusProperties) {
        this.producerProperties = serviceBusProperties.buildProducerProperties();
    }

    @Bean(SERVICE_BUS_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = SERVICE_BUS_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnExpression(
        "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.producer.connection-string:}') or "
            + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.producer.namespace:}')"
    )
    public ServiceBusClientBuilderFactory serviceBusClientBuilderFactoryForProducer() {

        final ServiceBusClientBuilderFactory builderFactory = new ServiceBusClientBuilderFactory(this.producerProperties);

        builderFactory.setConnectionStringProvider(new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS,
                                                                                        this.producerProperties.getConnectionString()));
        builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_SERVICE_BUS);

        return builderFactory;
    }

    @Bean(SERVICE_BUS_PRODUCER_CLIENT_BUILDER_BEAN_NAME)
    @ConditionalOnBean(name = SERVICE_BUS_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = SERVICE_BUS_PRODUCER_CLIENT_BUILDER_BEAN_NAME)
    public ServiceBusClientBuilder serviceBusClientBuilderForProducer(
        @Qualifier(SERVICE_BUS_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME) ServiceBusClientBuilderFactory clientBuilderFactory) {

        return clientBuilderFactory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = SERVICE_BUS_PRODUCER_CLIENT_BUILDER_BEAN_NAME)
    public ServiceBusClientBuilder.ServiceBusSenderClientBuilder serviceBusSenderClientBuilderForProducer(
        @Qualifier(SERVICE_BUS_PRODUCER_CLIENT_BUILDER_BEAN_NAME) ServiceBusClientBuilder serviceBusClientBuilder) {

        return buildServiceBusSenderClientBuilder(serviceBusClientBuilder);
    }

    @Bean
    @ConditionalOnMissingBean(name = SERVICE_BUS_PRODUCER_CLIENT_BUILDER_BEAN_NAME)
    @ConditionalOnBean(ServiceBusClientBuilder.class)
    public ServiceBusClientBuilder.ServiceBusSenderClientBuilder serviceBusSenderClientBuilder(ServiceBusClientBuilder serviceBusClientBuilder) {
        return buildServiceBusSenderClientBuilder(serviceBusClientBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusSenderClientBuilder.class)
    public ServiceBusSenderAsyncClient serviceBusSenderAsyncClient(ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderClientBuilder) {
        return senderClientBuilder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusSenderClientBuilder.class)
    public ServiceBusSenderClient serviceBusSenderClient(ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderClientBuilder) {
        return senderClientBuilder.buildClient();
    }

    private ServiceBusClientBuilder.ServiceBusSenderClientBuilder buildServiceBusSenderClientBuilder(ServiceBusClientBuilder builder) {

        final ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderClientBuilder = builder.sender();
        propertyMapper.from(this.producerProperties.getQueueName()).to(senderClientBuilder::queueName);
        propertyMapper.from(this.producerProperties.getTopicName()).to(senderClientBuilder::topicName);

        if (StringUtils.hasText(this.producerProperties.getQueueName())
                && StringUtils.hasText(this.producerProperties.getTopicName())) {
            LOGGER.warn(
                "Both queue and topic name configured for a service bus sender, but only the queue name will take effective");
        }

        return senderClientBuilder;
    }

}
