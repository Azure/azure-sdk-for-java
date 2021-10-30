// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.properties;

/**
 *
 */
public interface ServiceBusProducerDescriptor extends ServiceBusCommonDescriptor {

    String getQueueName();

    String getTopicName();

}
