// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 *  The scenario options which use spring for configuration.
 *  Hence, tester can change the options by environment variable or commandline arguments.
 */
@Configuration
public class ScenarioOptions {
    @Value("${TEST_CLASS:#{null}}")
    private String testClass;

    @Value("${SERVICEBUS_CONNECTION_STRING:#{null}}")
    private String serviceBusConnectionString;

    @Value("${SERVICEBUS_ENTITY_TYPE:QUEUE}}")
    private EntityType serviceBusEntityType;

    @Value("${SERVICEBUS_QUEUE_NAME:#{null}}")
    private String serviceBusQueueName;

    @Value("${SERVICEBUS_SESSION_QUEUE_NAME:#{null}}")
    private String serviceBusSessionQueueName;

    @Value("${SERVICEBUS_TOPIC_NAME:#{null}}")
    private String serviceBusTopicName;

    @Value("${SERVICEBUS_SUBSCRIPTION_NAME:#{null}}")
    private String serviceBusSubscriptionName;

    @Value("${SERVICEBUS_SESSION_SUBSCRIPTION_NAME:#{null}}")
    private String serviceBusSessionSubscriptionName;

    @Value("${METRIC_INTERVAL_SEC:60}")
    private String metricIntervalSec;

    public String getTestClass() {
        return testClass;
    }

    public String getServiceBusConnectionString() {
        return serviceBusConnectionString;
    }

    public EntityType getServiceBusEntityType() {
        return serviceBusEntityType;
    }

    public String getServiceBusQueueName() {
        return serviceBusQueueName;
    }

    public String getServiceBusSessionQueueName() {
        return serviceBusSessionQueueName;
    }

    public String getServiceBusTopicName() {
        return serviceBusTopicName;
    }

    public String getServiceBusSubscriptionName() {
        return serviceBusSubscriptionName;
    }

    public String getServicBusSessionSubscriptionName() {
        return serviceBusSessionSubscriptionName;
    }

    public String getMetricIntervalSec() {
        return metricIntervalSec;
    }
}
