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
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.ApplicationId;
import com.azure.spring.core.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_CONSUMER_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME;

/**
 * Configuration for a {@link ServiceBusSenderClient} or a {@link ServiceBusSenderAsyncClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus.consumer", name = { "queue-name", "topic-name" })
@Import({
    AzureServiceBusConsumerClientConfiguration.ShareConsumerConnectionConfiguration.class,
    AzureServiceBusConsumerClientConfiguration.DedicatedConsumerConnectionConfiguration.class,
    AzureServiceBusConsumerClientConfiguration.SessionConsumerClientConfiguration.class,
    AzureServiceBusConsumerClientConfiguration.NoneSessionConsumerClientConfiguration.class
})
class AzureServiceBusConsumerClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusConsumerClientConfiguration.class);

    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus.consumer", name = { "connection-string", "namespace" })
    @Configuration(proxyBeanMethods = false)
    static class DedicatedConsumerConnectionConfiguration {

        private final AzureServiceBusProperties.Consumer consumerProperties;

        DedicatedConsumerConnectionConfiguration(AzureServiceBusProperties serviceBusProperties) {
            this.consumerProperties = serviceBusProperties.buildConsumerProperties();
        }

        @Bean(SERVICE_BUS_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        @ConditionalOnMissingBean(name = SERVICE_BUS_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        public ServiceBusClientBuilderFactory serviceBusClientBuilderFactoryForConsumer() {

            final ServiceBusClientBuilderFactory builderFactory = new ServiceBusClientBuilderFactory(this.consumerProperties);

            builderFactory.setConnectionStringProvider(new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS,
                this.consumerProperties.getConnectionString()));
            builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_SERVICE_BUS);
            return builderFactory;
        }

        @Bean(SERVICE_BUS_CONSUMER_CLIENT_BUILDER_BEAN_NAME)
        @ConditionalOnMissingBean(name = SERVICE_BUS_CONSUMER_CLIENT_BUILDER_BEAN_NAME)
        public ServiceBusClientBuilder serviceBusClientBuilderForConsumer(
            @Qualifier(SERVICE_BUS_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME) ServiceBusClientBuilderFactory clientBuilderFactory) {

            return clientBuilderFactory.build();
        }

        @Bean
        @ConditionalOnMissingBean
        @ServiceBusSessionDisabled
        public ServiceBusClientBuilder.ServiceBusReceiverClientBuilder serviceBusReceiverClientBuilderForConsumer(
            @Qualifier(SERVICE_BUS_CONSUMER_CLIENT_BUILDER_BEAN_NAME) ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildReceiverClientBuilder(consumerProperties, serviceBusClientBuilder);
        }

        @Bean
        @ConditionalOnMissingBean
        @ServiceBusSessionEnabled
        public ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder serviceBusSessionReceiverClientBuilderForConsumer(
            @Qualifier(SERVICE_BUS_CONSUMER_CLIENT_BUILDER_BEAN_NAME) ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildSessionReceiverClientBuilder(consumerProperties, serviceBusClientBuilder);
        }

    }

    @ConditionalOnMissingProperty(prefix = "spring.cloud.azure.servicebus.consumer", name = { "connection-string", "namespace" })
    @Configuration(proxyBeanMethods = false)
    static class ShareConsumerConnectionConfiguration {

        @Bean
        @ConditionalOnMissingBean
        @ServiceBusSessionDisabled
        public ServiceBusClientBuilder.ServiceBusReceiverClientBuilder serviceBusReceiverClientBuilder(
            AzureServiceBusProperties serviceBusProperties, ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildReceiverClientBuilder(serviceBusProperties.getConsumer(), serviceBusClientBuilder);
        }

        @Bean
        @ConditionalOnMissingBean
        @ServiceBusSessionEnabled
        public ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder serviceBusSessionReceiverClientBuilder(
            AzureServiceBusProperties serviceBusProperties, ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildSessionReceiverClientBuilder(serviceBusProperties.getConsumer(), serviceBusClientBuilder);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ServiceBusSessionDisabled
    static class NoneSessionConsumerClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusReceiverAsyncClient serviceBusReceiverAsyncClient(
            ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildAsyncClient();
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusReceiverClient serviceBusReceiverClient(
            ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildClient();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ServiceBusSessionEnabled
    static class SessionConsumerClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusSessionReceiverAsyncClient serviceBusSessionReceiverAsyncClient(
            ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildAsyncClient();
        }

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusSessionReceiverClient serviceBusSessionReceiverClient(
            ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder) {
            return receiverClientBuilder.buildClient();
        }

    }

    private static ServiceBusClientBuilder.ServiceBusReceiverClientBuilder buildReceiverClientBuilder(
        AzureServiceBusProperties.Consumer consumerProperties, ServiceBusClientBuilder serviceBusClientBuilder) {
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

    private static ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder buildSessionReceiverClientBuilder(
        AzureServiceBusProperties.Consumer consumerProperties, ServiceBusClientBuilder serviceBusClientBuilder) {
        final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder sessionReceiverClientBuilder = serviceBusClientBuilder.sessionReceiver();

        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        propertyMapper.from(consumerProperties.getQueueName()).to(sessionReceiverClientBuilder::queueName);
        propertyMapper.from(consumerProperties.getTopicName()).to(sessionReceiverClientBuilder::topicName);
        propertyMapper.from(consumerProperties.getSubscriptionName()).to(sessionReceiverClientBuilder::subscriptionName);
        propertyMapper.from(consumerProperties.getReceiveMode()).to(sessionReceiverClientBuilder::receiveMode);
        propertyMapper.from(consumerProperties.getSubQueue()).to(sessionReceiverClientBuilder::subQueue);
        propertyMapper.from(consumerProperties.getPrefetchCount()).to(sessionReceiverClientBuilder::prefetchCount);
        propertyMapper.from(consumerProperties.getMaxAutoLockRenewDuration())
                      .to(sessionReceiverClientBuilder::maxAutoLockRenewDuration);
        propertyMapper.from(consumerProperties.getAutoComplete()).whenFalse().to(t -> sessionReceiverClientBuilder.disableAutoComplete());

        if (StringUtils.hasText(consumerProperties.getQueueName())
            && StringUtils.hasText(consumerProperties.getTopicName())
            && StringUtils.hasText(consumerProperties.getSubscriptionName())) {
            LOGGER.warn("Both queue and topic name configured for a service bus receiver, but only the queue name"
                + " will take effective");
        }
        return sessionReceiverClientBuilder;
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Documented
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.consumer.session-aware", havingValue = "true")
    private @interface ServiceBusSessionEnabled {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Documented
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.consumer.session-aware", havingValue = "false", matchIfMissing = true)
    private @interface ServiceBusSessionDisabled {
    }

}
