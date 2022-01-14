// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.eventhubs;

import com.azure.spring.service.eventhubs.properties.EventBatchProperties;
import com.azure.spring.service.implementation.eventhubs.properties.EventHubConsumerProperties;
import com.azure.spring.service.implementation.eventhubs.properties.EventHubProducerProperties;
import com.azure.spring.service.implementation.eventhubs.properties.EventHubsNamespaceProperties;
import com.azure.spring.service.implementation.eventhubs.properties.EventProcessorClientProperties;
import com.azure.spring.service.eventhubs.properties.LoadBalancingProperties;
import com.azure.spring.service.implementation.storage.blob.TestAzureStorageBlobProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Azure Event Hubs related properties.
 */
class TestAzureEventHubsProperties extends TestAzureEventHubsCommonProperties implements EventHubsNamespaceProperties {

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
    public static class Producer extends TestAzureEventHubsCommonProperties implements EventHubProducerProperties {

    }

    /**
     * Properties of an Event Hub consumer.
     */
    public static class Consumer extends TestAzureEventHubsConsumerProperties implements EventHubConsumerProperties {

    }

    /**
     * Properties of an Event Hub processor.
     */
    public static class Processor extends TestAzureEventHubsConsumerProperties implements EventProcessorClientProperties {

        private Boolean trackLastEnqueuedEventProperties;
        private Map<String, StartPosition> initialPartitionEventPosition = new HashMap<>();
        private Duration partitionOwnershipExpirationInterval;
        private final EventBatchProperties batch = new EventBatchProperties();
        private final LoadBalancingProperties loadBalancing = new LoadBalancingProperties();
        private final BlobCheckpointStore checkpointStore = new BlobCheckpointStore();

        public Boolean getTrackLastEnqueuedEventProperties() {
            return trackLastEnqueuedEventProperties;
        }

        public void setTrackLastEnqueuedEventProperties(Boolean trackLastEnqueuedEventProperties) {
            this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;
        }

        public Map<String, StartPosition> getInitialPartitionEventPosition() {
            return initialPartitionEventPosition;
        }

        public void setInitialPartitionEventPosition(Map<String, StartPosition> initialPartitionEventPosition) {
            this.initialPartitionEventPosition = initialPartitionEventPosition;
        }

        public Duration getPartitionOwnershipExpirationInterval() {
            return partitionOwnershipExpirationInterval;
        }

        public void setPartitionOwnershipExpirationInterval(Duration partitionOwnershipExpirationInterval) {
            this.partitionOwnershipExpirationInterval = partitionOwnershipExpirationInterval;
        }

        public EventBatchProperties getBatch() {
            return batch;
        }

        public LoadBalancingProperties getLoadBalancing() {
            return loadBalancing;
        }

        public BlobCheckpointStore getCheckpointStore() {
            return checkpointStore;
        }

        /**
         * Blob checkpoint store.
         */
        public static class BlobCheckpointStore extends TestAzureStorageBlobProperties {


        }
    }


}
