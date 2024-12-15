// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.util;

import com.azure.core.amqp.AmqpTransportType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Prepare all options for stress tests.
 */
@Configuration
public class ScenarioOptions {
    @Value("${TEST_CLASS:#{null}}")
    private String testClass;

    @Value("${EVENT_HUBS_FULLY_QUALIFIED_NAMESPACE}")
    private String eventHubsFullyQualifiedNamespace;

    @Value("${EVENT_HUBS_EVENT_HUB_NAME:#{null}}")
    private String eventHubsEventHubName;

    @Value("${EVENT_HUBS_CONSUMER_GROUP:$Default}")
    private String eventHubsConsumerGroup;

    @Value("${STORAGE_BLOB_ENDPOINT_URI}")
    private String storageBlobEndpointUri;

    @Value("${STORAGE_CONTAINER_NAME}")
    private String storageContainerName;

    @Value("${DURATION_MINUTES:15}")
    private int durationInMinutes;

    @Value("${MESSAGE_SIZE_IN_BYTES:16}")
    private int messageSize;

    @Value("${DELAY_START_MINUTES:0}")
    private int delayTestStartInMinutes;

    @Value("${AMQP_TRANSPORT_TYPE:AMQP}")
    private AmqpTransportType amqpTransportType;

    @Value("${IDLE_DURATION_MINUTES:0}")
    private int idleDurationInMinutes;

    @Value("${USE_V2:false}")
    private boolean useV2Stack;

    public String getTestClass() {
        return testClass;
    }

    public String getEventHubsFullyQualifiedNamespace() {
        return eventHubsFullyQualifiedNamespace;
    }

    public void setEventHubsFullyQualifiedNamespace(String eventHubsFullyQualifiedNamespace) {
        this.eventHubsFullyQualifiedNamespace = eventHubsFullyQualifiedNamespace;
    }

    /**
     * Gets the Event Hub name.
     *
     * @return The Event Hub name.
     */
    public String getEventHubsEventHubName() {
        return eventHubsEventHubName;
    }

    public String getEventHubsConsumerGroup() {
        return eventHubsConsumerGroup;
    }

    public String getStorageBlobEndpointUri() {
        return storageBlobEndpointUri;
    }

    public void setStorageBlobEndpointUri(String storageBlobEndpointUri) {
        this.storageBlobEndpointUri = storageBlobEndpointUri;
    }

    public String getStorageContainerName() {
        return storageContainerName;
    }

    public Duration getTestDuration() {
        return Duration.ofMinutes(durationInMinutes);
    }

    public Duration getStartDelay() {
        return Duration.ofMinutes(delayTestStartInMinutes);
    }

    /**
     * Gets the idle duration.
     *
     * @return The idle duration.
     * @throws IllegalArgumentException If the idle duration in minutes is less than zero.
     */
    public Duration getIdleDuration() {
        if (idleDurationInMinutes == 0) {
            return Duration.ZERO;
        }

        if (idleDurationInMinutes < 0) {
            throw new IllegalArgumentException("idleDurationInMinutes must be >= 0");
        }

        return Duration.ofMinutes(idleDurationInMinutes);
    }

    public int getMessageSize() {
        return messageSize;
    }

    public AmqpTransportType getAmqpTransportType() {
        return amqpTransportType;
    }

    public boolean useV2Stack() {
        return useV2Stack;
    }
}
