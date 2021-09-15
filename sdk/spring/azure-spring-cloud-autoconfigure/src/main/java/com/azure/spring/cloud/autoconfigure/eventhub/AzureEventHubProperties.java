// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.spring.cloud.autoconfigure.properties.AbstractAzureAmqpConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.storage.blob.AzureStorageBlobProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Azure Event Hub related properties.
 */
@Validated
public class AzureEventHubProperties extends AbstractAzureAmqpConfigurationProperties {

    public static final String PREFIX = "spring.cloud.azure.eventhub";

    private String domainName = "servicebus.windows.net";
    private String namespace;
    private String eventHubName;
    private String connectionString;
    private boolean isSharedConnection;
    private String customEndpointAddress;
    private String consumerGroup;
    private Integer prefetchCount;

    private final Processor processor = new Processor();

    // FQDN = the FQDN of the EventHubs namespace you created (it includes the EventHubs namespace name followed by
    // servicebus.windows.net)
    // Endpoint=sb://<FQDN>/;SharedAccessKeyName=<KeyName>;SharedAccessKey=<KeyValue>
    // https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-get-connection-string
    public String getFQDN() {
        return this.namespace + "." + domainName;
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

    public String getEventHubName() {
        return eventHubName;
    }

    public void setEventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public boolean isSharedConnection() {
        return isSharedConnection;
    }

    public void setSharedConnection(boolean sharedConnection) {
        isSharedConnection = sharedConnection;
    }

    public String getCustomEndpointAddress() {
        return customEndpointAddress;
    }

    public void setCustomEndpointAddress(String customEndpointAddress) {
        this.customEndpointAddress = customEndpointAddress;
    }

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

    public Processor getProcessor() {
        return processor;
    }

    /**
     * Azure Event Processor related properties.
     */
    public static class Processor {
        private boolean trackLastEnqueuedEventProperties;
        private Map<String, EventPosition> initialPartitionEventPosition = new HashMap<>();
        private Duration partitionOwnershipExpirationInterval;
        private final Batch batch = new Batch();
        private final LoadBalancing loadBalancing = new LoadBalancing();
        private final BlobCheckpointStore checkpointStore = new BlobCheckpointStore();

        public boolean isTrackLastEnqueuedEventProperties() {
            return trackLastEnqueuedEventProperties;
        }

        public void setTrackLastEnqueuedEventProperties(boolean trackLastEnqueuedEventProperties) {
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

        public LoadBalancing getLoadBalancing() {
            return loadBalancing;
        }

        public Batch getBatch() {
            return batch;
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
