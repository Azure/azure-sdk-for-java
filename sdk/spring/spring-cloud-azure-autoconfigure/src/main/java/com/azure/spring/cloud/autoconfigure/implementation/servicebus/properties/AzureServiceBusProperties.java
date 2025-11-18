// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.service.implementation.core.PropertiesValidator;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusNamespaceProperties;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusProcessorClientProperties;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusReceiverClientProperties;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusSenderClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.PropertyMapper;

import java.time.Duration;
import java.util.Objects;
import java.util.stream.Stream;

/**
 *
 */
public class AzureServiceBusProperties extends AzureServiceBusCommonProperties
    implements ServiceBusNamespaceProperties, InitializingBean {

    public static final String PREFIX = "spring.cloud.azure.servicebus";
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusProperties.class);
    /**
     * Whether to enable cross entity transaction on the connection to Service bus.
     */
    private Boolean crossEntityTransactions;
    private final Producer producer = new Producer();
    private final Consumer consumer = new Consumer();
    private final Processor processor = new Processor();

    public Boolean getCrossEntityTransactions() {
        return crossEntityTransactions;
    }

    public void setCrossEntityTransactions(Boolean crossEntityTransactions) {
        this.crossEntityTransactions = crossEntityTransactions;
    }

    public Producer getProducer() {
        return producer;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public Processor getProcessor() {
        return processor;
    }

    public Producer buildProducerProperties() {
        PropertyMapper propertyMapper = PropertyMapper.get();

        Producer properties = new Producer();

        AzurePropertiesUtils.mergeAzureCommonProperties(this, this.producer, properties);

        propertyMapper.from(this.getDomainName()).always().to(properties::setDomainName);
        propertyMapper.from(this.getCustomEndpointAddress()).always().to(properties::setCustomEndpointAddress);
        propertyMapper.from(this.getNamespace()).always().to(properties::setNamespace);
        propertyMapper.from(this.getConnectionString()).always().to(properties::setConnectionString);
        propertyMapper.from(this.getEntityName()).always().to(properties::setEntityName);
        propertyMapper.from(this.getEntityType()).always().to(properties::setEntityType);

        propertyMapper.from(this.producer.getDomainName()).always().to(properties::setDomainName);
        propertyMapper.from(this.producer.getCustomEndpointAddress()).always().to(properties::setCustomEndpointAddress);
        propertyMapper.from(this.producer.getNamespace()).always().to(properties::setNamespace);
        propertyMapper.from(this.producer.getConnectionString()).always().to(properties::setConnectionString);
        propertyMapper.from(this.producer.getEntityType()).always().to(properties::setEntityType);
        propertyMapper.from(this.producer.getEntityName()).always().to(properties::setEntityName);

        return properties;
    }

    public Consumer buildConsumerProperties() {
        PropertyMapper propertyMapper = PropertyMapper.get();

        Consumer properties = new Consumer();

        AzurePropertiesUtils.mergeAzureCommonProperties(this, this.consumer, properties);

        propertyMapper.from(this.getDomainName()).always().to(properties::setDomainName);
        propertyMapper.from(this.getCustomEndpointAddress()).always().to(properties::setCustomEndpointAddress);
        propertyMapper.from(this.getNamespace()).always().to(properties::setNamespace);
        propertyMapper.from(this.getConnectionString()).always().to(properties::setConnectionString);
        propertyMapper.from(this.getEntityName()).always().to(properties::setEntityName);
        propertyMapper.from(this.getEntityType()).always().to(properties::setEntityType);

        propertyMapper.from(this.consumer.getDomainName()).always().to(properties::setDomainName);
        propertyMapper.from(this.consumer.getCustomEndpointAddress()).always().to(properties::setCustomEndpointAddress);
        propertyMapper.from(this.consumer.getNamespace()).always().to(properties::setNamespace);
        propertyMapper.from(this.consumer.getConnectionString()).always().to(properties::setConnectionString);
        propertyMapper.from(this.consumer.getEntityType()).always().to(properties::setEntityType);
        propertyMapper.from(this.consumer.getEntityName()).always().to(properties::setEntityName);

        propertyMapper.from(this.consumer.getSessionEnabled()).always().to(properties::setSessionEnabled);
        propertyMapper.from(this.consumer.getAutoComplete()).always().to(properties::setAutoComplete);
        propertyMapper.from(this.consumer.getPrefetchCount()).always().to(properties::setPrefetchCount);
        propertyMapper.from(this.consumer.getSubQueue()).always().to(properties::setSubQueue);
        propertyMapper.from(this.consumer.getReceiveMode()).always().to(properties::setReceiveMode);
        propertyMapper.from(this.consumer.getSubscriptionName()).always().to(properties::setSubscriptionName);
        propertyMapper.from(this.consumer.getMaxAutoLockRenewDuration()).always().to(properties::setMaxAutoLockRenewDuration);

        return properties;
    }

    public Processor buildProcessorProperties() {
        PropertyMapper propertyMapper = PropertyMapper.get();

        Processor properties = new Processor();

        AzurePropertiesUtils.mergeAzureCommonProperties(this, this.processor, properties);

        propertyMapper.from(this.getDomainName()).always().to(properties::setDomainName);
        propertyMapper.from(this.getCustomEndpointAddress()).always().to(properties::setCustomEndpointAddress);
        propertyMapper.from(this.getNamespace()).always().to(properties::setNamespace);
        propertyMapper.from(this.getConnectionString()).always().to(properties::setConnectionString);
        propertyMapper.from(this.getEntityName()).always().to(properties::setEntityName);
        propertyMapper.from(this.getEntityType()).always().to(properties::setEntityType);

        propertyMapper.from(this.processor.getDomainName()).always().to(properties::setDomainName);
        propertyMapper.from(this.processor.getCustomEndpointAddress()).always().to(properties::setCustomEndpointAddress);
        propertyMapper.from(this.processor.getNamespace()).always().to(properties::setNamespace);
        propertyMapper.from(this.processor.getConnectionString()).always().to(properties::setConnectionString);
        propertyMapper.from(this.processor.getEntityType()).always().to(properties::setEntityType);
        propertyMapper.from(this.processor.getEntityName()).always().to(properties::setEntityName);

        propertyMapper.from(this.processor.getSessionEnabled()).always().to(properties::setSessionEnabled);
        propertyMapper.from(this.processor.getAutoComplete()).always().to(properties::setAutoComplete);
        propertyMapper.from(this.processor.getPrefetchCount()).always().to(properties::setPrefetchCount);
        propertyMapper.from(this.processor.getSubQueue()).always().to(properties::setSubQueue);
        propertyMapper.from(this.processor.getReceiveMode()).always().to(properties::setReceiveMode);
        propertyMapper.from(this.processor.getSubscriptionName()).always().to(properties::setSubscriptionName);
        propertyMapper.from(this.processor.getMaxAutoLockRenewDuration()).always().to(properties::setMaxAutoLockRenewDuration);
        propertyMapper.from(this.processor.getMaxConcurrentCalls()).always().to(properties::setMaxConcurrentCalls);
        propertyMapper.from(this.processor.getMaxConcurrentSessions()).always().to(properties::setMaxConcurrentSessions);

        return properties;
    }

    /**
     * Properties of a Service Bus producer.
     */
    public static class Producer extends AzureServiceBusCommonProperties implements ServiceBusSenderClientProperties {

    }

    /**
     * Properties of a Service Bus consumer.
     */
    public static class Consumer extends AzureServiceBusCommonProperties implements ServiceBusReceiverClientProperties {
        /**
         * Whether to enable session for the consumer.
         */
        private Boolean sessionEnabled;
        /**
         * Whether to enable auto-complete.
         */
        private Boolean autoComplete;
        /**
         * Prefetch count of the consumer.
         */
        private Integer prefetchCount;
        /**
         * Type of the SubQueue to connect to.
         */
        private SubQueue subQueue;
        /**
         * Mode for receiving messages.
         */
        private ServiceBusReceiveMode receiveMode;
        /**
         * Name for a topic subscription.
         */
        private String subscriptionName;
        /**
         * Amount of time to continue auto-renewing the lock.
         */
        private Duration maxAutoLockRenewDuration;

        @Override
        public Boolean getSessionEnabled() {
            return sessionEnabled;
        }

        public void setSessionEnabled(Boolean sessionEnabled) {
            this.sessionEnabled = sessionEnabled;
        }

        @Override
        public Boolean getAutoComplete() {
            return autoComplete;
        }

        public void setAutoComplete(Boolean autoComplete) {
            this.autoComplete = autoComplete;
        }

        @Override
        public Integer getPrefetchCount() {
            return prefetchCount;
        }

        public void setPrefetchCount(Integer prefetchCount) {
            this.prefetchCount = prefetchCount;
        }

        @Override
        public SubQueue getSubQueue() {
            return subQueue;
        }

        public void setSubQueue(SubQueue subQueue) {
            this.subQueue = subQueue;
        }

        @Override
        public ServiceBusReceiveMode getReceiveMode() {
            return receiveMode;
        }

        public void setReceiveMode(ServiceBusReceiveMode receiveMode) {
            this.receiveMode = receiveMode;
        }

        @Override
        public String getSubscriptionName() {
            return subscriptionName;
        }

        public void setSubscriptionName(String subscriptionName) {
            this.subscriptionName = subscriptionName;
        }

        @Override
        public Duration getMaxAutoLockRenewDuration() {
            return maxAutoLockRenewDuration;
        }

        public void setMaxAutoLockRenewDuration(Duration maxAutoLockRenewDuration) {
            this.maxAutoLockRenewDuration = maxAutoLockRenewDuration;
        }
    }

    /**
     * Properties of a Service Bus processor.
     */
    public static class Processor extends Consumer implements ServiceBusProcessorClientProperties {
        /**
         * Max concurrent messages to process. When session enabled, it applies to each session.
         */
        private Integer maxConcurrentCalls;
        /**
         * Maximum number of concurrent sessions to process at any given time.
         */
        private Integer maxConcurrentSessions;

        /**
         * Whether to automatically start the processor after initialization.
         */
        private boolean autoStartup = true;

        /**
         * Sets the maximum amount of time to wait for a message to be received for the currently active session.
         */
        private Duration sessionIdleTimeout;

        public Integer getMaxConcurrentCalls() {
            return maxConcurrentCalls;
        }

        public void setMaxConcurrentCalls(Integer maxConcurrentCalls) {
            this.maxConcurrentCalls = maxConcurrentCalls;
        }

        public Integer getMaxConcurrentSessions() {
            return maxConcurrentSessions;
        }

        public void setMaxConcurrentSessions(Integer maxConcurrentSessions) {
            this.maxConcurrentSessions = maxConcurrentSessions;
        }

        public boolean isAutoStartup() {
            return autoStartup;
        }

        public void setAutoStartup(boolean autoStartup) {
            this.autoStartup = autoStartup;
        }

        public Duration getSessionIdleTimeout() {
            return sessionIdleTimeout;
        }

        public void setSessionIdleTimeout(Duration sessionIdleTimeout) {
            this.sessionIdleTimeout = sessionIdleTimeout;
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            validateNamespaceProperties();
        } catch (IllegalArgumentException exception) {
            LOGGER.warn(exception.getMessage());
        }
    }

    private void validateNamespaceProperties() {
        Stream.of(getNamespace(), producer.getNamespace(), consumer.getNamespace(), processor.getNamespace())
              .filter(Objects::nonNull)
              .forEach(PropertiesValidator::validateNamespace);
    }
}
