// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.core.ApplicationId;
import com.azure.spring.core.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.servicebus.core.ServiceBusMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_FACTORY_BEAN_NAME;

/**
 * Configuration for a {@link ServiceBusProcessorClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(ServiceBusMessageProcessor.class)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus.processor", name = { "queue-name", "topic-name" })
@Import({
    AzureServiceBusProcessorConfiguration.ShareProcessorConnectionConfiguration.class,
    AzureServiceBusProcessorConfiguration.DedicatedProcessorConnectionConfiguration.class,
    AzureServiceBusProcessorConfiguration.SessionProcessorClientConfiguration.class,
    AzureServiceBusProcessorConfiguration.NoneSessionProcessorClientConfiguration.class
})
class AzureServiceBusProcessorConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusProcessorConfiguration.class);

    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus.processor", name = { "connection-string", "namespace" })
    @Configuration(proxyBeanMethods = false)
    static class DedicatedProcessorConnectionConfiguration {

        private final AzureServiceBusProperties.Processor processorProperties;

        DedicatedProcessorConnectionConfiguration(AzureServiceBusProperties serviceBusProperties) {
            this.processorProperties = serviceBusProperties.buildProcessorProperties();
        }

        @Bean(SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        @ConditionalOnMissingBean(name = SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        public ServiceBusClientBuilderFactory serviceBusClientBuilderFactoryForProcessor() {

            final ServiceBusClientBuilderFactory builderFactory = new ServiceBusClientBuilderFactory(this.processorProperties);
            builderFactory.setConnectionStringProvider(new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS,
                this.processorProperties.getConnectionString()));
            builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_SERVICE_BUS);

            return builderFactory;
        }

        @Bean(SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME)
        @ConditionalOnMissingBean(name = SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME)
        public ServiceBusClientBuilder serviceBusClientBuilderForProcessor(
            @Qualifier(SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_FACTORY_BEAN_NAME) ServiceBusClientBuilderFactory clientBuilderFactory) {

            return clientBuilderFactory.build();
        }

        @Bean
        @ConditionalOnMissingBean
        @ServiceBusSessionDisabled
        public ServiceBusClientBuilder.ServiceBusProcessorClientBuilder serviceBusProcessorClientBuilderForProcessor(
            @Qualifier(SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME) ServiceBusClientBuilder serviceBusClientBuilder) {
            return buildProcessorClientBuilder(this.processorProperties, serviceBusClientBuilder);
        }

        @Bean
        @ConditionalOnMissingBean
        @ServiceBusSessionEnabled
        public ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder serviceBusSessionProcessorClientBuilderForProcessor(
            @Qualifier(SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME) ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildSessionProcessorClientBuilder(this.processorProperties, serviceBusClientBuilder);
        }

    }

    @ConditionalOnMissingProperty(prefix = "spring.cloud.azure.servicebus.processor", name = { "connection-string", "namespace" })
    @Configuration(proxyBeanMethods = false)
    static class ShareProcessorConnectionConfiguration {

        @Bean
        @ConditionalOnMissingBean
        @ServiceBusSessionDisabled
        public ServiceBusClientBuilder.ServiceBusProcessorClientBuilder serviceBusProcessorClientBuilder(
            AzureServiceBusProperties serviceBusProperties, ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildProcessorClientBuilder(serviceBusProperties.getProcessor(), serviceBusClientBuilder);
        }

        @Bean
        @ConditionalOnMissingBean
        @ServiceBusSessionEnabled
        public ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder serviceBusSessionProcessorClientBuilder(
            AzureServiceBusProperties serviceBusProperties, ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildSessionProcessorClientBuilder(serviceBusProperties.getProcessor(), serviceBusClientBuilder);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ServiceBusSessionDisabled
    static class NoneSessionProcessorClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusProcessorClient serviceBusProcessorClient(
            ServiceBusMessageProcessor serviceBusMessageProcessor,
            ServiceBusClientBuilder.ServiceBusProcessorClientBuilder processorClientBuilder) {

            processorClientBuilder.processError(serviceBusMessageProcessor.processError());
            processorClientBuilder.processMessage(serviceBusMessageProcessor.processMessage());
            return processorClientBuilder.buildProcessorClient();
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ServiceBusSessionEnabled
    static class SessionProcessorClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ServiceBusProcessorClient serviceBusSessionProcessorClient(
            ServiceBusMessageProcessor serviceBusMessageProcessor,
            ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder sessionProcessorClientBuilder) {

            sessionProcessorClientBuilder.processError(serviceBusMessageProcessor.processError());
            sessionProcessorClientBuilder.processMessage(serviceBusMessageProcessor.processMessage());

            return sessionProcessorClientBuilder.buildProcessorClient();
        }

    }

    private static ServiceBusClientBuilder.ServiceBusProcessorClientBuilder buildProcessorClientBuilder(
        AzureServiceBusProperties.Processor processorProperties, ServiceBusClientBuilder serviceBusClientBuilder) {
        final ServiceBusClientBuilder.ServiceBusProcessorClientBuilder processorClientBuilder = serviceBusClientBuilder.processor();

        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

        propertyMapper.from(processorProperties.getQueueName()).to(processorClientBuilder::queueName);
        propertyMapper.from(processorProperties.getTopicName()).to(processorClientBuilder::topicName);
        propertyMapper.from(processorProperties.getSubscriptionName()).to(processorClientBuilder::subscriptionName);
        propertyMapper.from(processorProperties.getReceiveMode()).to(processorClientBuilder::receiveMode);
        propertyMapper.from(processorProperties.getSubQueue()).to(processorClientBuilder::subQueue);
        propertyMapper.from(processorProperties.getPrefetchCount()).to(processorClientBuilder::prefetchCount);
        propertyMapper.from(processorProperties.getMaxAutoLockRenewDuration()).to(processorClientBuilder::maxAutoLockRenewDuration);
        propertyMapper.from(processorProperties.getAutoComplete()).whenFalse().to(t -> processorClientBuilder.disableAutoComplete());
        propertyMapper.from(processorProperties.getMaxConcurrentCalls()).to(processorClientBuilder::maxConcurrentCalls);

        if (StringUtils.hasText(processorProperties.getQueueName())
            && StringUtils.hasText(processorProperties.getTopicName())
            && StringUtils.hasText(processorProperties.getSubscriptionName())) {
            LOGGER.warn(
                "Both queue and topic name configured for a service bus processor, but only the queue name will take effective");
        }

        return processorClientBuilder;
    }

    private static ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder buildSessionProcessorClientBuilder(
        AzureServiceBusProperties.Processor processorProperties, ServiceBusClientBuilder serviceBusClientBuilder) {

        final ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder sessionProcessorClientBuilder = serviceBusClientBuilder.sessionProcessor();

        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        propertyMapper.from(processorProperties.getQueueName()).to(sessionProcessorClientBuilder::queueName);
        propertyMapper.from(processorProperties.getTopicName()).to(sessionProcessorClientBuilder::topicName);
        propertyMapper.from(processorProperties.getSubscriptionName()).to(sessionProcessorClientBuilder::subscriptionName);
        propertyMapper.from(processorProperties.getReceiveMode()).to(sessionProcessorClientBuilder::receiveMode);
        propertyMapper.from(processorProperties.getSubQueue()).to(sessionProcessorClientBuilder::subQueue);
        propertyMapper.from(processorProperties.getPrefetchCount()).to(sessionProcessorClientBuilder::prefetchCount);
        propertyMapper.from(processorProperties.getMaxAutoLockRenewDuration()).to(sessionProcessorClientBuilder::maxAutoLockRenewDuration);
        propertyMapper.from(processorProperties.getAutoComplete()).whenFalse().to(t -> sessionProcessorClientBuilder.disableAutoComplete());
        propertyMapper.from(processorProperties.getMaxConcurrentCalls()).to(sessionProcessorClientBuilder::maxConcurrentCalls);
        propertyMapper.from(processorProperties.getMaxConcurrentSessions()).to(sessionProcessorClientBuilder::maxConcurrentSessions);

        if (StringUtils.hasText(processorProperties.getQueueName())
            && StringUtils.hasText(processorProperties.getTopicName())
            && StringUtils.hasText(processorProperties.getSubscriptionName())) {
            LOGGER.warn("Both queue and topic name configured for a service bus processor, but only the queue "
                + "name will take effective");
        }

        return sessionProcessorClientBuilder;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Documented
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.processor.session-aware", havingValue = "true")
    private @interface ServiceBusSessionEnabled {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Documented
    @ConditionalOnProperty(value = "spring.cloud.azure.servicebus.processor.session-aware", havingValue = "false", matchIfMissing = true)
    private @interface ServiceBusSessionDisabled {
    }

}
