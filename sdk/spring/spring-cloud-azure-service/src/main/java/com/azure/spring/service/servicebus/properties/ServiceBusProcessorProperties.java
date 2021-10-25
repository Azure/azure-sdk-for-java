// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.properties;

/**
 *
 */
public interface ServiceBusProcessorProperties extends ServiceBusConsumerProperties {

    Integer getMaxConcurrentCalls();

    Integer getMaxConcurrentSessions();

}
