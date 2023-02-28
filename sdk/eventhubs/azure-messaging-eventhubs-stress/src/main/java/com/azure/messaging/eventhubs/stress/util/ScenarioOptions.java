// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.util;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Prepare all options for stress tests.<br>
 */
@Configuration
public class ScenarioOptions {
    @Value("${TEST_CLASS:#{null}}")
    private String testClass;

    @Value("${EVENTHUBS_CONNECTION_STRING:#{null}}")
    private String eventhubsConnectionString;

    @Value("${EVENTHUBS_EVENT_HUB_NAME:#{null}}")
    private String eventhubsEventHubName;

    @Value("${EVENTHUBS_CONSUMER_GROUP:" + EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME + "}")
    private String eventHubsConsumerGroup;

    @Value("${STORAGE_CONNECTION_STRING:#{null}}")
    private String storageConnectionString;

    @Value("${STORAGE_CONTAINER_NAME:#{null}}")
    private String storageContainerName;

    @Value("${METRIC_INTERVAL_SEC:60}")
    private int metricIntervalSec;

    @Value("${SECOND_EVENTHUBS_EVENT_HUB_NAME:#{null}}")
    private String secondEventhubsEventHubName;

    public String getTestClass() {
        return testClass;
    }

    public String getEventhubsConnectionString() {
        return eventhubsConnectionString;
    }

    public String getEventhubsEventHubName() {
        return eventhubsEventHubName;
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

    public int getMetricIntervalSec() {
        return metricIntervalSec;
    }

    public String getSecondEventhubsEventHubName() {
        return secondEventhubsEventHubName;
    }

}
