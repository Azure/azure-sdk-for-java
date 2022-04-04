// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties;

import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties.AzureStorageBlobProperties;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.service.implementation.core.PropertiesValidator;
import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventHubConsumerProperties;
import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventHubProducerProperties;
import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventHubsNamespaceProperties;
import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventProcessorClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Azure Event Hubs related properties.
 */
public class AzureEventHubsProperties extends AzureEventHubsCommonProperties
    implements EventHubsNamespaceProperties, InitializingBean {

    public static final String PREFIX = "spring.cloud.azure.eventhubs";
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureEventHubsProperties.class);

    /**
     * Whether to share the same connection for producers or consumers.
     */
    private Boolean sharedConnection;
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
        propertyMapper.from(this.processor.initialPartitionEventPosition).when(c -> !CollectionUtils.isEmpty(c))
                      .to(m -> {
                          Map<String, Processor.StartPosition> eventPositionMap = m.entrySet()
                                                                                   .stream()
                                                                                   .filter(entry -> entry.getValue() != null)
                                                                                   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                          properties.getInitialPartitionEventPosition().putAll(eventPositionMap);
                      });
        propertyMapper.from(this.processor.batch.getMaxSize()).to(properties.batch::setMaxSize);
        propertyMapper.from(this.processor.batch.getMaxWaitTime()).to(properties.batch::setMaxWaitTime);
        propertyMapper.from(this.processor.loadBalancing.getStrategy()).to(properties.loadBalancing::setStrategy);
        propertyMapper.from(this.processor.loadBalancing.getUpdateInterval()).to(properties.loadBalancing::setUpdateInterval);

        AzurePropertiesUtils.copyAzureCommonProperties(this.processor.checkpointStore, properties.checkpointStore);
        BeanUtils.copyProperties(this.processor.checkpointStore, properties.checkpointStore);

        return properties;
    }

    public Boolean getSharedConnection() {
        return sharedConnection;
    }

    public void setSharedConnection(Boolean sharedConnection) {
        this.sharedConnection = sharedConnection;
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
    public static class Producer extends AzureEventHubsCommonProperties implements EventHubProducerProperties {

    }

    /**
     * Properties of an Event Hub consumer.
     */
    public static class Consumer extends AzureEventHubsCommonProperties implements EventHubConsumerProperties {

        /**
         * Name of the consumer group this consumer is associated with.
         */
        protected String consumerGroup;

        /**
         * The number of events the Event Hub consumer will actively receive and queue locally without regard to whether
         * a receiving operation is currently active.
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
    public static class Processor extends Consumer implements EventProcessorClientProperties {

        /**
         * Whether request information on the last enqueued event on its associated partition, and track that
         * information as events are received.
         */
        private Boolean trackLastEnqueuedEventProperties;
        /**
         * Map event position to use for each partition if a checkpoint for the partition does not exist in
         * CheckpointStore.
         */
        private final Map<String, StartPosition> initialPartitionEventPosition = new HashMap<>();

        private final EventBatch batch = new EventBatch();
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

        public EventBatch getBatch() {
            return batch;
        }

        public LoadBalancing getLoadBalancing() {
            return loadBalancing;
        }

        public BlobCheckpointStore getCheckpointStore() {
            return checkpointStore;
        }

        /**
         * The starting position from which to consume events.
         */
        public static class StartPosition implements EventProcessorClientProperties.StartPosition {

            /**
             * The offset of the event within that partition. String keyword, "earliest" and "latest"
             * (case-insensitive), are reserved for specifying the start and end of the partition. Other provided value
             * will be cast to Long.
             */
            private String offset;
            /**
             * The sequence number of the event within that partition.
             */
            private Long sequenceNumber;
            /**
             * The event enqueued after the requested enqueuedDateTime becomes the current position.
             */
            private Instant enqueuedDateTime;
            /**
             * Whether the event of the specified sequence number is included.
             */
            private boolean inclusive = false;

            @Override
            public String getOffset() {
                return offset;
            }

            public void setOffset(String offset) {
                this.offset = offset;
            }

            @Override
            public Long getSequenceNumber() {
                return sequenceNumber;
            }

            public void setSequenceNumber(Long sequenceNumber) {
                this.sequenceNumber = sequenceNumber;
            }

            @Override
            public Instant getEnqueuedDateTime() {
                return enqueuedDateTime;
            }

            public void setEnqueuedDateTime(Instant enqueuedDateTime) {
                this.enqueuedDateTime = enqueuedDateTime;
            }

            public boolean isInclusive() {
                return inclusive;
            }

            public void setInclusive(boolean inclusive) {
                this.inclusive = inclusive;
            }
        }

        /**
         * Event processor load balancing properties.
         */
        public static class LoadBalancing implements EventProcessorClientProperties.LoadBalancing {
            /**
             * The time interval between load balancing update cycles.
             */
            private Duration updateInterval;
            /**
             * The load balancing strategy for claiming partition ownership.
             */
            private LoadBalancingStrategy strategy = LoadBalancingStrategy.BALANCED;
            /**
             * The time duration after which the ownership of partition expires.
             */
            private Duration partitionOwnershipExpirationInterval;

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

            public Duration getPartitionOwnershipExpirationInterval() {
                return partitionOwnershipExpirationInterval;
            }

            public void setPartitionOwnershipExpirationInterval(Duration partitionOwnershipExpirationInterval) {
                this.partitionOwnershipExpirationInterval = partitionOwnershipExpirationInterval;
            }
        }

        /**
         * Event processor batch properties.
         */
        public static class EventBatch implements EventProcessorClientProperties.EventBatch {

            /**
             * The max time duration to wait to receive an event before processing events.
             */
            private Duration maxWaitTime;
            /**
             * The maximum number of events that will be in the batch.
             */
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
        public static class BlobCheckpointStore extends AzureStorageBlobProperties {

            /**
             * Whether to create the container if it does not exist.
             */
            private boolean createContainerIfNotExists = false;


            public boolean isCreateContainerIfNotExists() {
                return createContainerIfNotExists;
            }

            public void setCreateContainerIfNotExists(Boolean createContainerIfNotExists) {
                this.createContainerIfNotExists = createContainerIfNotExists;
            }
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
