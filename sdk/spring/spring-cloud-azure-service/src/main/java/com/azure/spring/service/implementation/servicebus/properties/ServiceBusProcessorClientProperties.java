// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.servicebus.properties;

/**
 *
 */
public interface ServiceBusProcessorClientProperties extends ServiceBusReceiverClientProperties {

    Integer getMaxConcurrentCalls();

    Integer getMaxConcurrentSessions();

}
