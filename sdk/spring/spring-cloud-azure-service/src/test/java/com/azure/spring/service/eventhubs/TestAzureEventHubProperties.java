// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs;

import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.spring.core.properties.AzurePropertiesUtils;
import com.azure.spring.service.eventhubs.properties.EventHubConsumerProperties;
import com.azure.spring.service.eventhubs.properties.EventHubProcessorProperties;
import com.azure.spring.service.eventhubs.properties.EventHubProducerProperties;
import com.azure.spring.service.eventhubs.properties.EventHubProperties;
import com.azure.spring.service.storage.blob.TestAzureStorageBlobHttpProperties;
import org.springframework.beans.BeanUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Azure Event Hub related properties.
 */
public class TestAzureEventHubProperties extends TestAzureEventHubCommonProperties implements EventHubProperties {

    private Boolean isSharedConnection;
    private final Producer producer = new Producer();
    private final Consumer consumer = new Consumer();
    private final Processor processor = new Processor();

    public Boolean getSharedConnection() {
        return isSharedConnection;
    }

    public void setSharedConnection(Boolean sharedConnection) {
        isSharedConnection = sharedConnection;
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

    /**
     * Properties of an Event Hub producer.
     */
    public static class Producer extends TestAzureEventHubCommonProperties implements EventHubProducerProperties {

    }

    /**
     * Properties of an Event Hub consumer.
     */
    public static class Consumer extends TestAzureEventHubConsumerProperties implements EventHubConsumerProperties {

    }

    /**
     * Properties of an Event Hub processor.
     */
    public static class Processor extends TestAzureEventHubConsumerProperties implements EventHubProcessorProperties {

        private Boolean trackLastEnqueuedEventProperties;
        private Map<String, EventPosition> initialPartitionEventPosition = new HashMap<>();
        private Duration partitionOwnershipExpirationInterval;
        private final Batch batch = new Batch();
        private final LoadBalancing loadBalancing = new LoadBalancing();
        private final BlobCheckpointStore checkpointStore = new BlobCheckpointStore();

        public Boolean getTrackLastEnqueuedEventProperties() {
            return trackLastEnqueuedEventProperties;
        }

        public void setTrackLastEnqueuedEventProperties(Boolean trackLastEnqueuedEventProperties) {
            this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;
        }

        public Map<String, EventPosition> getInitialPartitionEventPosition() {
            return initialPartitionEventPosition;
        }

        public void setInitialPartitionEventPosition(Map<String, EventPosition> initialPartitionEventPosition) {
            this.initialPartitionEventPosition = initialPartitionEventPosition;
        }

        public Duration getPartitionOwnershipExpirationInterval() {
            return partitionOwnershipExpirationInterval;
        }

        public void setPartitionOwnershipExpirationInterval(Duration partitionOwnershipExpirationInterval) {
            this.partitionOwnershipExpirationInterval = partitionOwnershipExpirationInterval;
        }

        public Batch getBatch() {
            return batch;
        }

        public LoadBalancing getLoadBalancing() {
            return loadBalancing;
        }

        public BlobCheckpointStore getCheckpointStore() {
            return checkpointStore;
        }

        /**
         * Event processor load balancing properties.
         */
        public static class LoadBalancing implements EventHubProcessorProperties.LoadBalancing {
            private Duration updateInterval;
            private LoadBalancingStrategy strategy = LoadBalancingStrategy.BALANCED;

            public Duration getUpdateInterval() {
                return updateInterval;
            }

            public void setUpdateInterval(Duration updateInterval) {
                this.updateInterval = updateInterval;
            }

            public LoadBalancingStrategy getStrategy() {
                return strategy;
            }

            public void setStrategy(LoadBalancingStrategy strategy) {
                this.strategy = strategy;
            }
        }

        /**
         * Event processor batch properties.
         */
        public static class Batch implements EventHubProcessorProperties.Batch {
            private Duration maxWaitTime;
            private Integer maxSize;

            public Duration getMaxWaitTime() {
                return maxWaitTime;
            }

            public void setMaxWaitTime(Duration maxWaitTime) {
                this.maxWaitTime = maxWaitTime;
            }

            public Integer getMaxSize() {
                return maxSize;
            }

            public void setMaxSize(Integer maxSize) {
                this.maxSize = maxSize;
            }
        }

        /**
         * Blob checkpoint store.
         */
        public static class BlobCheckpointStore extends TestAzureStorageBlobHttpProperties {


        }
    }


}
