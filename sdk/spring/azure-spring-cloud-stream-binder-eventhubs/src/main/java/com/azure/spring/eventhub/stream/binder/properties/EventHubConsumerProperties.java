// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder.properties;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.StartPosition;
import org.springframework.util.ObjectUtils;

import java.time.Duration;

/**
 * @author Warren Zhu
 */
public class EventHubConsumerProperties {
    /**
     * Whether the consumer receives messages from the beginning or end of event hub. If {@link StartPosition#EARLIEST},
     * from beginning. If {@link StartPosition#LATEST}, from end.
     * <p>
     * Default: {@link StartPosition#LATEST}
     */
    private StartPosition startPosition = StartPosition.LATEST;

    /**
     * Checkpoint mode used when consumer decide how to checkpoint message
     * <p>
     * Default: {@link CheckpointMode#BATCH}
     */
    private CheckpointMode checkpointMode = CheckpointMode.BATCH;

    /**
     * Effectively only when {@link CheckpointMode#PARTITION_COUNT}. Decides the amount of message for each partition to
     * do one checkpoint
     *
     * <p>
     * Default : 10
     */
    private int checkpointCount = 10;

    /**
     * Effectively only when {@link CheckpointMode#TIME}. Decides the time interval to do one checkpoint
     *
     * <p>
     * Default : 5s
     */
    private Duration checkpointInterval = Duration.ofSeconds(5);

    private int prefetchCount = 1;

    private boolean shareConnection = false;

    private String customEndpointAddress;

    private AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(ClientConstants.OPERATION_TIMEOUT);

    private AmqpTransportType transport = AmqpTransportType.AMQP;

    private LoadBalancingStrategy loadBalancingStrategy = LoadBalancingStrategy.BALANCED;

    private Duration loadBalancingUpdateInterval = Duration.ofSeconds(10);

    private Duration partitionOwnershipExpirationInterval;

    private boolean trackLastEnqueuedEventProperties = false;

    public StartPosition getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(StartPosition startPosition) {
        this.startPosition = startPosition;
    }

    public CheckpointMode getCheckpointMode() {
        return checkpointMode;
    }

    public void setCheckpointMode(CheckpointMode checkpointMode) {
        this.checkpointMode = checkpointMode;
    }

    public int getCheckpointCount() {
        return checkpointCount;
    }

    public void setCheckpointCount(int checkpointCount) {
        this.checkpointCount = checkpointCount;
    }

    public Duration getCheckpointInterval() {
        return checkpointInterval;
    }

    public void setCheckpointInterval(Duration checkpointInterval) {
        this.checkpointInterval = checkpointInterval;
    }

    public int getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    public boolean isShareConnection() {
        return shareConnection;
    }

    public void setShareConnection(boolean shareConnection) {
        this.shareConnection = shareConnection;
    }

    public String getCustomEndpointAddress() {
        return customEndpointAddress;
    }

    public void setCustomEndpointAddress(String customEndpointAddress) {
        this.customEndpointAddress = customEndpointAddress;
    }

    public AmqpRetryOptions getRetryOptions() {
        return retryOptions;
    }

    public void setRetryOptions(AmqpRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
    }

    public AmqpTransportType getTransport() {
        return transport;
    }

    public void setTransport(AmqpTransportType transport) {
        this.transport = transport;
    }

    public LoadBalancingStrategy getLoadBalancingStrategy() {
        return loadBalancingStrategy;
    }

    public void setLoadBalancingStrategy(LoadBalancingStrategy loadBalancingStrategy) {
        this.loadBalancingStrategy = loadBalancingStrategy;
    }

    public Duration getLoadBalancingUpdateInterval() {
        return loadBalancingUpdateInterval;
    }

    public void setLoadBalancingUpdateInterval(Duration loadBalancingUpdateInterval) {
        this.loadBalancingUpdateInterval = loadBalancingUpdateInterval;
    }

    public Duration getPartitionOwnershipExpirationInterval() {
        if (ObjectUtils.isEmpty(partitionOwnershipExpirationInterval)) {
            this.partitionOwnershipExpirationInterval = loadBalancingUpdateInterval.multipliedBy(6);
        }
        return partitionOwnershipExpirationInterval;
    }

    public void setPartitionOwnershipExpirationInterval(int partitionOwnershipExpirationInterval) {
        this.partitionOwnershipExpirationInterval =
            this.loadBalancingUpdateInterval.multipliedBy(partitionOwnershipExpirationInterval);
    }

    public boolean isTrackLastEnqueuedEventProperties() {
        return trackLastEnqueuedEventProperties;
    }

    public void setTrackLastEnqueuedEventProperties(boolean trackLastEnqueuedEventProperties) {
        this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;
    }
}
