// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import com.azure.spring.cloud.autoconfigure.properties.AzureAmqpConfigurationProperties;
import com.azure.spring.core.properties.client.AmqpClientProperties;

import java.time.Duration;

/**
 *
 */
public class AzureServiceBusProperties extends AzureAmqpConfigurationProperties {

    public static final String PREFIX = "spring.cloud.azure.servicebus";

    // https://help.boomi.com/bundle/connectors/page/r-atm-Microsoft_Azure_Service_Bus_connection.html
    // https://docs.microsoft.com/en-us/rest/api/servicebus/addressing-and-protocol
    private String domainName = "servicebus.windows.net";

    private String namespace;

    private String connectionString;

    private boolean crossEntityTransactions;

    private AmqpClientProperties client = new AmqpClientProperties();

    private final ServiceBusSender sender = new ServiceBusSender();
    private final ServiceBusReceiver receiver = new ServiceBusReceiver();
    private final ServiceBusProcessor processor = new ServiceBusProcessor();

    public String getFQDN() {
        return this.namespace + "." + this.domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public boolean isCrossEntityTransactions() {
        return crossEntityTransactions;
    }

    public void setCrossEntityTransactions(boolean crossEntityTransactions) {
        this.crossEntityTransactions = crossEntityTransactions;
    }

    @Override
    public AmqpClientProperties getClient() {
        return client;
    }

    public void setClient(AmqpClientProperties client) {
        this.client = client;
    }

    public ServiceBusSender getSender() {
        return sender;
    }

    public ServiceBusReceiver getReceiver() {
        return receiver;
    }

    static class ServiceBusSender {
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

    static class ServiceBusReceiver {
        // TODO (xiada): name for session
        private boolean sessionAware = false;
        private boolean autoComplete = true;
        private Integer prefetchCount;
        private String queueName;
        private SubQueue subQueue;
        private ServiceBusReceiveMode receiveMode = ServiceBusReceiveMode.PEEK_LOCK;
        private String subscriptionName;
        private String topicName;
        private Duration maxAutoLockRenewDuration;

        public boolean isSessionAware() {
            return sessionAware;
        }

        public void setSessionAware(boolean sessionAware) {
            this.sessionAware = sessionAware;
        }

        public boolean isAutoComplete() {
            return autoComplete;
        }

        public void setAutoComplete(boolean autoComplete) {
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

    static class ServiceBusProcessor extends ServiceBusReceiver {
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

    public ServiceBusProcessor getProcessor() {
        return processor;
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
