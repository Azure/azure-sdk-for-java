// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.perf.test.core.BatchPerfTest;

/**
 * Base class for batch performance test.
 * @param <TOptions> for performance configuration.
 */
abstract class ServiceBusBatchTest<TOptions extends ServiceBusStressOptions> extends BatchPerfTest<TOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusBatchTest.class);

    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";
    protected static final int TOTAL_MESSAGE_MULTIPLIER = 300;

    final ServiceBusReceiverClient receiver;
    final ServiceBusReceiverAsyncClient receiverAsync;
    final ServiceBusSenderClient sender;
    final ServiceBusSenderAsyncClient senderAsync;
    final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder;

    /**
     * Creates an instance of Batch performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public ServiceBusBatchTest(TOptions options) {
        super(options);
        String connectionString = System.getenv(AZURE_SERVICE_BUS_CONNECTION_STRING);
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("Environment variable %s must be set", AZURE_SERVICE_BUS_CONNECTION_STRING)));
        }

        ServiceBusClientBuilder builder = new ServiceBusClientBuilder().connectionString(connectionString);
        String queueName = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
        if (CoreUtils.isNullOrEmpty(queueName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("Environment variable %s must be set", AZURE_SERVICEBUS_QUEUE_NAME)));
        }

        sender = builder.sender().queueName(queueName).buildClient();
        senderAsync = builder.sender().queueName(queueName).buildAsyncClient();
        ServiceBusReceiveMode receiveMode = options.getIsDeleteMode() ? ServiceBusReceiveMode.RECEIVE_AND_DELETE : ServiceBusReceiveMode.PEEK_LOCK;
        receiverClientBuilder = builder.receiver().queueName(queueName).receiveMode(receiveMode);
        receiver = receiverClientBuilder.buildClient();
        receiverAsync = receiverClientBuilder.buildAsyncClient();
    }
}
