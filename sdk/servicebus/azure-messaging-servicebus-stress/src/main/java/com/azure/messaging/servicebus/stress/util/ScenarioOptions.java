// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.util;

import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ScenarioOptions {
    @Value("${TEST_CLASS:#{null}}")
    private String testClass;

    @Value("${SERVICEBUS_CONNECTION_STRING:#{null}}")
    private String servicebusConnectionString;

    @Value("${SERVICEBUS_ENTITY_TYPE:QUEUE}}")
    private MessagingEntityType servicebusEntityType;

    @Value("${SERVICEBUS_QUEUE_NAME:#{null}}")
    private String servicebusQueueName;

    @Value("${SERVICEBUS_TOPIC_NAME:#{null}}")
    private String servicebusTopicName;

    @Value("${SERVICEBUS_SUBSCRIPTION_NAME:#{null}}")
    private String servicebusSubscriptionName;

    @Value("${METRIC_INTERVAL_SEC:60}")
    private String metricIntervalSec;

    public String getTestClass() {
        return testClass;
    }

    public String getServicebusConnectionString() {
        return servicebusConnectionString;
    }

    public MessagingEntityType getServicebusEntityType() {
        return servicebusEntityType;
    }

    public String getServicebusQueueName() {
        return servicebusQueueName;
    }

    public String getServicebusTopicName() {
        return servicebusTopicName;
    }

    public String getServicebusSubscriptionName() {
        return servicebusSubscriptionName;
    }

    public String getMetricIntervalSec() {
        return metricIntervalSec;
    }
}
