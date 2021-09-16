// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.spring.core.ApplicationId;
import com.azure.spring.core.StaticConnectionStringProvider;
import com.azure.spring.core.properties.AzurePropertiesUtils;
import com.azure.spring.core.service.AzureServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

/**
 * Configuration for a {@link ServiceBusSenderClient} or a {@link ServiceBusSenderAsyncClient}.
 */
@Configuration(proxyBeanMethods = false)
@ServiceBusConditions.ConditionalOnServiceBusConsumer
@Import({
    AzureServiceBusConsumerClientConfiguration.SessionConsumerClientConfiguration.class,
    AzureServiceBusConsumerClientConfiguration.NoneSessionConsumerClientConfiguration.class
})
class AzureServiceBusConsumerClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusConsumerClientConfiguration.class);

    public static final String CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME = "com.azure.spring.cloud.autoconfigure.servicebus.CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME";
    public static final String CONSUMER_CLIENT_BUILDER_BEAN_NAME = "com.azure.spring.cloud.autoconfigure.servicebus.CONSUMER_CLIENT_BUILDER_BEAN_NAME";

    private final AzureServiceBusProperties serviceBusProperties;

    AzureServiceBusConsumerClientConfiguration(AzureServiceBusProperties serviceBusProperties) {
        this.serviceBusProperties = buildConsumerProperties(serviceBusProperties);
    }

    // TODO (xiada): this logic seems weird
    private AzureServiceBusProperties buildConsumerProperties(AzureServiceBusProperties source) {
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

        AzureServiceBusProperties target = new AzureServiceBusProperties();

        AzurePropertiesUtils.copyAzureProperties(source, target);
        propertyMapper.from(source.getConsumer().getDomainName()).to(target::setDomainName);
        propertyMapper.from(source.getConsumer().getNamespace()).to(target::setNamespace);
        propertyMapper.from(source.getConsumer().getConnectionString()).to(target::setConnectionString);

        return target;
    }

    @Bean(CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ServiceBusConditions.ConditionalOnDedicatedServiceBusConsumer
    public ServiceBusClientBuilderFactory serviceBusClientBuilderFactoryForConsumer() {

        final ServiceBusClientBuilderFactory builderFactory = new ServiceBusClientBuilderFactory(this.serviceBusProperties);

        builderFactory.setConnectionStringProvider(new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS,
                                                                                        this.serviceBusProperties.getConnectionString()));
        builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_SERVICE_BUS);
        return builderFactory;
    }

    @Bean(CONSUMER_CLIENT_BUILDER_BEAN_NAME)
    @ConditionalOnBean(name = CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = CONSUMER_CLIENT_BUILDER_BEAN_NAME)
    public ServiceBusClientBuilder serviceBusClientBuilderForConsumer(
        @Qualifier(CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME) ServiceBusClientBuilderFactory clientBuilderFactory) {

        return clientBuilderFactory.build();
    }

    @ConditionalOnExpression("!${spring.cloud.azure.servicebus.consumer.session-aware:false}")
    static class NoneSessionConsumerClientConfiguration {

        @Bean
        @Conditional(ServiceBusConditions.ConditionOnGlobalClientBuilderAndMissingReceiverClientBuilder.class)
        public ServiceBusClientBuilder.ServiceBusReceiverClientBuilder serviceBusReceiverClientBuilder(
            AzureServiceBusProperties serviceBusProperties,
            ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildReceiverClientBuilder(serviceBusProperties, serviceBusClientBuilder);
        }

        @Bean
        @ConditionalOnBean(name = CONSUMER_CLIENT_BUILDER_BEAN_NAME)
        @ConditionalOnMissingBean(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class)
        public ServiceBusClientBuilder.ServiceBusReceiverClientBuilder serviceBusReceiverClientBuilderForConsumer(
            AzureServiceBusProperties serviceBusProperties,
            @Qualifier(CONSUMER_CLIENT_BUILDER_BEAN_NAME) ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildReceiverClientBuilder(serviceBusProperties, serviceBusClientBuilder);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class)
        public ServiceBusReceiverAsyncClient serviceBusReceiverAsyncClient(
            ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildAsyncClient();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class)
        public ServiceBusReceiverClient serviceBusReceiverClient(
            ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildClient();
        }

        private ServiceBusClientBuilder.ServiceBusReceiverClientBuilder buildReceiverClientBuilder(
            AzureServiceBusProperties serviceBusProperties,
            ServiceBusClientBuilder serviceBusClientBuilder) {
            final AzureServiceBusProperties.ServiceBusConsumer consumerProperties = serviceBusProperties.getConsumer();
            final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder = serviceBusClientBuilder.receiver();

            PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
            propertyMapper.from(consumerProperties.getQueueName()).to(receiverClientBuilder::queueName);
            propertyMapper.from(consumerProperties.getTopicName()).to(receiverClientBuilder::topicName);
            propertyMapper.from(consumerProperties.getSubscriptionName()).to(receiverClientBuilder::subscriptionName);
            propertyMapper.from(consumerProperties.getReceiveMode()).to(receiverClientBuilder::receiveMode);
            propertyMapper.from(consumerProperties.getSubQueue()).to(receiverClientBuilder::subQueue);
            propertyMapper.from(consumerProperties.getPrefetchCount()).to(receiverClientBuilder::prefetchCount);
            propertyMapper.from(consumerProperties.getMaxAutoLockRenewDuration())
                          .to(receiverClientBuilder::maxAutoLockRenewDuration);
            propertyMapper.from(consumerProperties.isAutoComplete()).whenFalse().to(t -> receiverClientBuilder.disableAutoComplete());

            if (StringUtils.hasText(consumerProperties.getQueueName())
                    && StringUtils.hasText(consumerProperties.getTopicName())
                    && StringUtils.hasText(consumerProperties.getSubscriptionName())) {
                LOGGER.warn("Both queue and topic name configured for a service bus receiver, but only the queue name"
                                + " will take effective");
            }
            return receiverClientBuilder;
        }


    }

    @ConditionalOnExpression("${spring.cloud.azure.servicebus.consumer.session-aware:false}")
    static class SessionConsumerClientConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = CONSUMER_CLIENT_BUILDER_BEAN_NAME, value = ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class)
        public ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder serviceBusSessionReceiverClientBuilder(
            AzureServiceBusProperties serviceBusProperties,
            ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildSessionReceiverClientBuilder(serviceBusProperties, serviceBusClientBuilder);
        }

        @Bean
        @ConditionalOnBean(name = CONSUMER_CLIENT_BUILDER_BEAN_NAME)
        @ConditionalOnMissingBean(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class)
        public ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder serviceBusSessionReceiverClientBuilderForProducer(
            AzureServiceBusProperties serviceBusProperties,
            @Qualifier(CONSUMER_CLIENT_BUILDER_BEAN_NAME) ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildSessionReceiverClientBuilder(serviceBusProperties, serviceBusClientBuilder);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class)
        public ServiceBusSessionReceiverAsyncClient serviceBusSessionReceiverAsyncClient(
            ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildAsyncClient();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class)
        public ServiceBusSessionReceiverClient serviceBusSessionReceiverClient(
            ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildClient();
        }

        private ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder buildSessionReceiverClientBuilder(
            AzureServiceBusProperties serviceBusProperties,
            ServiceBusClientBuilder serviceBusClientBuilder) {
            final AzureServiceBusProperties.ServiceBusConsumer receiverProperties = serviceBusProperties.getConsumer();
            final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder sessionReceiverClientBuilder = serviceBusClientBuilder.sessionReceiver();

            PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
            propertyMapper.from(receiverProperties.getQueueName()).to(sessionReceiverClientBuilder::queueName);
            propertyMapper.from(receiverProperties.getTopicName()).to(sessionReceiverClientBuilder::topicName);
            propertyMapper.from(receiverProperties.getSubscriptionName()).to(sessionReceiverClientBuilder::subscriptionName);
            propertyMapper.from(receiverProperties.getReceiveMode()).to(sessionReceiverClientBuilder::receiveMode);
            propertyMapper.from(receiverProperties.getSubQueue()).to(sessionReceiverClientBuilder::subQueue);
            propertyMapper.from(receiverProperties.getPrefetchCount()).to(sessionReceiverClientBuilder::prefetchCount);
            propertyMapper.from(receiverProperties.getMaxAutoLockRenewDuration())
                          .to(sessionReceiverClientBuilder::maxAutoLockRenewDuration);
            propertyMapper.from(receiverProperties.isAutoComplete()).whenFalse().to(t -> sessionReceiverClientBuilder.disableAutoComplete());

            if (StringUtils.hasText(receiverProperties.getQueueName())
                    && StringUtils.hasText(receiverProperties.getTopicName())
                    && StringUtils.hasText(receiverProperties.getSubscriptionName())) {
                LOGGER.warn("Both queue and topic name configured for a service bus receiver, but only the queue name"
                                + " will take effective");
            }
            return sessionReceiverClientBuilder;
        }
    }

}
