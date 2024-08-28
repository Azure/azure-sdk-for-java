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

    @Value("${EVENTHUBS_CONNECTION_STRING:#{null}}")
    private String eventhubsConnectionString;

    @Value("${EVENTHUBS_EVENT_HUB_NAME:#{null}}")
    private String eventHubsEventHubName;

    @Value("${EVENTHUBS_CONSUMER_GROUP:$Default}")
    private String eventHubsConsumerGroup;

    @Value("${STORAGE_CONNECTION_STRING:#{null}}")
    private String storageConnectionString;

    @Value("${STORAGE_CONTAINER_NAME:#{null}}")
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

    public String getTestClass() {
        return testClass;
    }

    public String getEventHubsConnectionString() {
        return eventhubsConnectionString;
    }

    public String getEventHubsEventHubName() {
        return eventHubsEventHubName;
    }

    public String getEventHubsConsumerGroup() {
        return eventHubsConsumerGroup;
    }

    public String getStorageConnectionString() {
        return storageConnectionString;
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

    public Duration getIdleDuration() {
        if  (idleDurationInMinutes == 0) {
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
}
