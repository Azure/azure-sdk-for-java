// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.properties;

/**
 *
 */
public interface ServiceBusProperties extends ServiceBusCommonProperties {

    Boolean getCrossEntityTransactions();

    ServiceBusProducerProperties getProducer();

    ServiceBusConsumerProperties getConsumer();

    ServiceBusProcessorProperties getProcessor();

}
