// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs;

import com.azure.spring.service.eventhubs.properties.EventHubsConsumerDescriptor;
import com.azure.spring.service.eventhubs.properties.EventHubsNamespaceDescriptor;
import com.azure.spring.service.eventhubs.properties.EventHubsProcessorDescriptor;
import com.azure.spring.service.eventhubs.properties.EventHubsProducerDescriptor;
import com.azure.spring.service.storage.blob.TestAzureStorageBlobProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Azure Event Hub related properties.
 */
public class TestAzureEventHubsProperties extends TestAzureEventHubCommonProperties implements EventHubsNamespaceDescriptor {

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
    public static class Producer extends TestAzureEventHubCommonProperties implements EventHubsProducerDescriptor {

    }

    /**
     * Properties of an Event Hub consumer.
     */
    public static class Consumer extends TestAzureEventHubConsumerProperties implements EventHubsConsumerDescriptor {

    }

    /**
     * Properties of an Event Hub processor.
     */
    public static class Processor extends TestAzureEventHubConsumerProperties implements EventHubsProcessorDescriptor {

        private Boolean trackLastEnqueuedEventProperties;
        private Map<String, StartPosition> initialPartitionEventPosition = new HashMap<>();
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
         * Blob checkpoint store.
         */
        public static class BlobCheckpointStore extends TestAzureStorageBlobProperties {


        }
    }


}
