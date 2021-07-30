// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.integration.eventhub;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.implementation.ClientConstants;

import java.time.Duration;

/**
 * Event hub client related config.
 */
public class EventHubClientConfig {

    private final int prefetchCount;
    private final boolean shareConnection;
    private final String customEndpointAddress;
    private final AmqpRetryOptions retryOptions;
    private final AmqpTransportType transport;
    private final LoadBalancingStrategy loadBalancingStrategy;
    private final Duration loadBalancingUpdateInterval;
    private final Duration partitionOwnershipExpirationInterval;
    private final boolean trackLastEnqueuedEventProperties;

    public EventHubClientConfig(int prefetchCount, boolean shareConnection, String customEndpointAddress,
                                AmqpRetryOptions retryOptions, AmqpTransportType transport,
                                LoadBalancingStrategy loadBalancingStrategy,
                                Duration loadBalancingUpdateInterval, Duration partitionOwnershipExpirationInterval,
                                boolean trackLastEnqueuedEventProperties) {
        this.prefetchCount = prefetchCount;
        this.shareConnection = shareConnection;
        this.customEndpointAddress = customEndpointAddress;
        this.retryOptions = retryOptions;
        this.transport = transport;
        this.loadBalancingStrategy = loadBalancingStrategy;
        this.loadBalancingUpdateInterval = loadBalancingUpdateInterval;
        this.partitionOwnershipExpirationInterval = partitionOwnershipExpirationInterval;
        this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;
    }

    public static EventHubClientConfigBuilder eventHubClientConifgBuilder() {
        return new EventHubClientConfigBuilder();
    }

    public int getPrefetchCount() {
        return prefetchCount;
    }

    public boolean isShareConnection() {
        return shareConnection;
    }

    public String getCustomEndpointAddress() {
        return customEndpointAddress;
    }

    public AmqpRetryOptions getRetryOptions() {
        return retryOptions;
    }

    public AmqpTransportType getTransport() {
        return transport;
    }

    public LoadBalancingStrategy getLoadBalancingStrategy() {
        return loadBalancingStrategy;
    }

    public Duration getLoadBalancingUpdateInterval() {
        return loadBalancingUpdateInterval;
    }

    public Duration getPartitionOwnershipExpirationInterval() {
        return partitionOwnershipExpirationInterval;
    }

    public boolean isTrackLastEnqueuedEventProperties() {
        return trackLastEnqueuedEventProperties;
    }

    /**
     * Builder class for {@link EventHubClientConfig}.
     */
    public static class EventHubClientConfigBuilder {
        private int prefetchCount = 1;
        private boolean shareConnection = false;
        private String customEndpointAddress;
        private AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(ClientConstants.OPERATION_TIMEOUT);
        private AmqpTransportType transport = AmqpTransportType.AMQP;
        private LoadBalancingStrategy loadBalancingStrategy = LoadBalancingStrategy.BALANCED;
        private Duration loadBalancingUpdateInterval = Duration.ofSeconds(10);
        private Duration partitionOwnershipExpirationInterval = loadBalancingUpdateInterval.multipliedBy(6);
        private boolean trackLastEnqueuedEventProperties = false;

        public EventHubClientConfig build() {
            return new EventHubClientConfig(prefetchCount, shareConnection, customEndpointAddress, retryOptions,
                transport, loadBalancingStrategy, loadBalancingUpdateInterval,
                partitionOwnershipExpirationInterval, trackLastEnqueuedEventProperties);
        }

        public void setPrefetchCount(int prefetchCount) {
            this.prefetchCount = prefetchCount;
        }

        public void setShareConnection(boolean shareConnection) {
            this.shareConnection = shareConnection;
        }

        public void setCustomEndpointAddress(String customEndpointAddress) {
            this.customEndpointAddress = customEndpointAddress;
        }

        public void setRetryOptions(AmqpRetryOptions retryOptions) {
            this.retryOptions = retryOptions;
        }

        public void setTransport(AmqpTransportType transport) {
            this.transport = transport;
        }

        public void setLoadBalancingStrategy(LoadBalancingStrategy loadBalancingStrategy) {
            this.loadBalancingStrategy = loadBalancingStrategy;
        }

        public void setLoadBalancingUpdateInterval(Duration loadBalancingUpdateInterval) {
            this.loadBalancingUpdateInterval = loadBalancingUpdateInterval;
        }

        public void setPartitionOwnershipExpirationInterval(Duration partitionOwnershipExpirationInterval) {
            this.partitionOwnershipExpirationInterval = partitionOwnershipExpirationInterval;
        }

        public void setTrackLastEnqueuedEventProperties(boolean trackLastEnqueuedEventProperties) {
            this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;
        }
    }

}
