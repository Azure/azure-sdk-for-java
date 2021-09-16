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
import com.azure.spring.core.properties.AzurePropertiesUtils;
import com.azure.spring.core.service.AzureServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Configuration for a {@link ServiceBusReceiverClient} or a {@link ServiceBusReceiverAsyncClient}.
 */
@Configuration(proxyBeanMethods = false)
@ServiceBusConditions.ConditionalOnServiceBusProducer
class AzureServiceBusProducerClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusProducerClientConfiguration.class);

    public static final String PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME = "com.azure.spring.cloud.autoconfigure.servicebus.PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME";
    public static final String PRODUCER_CLIENT_BUILDER_BEAN_NAME = "com.azure.spring.cloud.autoconfigure.servicebus.PRODUCER_CLIENT_BUILDER_BEAN_NAME";

    private final PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
    private final AzureServiceBusProperties serviceBusProperties;

    AzureServiceBusProducerClientConfiguration(AzureServiceBusProperties serviceBusProperties) {
        this.serviceBusProperties = buildProducerServiceBusProperties(serviceBusProperties);
    }

    // TODO (xiada): this logic seems weird
    private AzureServiceBusProperties buildProducerServiceBusProperties(AzureServiceBusProperties source) {
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

        AzureServiceBusProperties target = new AzureServiceBusProperties();

        AzurePropertiesUtils.copyAzureProperties(source, target);
        propertyMapper.from(source.getProducer().getDomainName()).to(target::setDomainName);
        propertyMapper.from(source.getProducer().getNamespace()).to(target::setNamespace);
        propertyMapper.from(source.getProducer().getConnectionString()).to(target::setConnectionString);
        propertyMapper.from(source.getProducer().getTopicName()).to(t -> target.getProducer().setTopicName(t));
        propertyMapper.from(source.getProducer().getQueueName()).to(t -> target.getProducer().setQueueName(t));

        return target;
    }

    @Bean(PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ServiceBusConditions.ConditionalOnDedicatedServiceBusProducer
    public ServiceBusClientBuilderFactory serviceBusClientBuilderFactoryForProducer() {

        final ServiceBusClientBuilderFactory builderFactory = new ServiceBusClientBuilderFactory(this.serviceBusProperties);

        builderFactory.setConnectionStringProvider(new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS,
                                                                                        this.serviceBusProperties.getConnectionString()));
        builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_SERVICE_BUS);

        return builderFactory;
    }

    @Bean(PRODUCER_CLIENT_BUILDER_BEAN_NAME)
    @ConditionalOnBean(name = PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = PRODUCER_CLIENT_BUILDER_BEAN_NAME)
    public ServiceBusClientBuilder serviceBusClientBuilderForProducer(
        @Qualifier(PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME) ServiceBusClientBuilderFactory clientBuilderFactory) {

        return clientBuilderFactory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = PRODUCER_CLIENT_BUILDER_BEAN_NAME)
    public ServiceBusClientBuilder.ServiceBusSenderClientBuilder serviceBusSenderClientBuilderForProducer(
        @Qualifier(PRODUCER_CLIENT_BUILDER_BEAN_NAME) ServiceBusClientBuilder serviceBusClientBuilder) {

        return buildServiceBusSenderClientBuilder(serviceBusClientBuilder);
    }

    @Bean
    @ConditionalOnMissingBean(name = PRODUCER_CLIENT_BUILDER_BEAN_NAME)
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
        propertyMapper.from(this.serviceBusProperties.getProducer().getQueueName()).to(senderClientBuilder::queueName);
        propertyMapper.from(this.serviceBusProperties.getProducer().getTopicName()).to(senderClientBuilder::topicName);

        if (StringUtils.hasText(this.serviceBusProperties.getProducer().getQueueName())
                && StringUtils.hasText(this.serviceBusProperties.getProducer().getTopicName())) {
            LOGGER.warn(
                "Both queue and topic name configured for a service bus sender, but only the queue name will take effective");
        }

        return senderClientBuilder;
    }

}
