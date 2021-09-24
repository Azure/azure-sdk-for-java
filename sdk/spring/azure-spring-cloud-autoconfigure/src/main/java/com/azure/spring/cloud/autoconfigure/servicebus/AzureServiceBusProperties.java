// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import com.azure.spring.core.properties.AzurePropertiesUtils;
import org.springframework.boot.context.properties.PropertyMapper;

import java.time.Duration;

/**
 *
 */
public class AzureServiceBusProperties extends AzureServiceBusCommonProperties {

    public static final String PREFIX = "spring.cloud.azure.servicebus";

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
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

        Producer properties = new Producer();

        AzurePropertiesUtils.copyAzureCommonProperties(this.producer, properties);
        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(this, properties);

        propertyMapper.from(this.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.getConnectionString()).to(properties::setConnectionString);

        propertyMapper.from(this.producer.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.producer.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.producer.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(this.producer.getTopicName()).to(properties::setTopicName);
        propertyMapper.from(this.producer.getQueueName()).to(properties::setQueueName);

        return properties;
    }

    public Consumer buildConsumerProperties() {
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

        Consumer properties = new Consumer();

        AzurePropertiesUtils.copyAzureCommonProperties(this.consumer, properties);
        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(this, properties);

        propertyMapper.from(this.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.getConnectionString()).to(properties::setConnectionString);

        propertyMapper.from(this.consumer.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.consumer.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.consumer.getConnectionString()).to(properties::setConnectionString);

        propertyMapper.from(this.consumer.getTopicName()).to(properties::setTopicName);
        propertyMapper.from(this.consumer.getQueueName()).to(properties::setQueueName);
        propertyMapper.from(this.consumer.getSessionAware()).to(properties::setSessionAware);
        propertyMapper.from(this.consumer.getAutoComplete()).to(properties::setAutoComplete);
        propertyMapper.from(this.consumer.getPrefetchCount()).to(properties::setPrefetchCount);
        propertyMapper.from(this.consumer.getSubQueue()).to(properties::setSubQueue);
        propertyMapper.from(this.consumer.getReceiveMode()).to(properties::setReceiveMode);
        propertyMapper.from(this.consumer.getSubscriptionName()).to(properties::setSubscriptionName);
        propertyMapper.from(this.consumer.getMaxAutoLockRenewDuration()).to(properties::setMaxAutoLockRenewDuration);

        return properties;
    }

    public Processor buildProcessorProperties() {
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

        Processor properties = new Processor();

        AzurePropertiesUtils.copyAzureCommonProperties(this.processor, properties);
        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(this, properties);

        propertyMapper.from(this.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.getConnectionString()).to(properties::setConnectionString);

        propertyMapper.from(this.processor.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.processor.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.processor.getConnectionString()).to(properties::setConnectionString);

        propertyMapper.from(this.processor.getTopicName()).to(properties::setTopicName);
        propertyMapper.from(this.processor.getQueueName()).to(properties::setQueueName);
        propertyMapper.from(this.processor.getSessionAware()).to(properties::setSessionAware);
        propertyMapper.from(this.processor.getAutoComplete()).to(properties::setAutoComplete);
        propertyMapper.from(this.processor.getPrefetchCount()).to(properties::setPrefetchCount);
        propertyMapper.from(this.processor.getSubQueue()).to(properties::setSubQueue);
        propertyMapper.from(this.processor.getReceiveMode()).to(properties::setReceiveMode);
        propertyMapper.from(this.processor.getSubscriptionName()).to(properties::setSubscriptionName);
        propertyMapper.from(this.processor.getMaxAutoLockRenewDuration()).to(properties::setMaxAutoLockRenewDuration);
        propertyMapper.from(this.processor.getMaxConcurrentCalls()).to(properties::setMaxConcurrentCalls);
        propertyMapper.from(this.processor.getMaxConcurrentSessions()).to(properties::setMaxConcurrentSessions);

        return properties;
    }


    static class Producer extends AzureServiceBusCommonProperties {

        private String queueName;
        private String topicName;

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public String getTopicName() {
            return topicName;
        }

        public void setTopicName(String topicName) {
            this.topicName = topicName;
        }
    }

    static class Consumer extends AzureServiceBusCommonProperties {

        // TODO (xiada): name for session
        private Boolean sessionAware;
        private Boolean autoComplete;
        private Integer prefetchCount;
        private String queueName;
        private SubQueue subQueue;
        private ServiceBusReceiveMode receiveMode = ServiceBusReceiveMode.PEEK_LOCK;
        private String subscriptionName;
        private String topicName;
        private Duration maxAutoLockRenewDuration;

        public Boolean getSessionAware() {
            return sessionAware;
        }

        public void setSessionAware(Boolean sessionAware) {
            this.sessionAware = sessionAware;
        }

        public Boolean getAutoComplete() {
            return autoComplete;
        }

        public void setAutoComplete(Boolean autoComplete) {
            this.autoComplete = autoComplete;
        }

        public Integer getPrefetchCount() {
            return prefetchCount;
        }

        public void setPrefetchCount(Integer prefetchCount) {
            this.prefetchCount = prefetchCount;
        }

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public SubQueue getSubQueue() {
            return subQueue;
        }

        public void setSubQueue(SubQueue subQueue) {
            this.subQueue = subQueue;
        }

        public ServiceBusReceiveMode getReceiveMode() {
            return receiveMode;
        }

        public void setReceiveMode(ServiceBusReceiveMode receiveMode) {
            this.receiveMode = receiveMode;
        }

        public String getSubscriptionName() {
            return subscriptionName;
        }

        public void setSubscriptionName(String subscriptionName) {
            this.subscriptionName = subscriptionName;
        }

        public String getTopicName() {
            return topicName;
        }

        public void setTopicName(String topicName) {
            this.topicName = topicName;
        }

        public Duration getMaxAutoLockRenewDuration() {
            return maxAutoLockRenewDuration;
        }

        public void setMaxAutoLockRenewDuration(Duration maxAutoLockRenewDuration) {
            this.maxAutoLockRenewDuration = maxAutoLockRenewDuration;
        }
    }

    static class Processor extends Consumer {
        private Integer maxConcurrentCalls;
        private Integer maxConcurrentSessions;

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
    }

    // TODO (xiada) we removed these properties, and not mark them as deprecated, should we mention them in the migration docs?
//    public AmqpRetryOptions getRetryOptions() {
//        return retryOptions;
//    }
//
//    public void setRetryOptions(AmqpRetryOptions retryOptions) {
//        this.retryOptions = retryOptions;
//    }
//
//    @DeprecatedConfigurationProperty(reason = "Use ", replacement = "")
//    public AmqpTransportType getTransportType() {
//        return transportType;
//    }
//
//    public void setTransportType(AmqpTransportType transportType) {
//        this.transportType = transportType;
//    }
}
