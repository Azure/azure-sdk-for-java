// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

import java.time.Duration;

/**
 * Base class for performance test.
 * @param <TOptions> for performance configuration.
 */
abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    private final ClientLogger logger = new ClientLogger(ServiceTest.class);
    protected static final int TOTAL_MESSAGE_MULTIPLIER = 300;

    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";
    private static final String AZURE_SERVICEBUS_TOPIC_NAME = "AZURE_SERVICEBUS_TOPIC_NAME";
    private static final String AZURE_SERVICEBUS_SUBSCRIPTION_NAME = "AZURE_SERVICEBUS_SUBSCRIPTION_NAME";

    final ServiceBusReceiverClient receiver;
    final ServiceBusReceiverAsyncClient receiverAsync;
    final ServiceBusSenderClient sender;
    final ServiceBusSenderAsyncClient senderAsync;

    /**
     *
     * @param options to configure.
     * @param receiveMode to receive messages.
     * @throws IllegalArgumentException if environment variable not being available.
     */
    ServiceTest(TOptions options, ServiceBusReceiveMode receiveMode) {
        super(options);
        String connectionString = System.getenv(AZURE_SERVICE_BUS_CONNECTION_STRING);
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Environment variable "
                + AZURE_SERVICE_BUS_CONNECTION_STRING + " must be set."));
        }

        String queueName = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
        if (CoreUtils.isNullOrEmpty(queueName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Environment variable "
                + AZURE_SERVICEBUS_QUEUE_NAME + " must be set."));
        }

        // Setup the service client
        final ServiceBusClientBuilder baseBuilder = new ServiceBusClientBuilder()
            .proxyOptions(ProxyOptions.SYSTEM_DEFAULTS)
            .retryOptions(new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(60)))
            .transportType(AmqpTransportType.AMQP)
            .connectionString(connectionString);

        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder = baseBuilder
            .receiver()
            .receiveMode(receiveMode)
            .queueName(queueName);

        ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderBuilder = baseBuilder
            .sender()
            .queueName(queueName);

        receiver = receiverBuilder.buildClient();
        receiverAsync = receiverBuilder.buildAsyncClient();

        sender = senderBuilder.buildClient();
        senderAsync = senderBuilder.buildAsyncClient();
    }
}
