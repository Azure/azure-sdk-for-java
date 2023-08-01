// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;

import java.time.Duration;

/**
 * Class containing code snippets that set advanced configurations.
 */
public class AdvancedConfigurationSample {

    /**
     * Create an asynchronous receiver that use customized retry options.
     */
    public void createAsyncServiceBusReceiver() {
        AmqpRetryOptions retryOptions = new AmqpRetryOptions();
        retryOptions.setTryTimeout(Duration.ofSeconds(60));
        retryOptions.setMaxRetries(3);
        retryOptions.setDelay(Duration.ofMillis(800));

        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .retryOptions(retryOptions)
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
    }

    /**
     * Create an asynchronous receiver that prefetch 100 messages.
     */
    public void createAsyncServiceBusReceiverWithPrefetch() {
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .prefetchCount(100)
            .buildAsyncClient();
    }
}
