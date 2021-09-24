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
import com.azure.spring.core.service.AzureServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_CONSUMER_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME;

/**
 * Configuration for a {@link ServiceBusSenderClient} or a {@link ServiceBusSenderAsyncClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnExpression(
    "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.consumer.queue-name:}') or "
        + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.consumer.topic-name:}')"
)
@Import({
    AzureServiceBusConsumerClientConfiguration.SessionConsumerClientConfiguration.class,
    AzureServiceBusConsumerClientConfiguration.NoneSessionConsumerClientConfiguration.class
})
class AzureServiceBusConsumerClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusConsumerClientConfiguration.class);

    private final AzureServiceBusProperties.Consumer consumerProperties;

    AzureServiceBusConsumerClientConfiguration(AzureServiceBusProperties serviceBusProperties) {
        this.consumerProperties = serviceBusProperties.buildConsumerProperties();
    }

    @Bean(SERVICE_BUS_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = SERVICE_BUS_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnExpression(
        "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.consumer.connection-string:}') or "
            + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.consumer.namespace:}')"
    )
    public ServiceBusClientBuilderFactory serviceBusClientBuilderFactoryForConsumer() {

        final ServiceBusClientBuilderFactory builderFactory = new ServiceBusClientBuilderFactory(this.consumerProperties);

        builderFactory.setConnectionStringProvider(new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS,
                                                                                        this.consumerProperties.getConnectionString()));
        builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_SERVICE_BUS);
        return builderFactory;
    }

    @Bean(SERVICE_BUS_CONSUMER_CLIENT_BUILDER_BEAN_NAME)
    @ConditionalOnBean(name = SERVICE_BUS_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = SERVICE_BUS_CONSUMER_CLIENT_BUILDER_BEAN_NAME)
    public ServiceBusClientBuilder serviceBusClientBuilderForConsumer(
        @Qualifier(SERVICE_BUS_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME) ServiceBusClientBuilderFactory clientBuilderFactory) {

        return clientBuilderFactory.build();
    }

    @ConditionalOnExpression("!${spring.cloud.azure.servicebus.consumer.session-aware:false}")
    static class NoneSessionConsumerClientConfiguration {

        @Bean
        @Conditional(ConditionOnGlobalClientBuilderAndMissingReceiverClientBuilder.class)
        public ServiceBusClientBuilder.ServiceBusReceiverClientBuilder serviceBusReceiverClientBuilder(
            AzureServiceBusProperties serviceBusProperties,
            ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildReceiverClientBuilder(serviceBusProperties, serviceBusClientBuilder);
        }

        @Bean
        @ConditionalOnBean(name = SERVICE_BUS_CONSUMER_CLIENT_BUILDER_BEAN_NAME)
        @ConditionalOnMissingBean(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class)
        public ServiceBusClientBuilder.ServiceBusReceiverClientBuilder serviceBusReceiverClientBuilderForConsumer(
            AzureServiceBusProperties serviceBusProperties,
            @Qualifier(SERVICE_BUS_CONSUMER_CLIENT_BUILDER_BEAN_NAME) ServiceBusClientBuilder serviceBusClientBuilder) {

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
            final AzureServiceBusProperties.Consumer consumerProperties = serviceBusProperties.getConsumer();
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
            propertyMapper.from(consumerProperties.getAutoComplete()).whenFalse().to(t -> receiverClientBuilder.disableAutoComplete());

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
        @ConditionalOnMissingBean(name = SERVICE_BUS_CONSUMER_CLIENT_BUILDER_BEAN_NAME, value = ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class)
        public ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder serviceBusSessionReceiverClientBuilder(
            AzureServiceBusProperties serviceBusProperties,
            ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildSessionReceiverClientBuilder(serviceBusProperties, serviceBusClientBuilder);
        }

        @Bean
        @ConditionalOnBean(name = SERVICE_BUS_CONSUMER_CLIENT_BUILDER_BEAN_NAME)
        @ConditionalOnMissingBean(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class)
        public ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder serviceBusSessionReceiverClientBuilderForProducer(
            AzureServiceBusProperties serviceBusProperties,
            @Qualifier(SERVICE_BUS_CONSUMER_CLIENT_BUILDER_BEAN_NAME) ServiceBusClientBuilder serviceBusClientBuilder) {

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
            final AzureServiceBusProperties.Consumer receiverProperties = serviceBusProperties.getConsumer();
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
            propertyMapper.from(receiverProperties.getAutoComplete()).whenFalse().to(t -> sessionReceiverClientBuilder.disableAutoComplete());

            if (StringUtils.hasText(receiverProperties.getQueueName())
                    && StringUtils.hasText(receiverProperties.getTopicName())
                    && StringUtils.hasText(receiverProperties.getSubscriptionName())) {
                LOGGER.warn("Both queue and topic name configured for a service bus receiver, but only the queue name"
                                + " will take effective");
            }
            return sessionReceiverClientBuilder;
        }
    }

    static class ConditionOnGlobalClientBuilderAndMissingReceiverClientBuilder extends AllNestedConditions {

        ConditionOnGlobalClientBuilderAndMissingReceiverClientBuilder() {

            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnMissingBean(name = SERVICE_BUS_CONSUMER_CLIENT_BUILDER_BEAN_NAME)
        static class OnMissingDedicatedClientBuilderForConsumer {
        }

        @ConditionalOnMissingBean(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class)
        static class OnMissingReceiverClientBuilder {
        }
    }

}
