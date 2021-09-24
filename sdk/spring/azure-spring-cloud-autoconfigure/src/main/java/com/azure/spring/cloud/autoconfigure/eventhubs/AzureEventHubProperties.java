// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.spring.cloud.autoconfigure.storage.blob.AzureStorageBlobProperties;
import com.azure.spring.core.properties.AzurePropertiesUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.PropertyMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Azure Event Hub related properties.
 */
public class AzureEventHubProperties extends AzureEventHubConsumerProperties {

    public static final String PREFIX = "spring.cloud.azure.eventhubs";

    private Boolean isSharedConnection;
    private final Producer producer = new Producer();
    private final Consumer consumer = new Consumer();
    private final Processor processor = new Processor();

    public Producer buildProducerProperties() {
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

        Producer properties = new Producer();
        AzurePropertiesUtils.copyAzureCommonProperties(this.producer, properties);
        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(this, properties);

        propertyMapper.from(this.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.getEventHubName()).to(properties::setEventHubName);
        propertyMapper.from(this.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(this.getCustomEndpointAddress()).to(properties::setCustomEndpointAddress);
        propertyMapper.from(this.getPrefetchCount()).to(properties::setPrefetchCount);

        propertyMapper.from(this.producer.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.producer.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.producer.getEventHubName()).to(properties::setEventHubName);
        propertyMapper.from(this.producer.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(this.producer.getCustomEndpointAddress()).to(properties::setCustomEndpointAddress);
        propertyMapper.from(this.producer.getPrefetchCount()).to(properties::setPrefetchCount);

        return properties;
    }

    public Consumer buildConsumerProperties() {
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

        Consumer properties = new Consumer();
        AzurePropertiesUtils.copyAzureCommonProperties(this.consumer, properties);
        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(this, properties);

        propertyMapper.from(this.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.getEventHubName()).to(properties::setEventHubName);
        propertyMapper.from(this.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(this.getCustomEndpointAddress()).to(properties::setCustomEndpointAddress);
        propertyMapper.from(this.getPrefetchCount()).to(properties::setPrefetchCount);
        propertyMapper.from(this.getConsumerGroup()).to(properties::setConsumerGroup);

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
        AzurePropertiesUtils.copyAzureCommonProperties(this.processor, properties);
        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(this, properties);

        propertyMapper.from(this.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(this.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(this.getEventHubName()).to(properties::setEventHubName);
        propertyMapper.from(this.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(this.getCustomEndpointAddress()).to(properties::setCustomEndpointAddress);
        propertyMapper.from(this.getPrefetchCount()).to(properties::setPrefetchCount);
        propertyMapper.from(this.getConsumerGroup()).to(properties::setConsumerGroup);

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
        propertyMapper.from(this.processor.batch.maxSize).to(properties.batch::setMaxSize);
        propertyMapper.from(this.processor.batch.maxWaitTime).to(properties.batch::setMaxWaitTime);
        propertyMapper.from(this.processor.loadBalancing.strategy).to(properties.loadBalancing::setStrategy);
        propertyMapper.from(this.processor.loadBalancing.updateInterval).to(properties.loadBalancing::setUpdateInterval);

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

    static class Producer extends AzureEventHubCommonProperties {

    }

    static class Consumer extends AzureEventHubConsumerProperties {

    }

    /**
     * Azure Event Processor related properties.
     */
    public static class Processor extends AzureEventHubConsumerProperties {

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
        public static class LoadBalancing {
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
        public static class Batch {
            private Duration maxWaitTime;
            private int maxSize = 1;

            public Duration getMaxWaitTime() {
                return maxWaitTime;
            }

            public void setMaxWaitTime(Duration maxWaitTime) {
                this.maxWaitTime = maxWaitTime;
            }

            public int getMaxSize() {
                return maxSize;
            }

            public void setMaxSize(int maxSize) {
                this.maxSize = maxSize;
            }
        }

        /**
         * Blob checkpoint store.
         */
        static class BlobCheckpointStore extends AzureStorageBlobProperties  {


        }
    }


}
