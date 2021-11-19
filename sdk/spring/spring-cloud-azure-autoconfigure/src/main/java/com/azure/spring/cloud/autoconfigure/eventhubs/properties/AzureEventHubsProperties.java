// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs.properties;

import com.azure.spring.cloud.autoconfigure.storage.blob.properties.AzureStorageBlobProperties;
import com.azure.spring.core.properties.util.AzurePropertiesUtils;
import com.azure.spring.service.eventhubs.properties.EventHubsConsumerDescriptor;
import com.azure.spring.service.eventhubs.properties.EventHubsNamespaceDescriptor;
import com.azure.spring.service.eventhubs.properties.EventHubsProcessorDescriptor;
import com.azure.spring.service.eventhubs.properties.EventHubsProducerDescriptor;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.PropertyMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Azure Event Hubs related properties.
 */
public class AzureEventHubsProperties extends AzureEventHubsCommonProperties implements EventHubsNamespaceDescriptor {

    public static final String PREFIX = "spring.cloud.azure.eventhubs";

    private Boolean isSharedConnection;
    private final Producer producer = new Producer();
    private final Consumer consumer = new Consumer();
    private final Processor processor = new Processor();

    public Producer buildProducerProperties() {
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

        Producer properties = new Producer();
        AzurePropertiesUtils.mergeAzureCommonProperties(this, this.producer, properties);

        propertyMapper.from(this.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.getEventHubName()).to(properties::setEventHubName);
        propertyMapper.from(this.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(this.getCustomEndpointAddress()).to(properties::setCustomEndpointAddress);

        propertyMapper.from(this.producer.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.producer.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.producer.getEventHubName()).to(properties::setEventHubName);
        propertyMapper.from(this.producer.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(this.producer.getCustomEndpointAddress()).to(properties::setCustomEndpointAddress);

        return properties;
    }

    public Consumer buildConsumerProperties() {
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

        Consumer properties = new Consumer();
        AzurePropertiesUtils.mergeAzureCommonProperties(this, this.consumer, properties);

        propertyMapper.from(this.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.getEventHubName()).to(properties::setEventHubName);
        propertyMapper.from(this.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(this.getCustomEndpointAddress()).to(properties::setCustomEndpointAddress);

        propertyMapper.from(this.consumer.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.consumer.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.consumer.getEventHubName()).to(properties::setEventHubName);
        propertyMapper.from(this.consumer.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(this.consumer.getCustomEndpointAddress()).to(properties::setCustomEndpointAddress);
        propertyMapper.from(this.consumer.getPrefetchCount()).to(properties::setPrefetchCount);
        propertyMapper.from(this.consumer.getConsumerGroup()).to(properties::setConsumerGroup);

        return properties;
    }

    public Processor buildProcessorProperties() {
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

        Processor properties = new Processor();
        AzurePropertiesUtils.mergeAzureCommonProperties(this, this.processor, properties);

        propertyMapper.from(this.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.getEventHubName()).to(properties::setEventHubName);
        propertyMapper.from(this.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(this.getCustomEndpointAddress()).to(properties::setCustomEndpointAddress);

        propertyMapper.from(this.processor.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.processor.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.processor.getEventHubName()).to(properties::setEventHubName);
        propertyMapper.from(this.processor.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(this.processor.getCustomEndpointAddress()).to(properties::setCustomEndpointAddress);
        propertyMapper.from(this.processor.getPrefetchCount()).to(properties::setPrefetchCount);
        propertyMapper.from(this.processor.getConsumerGroup()).to(properties::setConsumerGroup);

        propertyMapper.from(this.processor.trackLastEnqueuedEventProperties).to(properties::setTrackLastEnqueuedEventProperties);
        propertyMapper.from(this.processor.initialPartitionEventPosition).to(properties::setInitialPartitionEventPosition);
        propertyMapper.from(this.processor.partitionOwnershipExpirationInterval).to(properties::setPartitionOwnershipExpirationInterval);
        propertyMapper.from(this.processor.batch.getMaxSize()).to(properties.batch::setMaxSize);
        propertyMapper.from(this.processor.batch.getMaxWaitTime()).to(properties.batch::setMaxWaitTime);
        propertyMapper.from(this.processor.loadBalancing.getStrategy()).to(properties.loadBalancing::setStrategy);
        propertyMapper.from(this.processor.loadBalancing.getUpdateInterval()).to(properties.loadBalancing::setUpdateInterval);

        AzurePropertiesUtils.copyAzureCommonProperties(this.processor.checkpointStore, properties.checkpointStore);
        BeanUtils.copyProperties(this.processor.checkpointStore, properties.checkpointStore);

        return properties;
    }

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
    public static class Producer extends AzureEventHubsCommonProperties implements EventHubsProducerDescriptor {

    }

    /**
     * Properties of an Event Hub consumer.
     */
    public static class Consumer extends AzureEventHubsCommonProperties implements EventHubsConsumerDescriptor {

        /**
         * Name of the consumer group this consumer is associated with.
         */
        protected String consumerGroup;

        /**
         * The number of events the Event Hub consumer will actively receive and queue locally without regard to
         * whether a receiving operation is currently active.
         *
         */
        protected Integer prefetchCount;

        public String getConsumerGroup() {
            return consumerGroup;
        }

        public void setConsumerGroup(String consumerGroup) {
            this.consumerGroup = consumerGroup;
        }

        public Integer getPrefetchCount() {
            return prefetchCount;
        }

        public void setPrefetchCount(Integer prefetchCount) {
            this.prefetchCount = prefetchCount;
        }
    }

    /**
     * Properties of an Event Hub processor.
     */
    public static class Processor extends Consumer implements EventHubsProcessorDescriptor {

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
         * Event processor load balancing properties.
         */
        public static class LoadBalancing extends EventHubsProcessorDescriptor.LoadBalancing {

        }

        /**
         * Event processor batch properties.
         */
        public static class Batch extends EventHubsProcessorDescriptor.Batch {

        }

        /**
         * Blob checkpoint store.
         */
        public static class BlobCheckpointStore extends AzureStorageBlobProperties  {

            private Boolean createContainerIfNotExists;


            public Boolean getCreateContainerIfNotExists() {
                return createContainerIfNotExists;
            }

            public void setCreateContainerIfNotExists(Boolean createContainerIfNotExists) {
                this.createContainerIfNotExists = createContainerIfNotExists;
            }
        }
    }


}
