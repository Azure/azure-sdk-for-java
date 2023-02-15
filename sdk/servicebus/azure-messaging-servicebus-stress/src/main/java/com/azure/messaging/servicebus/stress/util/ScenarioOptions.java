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
    private String servicebusConnectionString;

    @Value("${SERVICEBUS_ENTITY_TYPE:QUEUE}}")
    private EntityType servicebusEntityType;

    @Value("${SERVICEBUS_QUEUE_NAME:#{null}}")
    private String servicebusQueueName;

    @Value("${SERVICEBUS_SESSION_QUEUE_NAME:#{null}}")
    private String servicebusSessionQueueName;

    @Value("${SERVICEBUS_TOPIC_NAME:#{null}}")
    private String servicebusTopicName;

    @Value("${SERVICEBUS_SUBSCRIPTION_NAME:#{null}}")
    private String servicebusSubscriptionName;

    @Value("${SERVICEBUS_SESSION_SUBSCRIPTION_NAME:#{null}}")
    private String servicebusSessionSubscriptionName;

    @Value("${METRIC_INTERVAL_SEC:60}")
    private String metricIntervalSec;

    public String getTestClass() {
        return testClass;
    }

    public String getServicebusConnectionString() {
        return servicebusConnectionString;
    }

    public EntityType getServicebusEntityType() {
        return servicebusEntityType;
    }

    public String getServicebusQueueName() {
        return servicebusQueueName;
    }

    public String getServicebusSessionQueueName() {
        return servicebusSessionQueueName;
    }

    public String getServicebusTopicName() {
        return servicebusTopicName;
    }

    public String getServicebusSubscriptionName() {
        return servicebusSubscriptionName;
    }

    public String getServicebusSessionSubscriptionName() {
        return servicebusSessionSubscriptionName;
    }

    public String getMetricIntervalSec() {
        return metricIntervalSec;
    }
}
