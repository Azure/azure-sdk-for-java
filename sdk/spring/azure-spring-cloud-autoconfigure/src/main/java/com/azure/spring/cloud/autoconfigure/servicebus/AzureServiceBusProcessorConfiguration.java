// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.core.ApplicationId;
import com.azure.spring.core.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.integration.servicebus.ServiceBusMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_FACTORY_BEAN_NAME;

/**
 * Configuration for a {@link ServiceBusProcessorClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(ServiceBusMessageProcessor.class)
@ConditionalOnExpression(
    "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.processor.queue-name:}') or "
        + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.processor.topic-name:}')"
)
@Import({
    AzureServiceBusProcessorConfiguration.SessionProcessorClientConfiguration.class,
    AzureServiceBusProcessorConfiguration.NoneSessionProcessorClientConfiguration.class
})
class AzureServiceBusProcessorConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusProcessorConfiguration.class);

    private final PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
    private final AzureServiceBusProperties.Processor processorProperties;

    AzureServiceBusProcessorConfiguration(AzureServiceBusProperties serviceBusProperties) {
        this.processorProperties = serviceBusProperties.buildProcessorProperties();
    }

    @Bean(SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnExpression(
        "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.processor.connection-string:}') or "
            + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.processor.producer.namespace:}')"
    )
    public ServiceBusClientBuilderFactory serviceBusClientBuilderFactoryForProcessor() {

        final ServiceBusClientBuilderFactory builderFactory = new ServiceBusClientBuilderFactory(this.processorProperties);
        builderFactory.setConnectionStringProvider(new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS,
                                                                                        this.processorProperties.getConnectionString()));
        builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_SERVICE_BUS);

        return builderFactory;
    }

    @Bean(SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME)
    @ConditionalOnBean(name = SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME)
    public ServiceBusClientBuilder serviceBusClientBuilderForProcessor(
        @Qualifier(SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_FACTORY_BEAN_NAME) ServiceBusClientBuilderFactory clientBuilderFactory) {

        return clientBuilderFactory.build();
    }

    @ConditionalOnProperty(
        prefix = "spring.cloud.azure.servicebus.processor.session-aware", havingValue = "false", matchIfMissing = true
    )
    @ConditionalOnBean(ServiceBusMessageProcessor.class)
    static class NoneSessionProcessorClientConfiguration {

        @Bean
        @ConditionalOnMissingBean(
            name = SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME,
            value = ServiceBusClientBuilder.ServiceBusProcessorClientBuilder.class)
        public ServiceBusClientBuilder.ServiceBusProcessorClientBuilder serviceBusProcessorClientBuilder(
            AzureServiceBusProperties serviceBusProperties,
            ServiceBusClientBuilder serviceBusClientBuilder) {
            return buildProcessorClientBuilder(serviceBusProperties, serviceBusClientBuilder);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(name = SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME)
        public ServiceBusClientBuilder.ServiceBusProcessorClientBuilder serviceBusProcessorClientBuilderForProcessor(
            AzureServiceBusProperties serviceBusProperties,
            @Qualifier(SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME) ServiceBusClientBuilder serviceBusClientBuilder) {
            return buildProcessorClientBuilder(serviceBusProperties, serviceBusClientBuilder);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder.class)
        public ServiceBusProcessorClient serviceBusProcessorClient(
            ServiceBusMessageProcessor serviceBusMessageProcessor,
            ServiceBusClientBuilder.ServiceBusProcessorClientBuilder processorClientBuilder) {

            processorClientBuilder.processError(serviceBusMessageProcessor.processError());
            processorClientBuilder.processMessage(serviceBusMessageProcessor.processMessage());
            return processorClientBuilder.buildProcessorClient();
        }

        private ServiceBusClientBuilder.ServiceBusProcessorClientBuilder buildProcessorClientBuilder(
            AzureServiceBusProperties serviceBusProperties,
            ServiceBusClientBuilder serviceBusClientBuilder) {
            final AzureServiceBusProperties.Processor processorProperties = serviceBusProperties.getProcessor();
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

    }

    @ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, name = "processor.session-aware")
    @ConditionalOnBean(ServiceBusMessageProcessor.class)
    static class SessionProcessorClientConfiguration {

        @Bean
        @ConditionalOnMissingBean(
            name = SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME,
            value = ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder.class)
        public ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder serviceBusSessionProcessorClientBuilder(
            AzureServiceBusProperties serviceBusProperties,
            ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildSessionProcessorClientBuilder(serviceBusProperties, serviceBusClientBuilder);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(name = SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME)
        public ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder serviceBusSessionProcessorClientBuilderForProcessor(
            AzureServiceBusProperties serviceBusProperties,
            @Qualifier(SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME) ServiceBusClientBuilder serviceBusClientBuilder) {

            return buildSessionProcessorClientBuilder(serviceBusProperties, serviceBusClientBuilder);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder.class)
        public ServiceBusProcessorClient serviceBusSessionProcessorClient(
            ServiceBusMessageProcessor serviceBusMessageProcessor,
            ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder sessionProcessorClientBuilder) {

            sessionProcessorClientBuilder.processError(serviceBusMessageProcessor.processError());
            sessionProcessorClientBuilder.processMessage(serviceBusMessageProcessor.processMessage());

            return sessionProcessorClientBuilder.buildProcessorClient();
        }

        private ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder buildSessionProcessorClientBuilder(
            AzureServiceBusProperties serviceBusProperties,
            ServiceBusClientBuilder serviceBusClientBuilder) {
            final AzureServiceBusProperties.Processor processorProperties = serviceBusProperties.getProcessor();
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
    }

}
