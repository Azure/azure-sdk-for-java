// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

import java.util.concurrent.ExecutionException;

/**
 * Base class for performance etest.
 * @param <TOptions> for performance configuration.
 */
public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";
    protected static final int TOTAL_MESSAGE_MULTIPLIER = 300;

    private final MessagingFactory factory;

    protected IMessageSender sender;
    protected IMessageReceiver receiver;

    /**
     *
     * @param options to configure.
     * @param receiveMode to receive messages.
     * @throws IllegalArgumentException for environment variable not being available.
     */
    public ServiceTest(TOptions options, ReceiveMode receiveMode) {
        super(options);
        String connectionString = System.getenv(AZURE_SERVICE_BUS_CONNECTION_STRING);
        if (connectionString == null || connectionString.length() == 0) {
            throw new IllegalArgumentException("Environment variable "
                + AZURE_SERVICE_BUS_CONNECTION_STRING + " must be set.");
        }

        String queueName = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
        if (queueName == null || queueName.length() == 0) {
            throw new IllegalArgumentException("Environment variable "
                + AZURE_SERVICEBUS_QUEUE_NAME + " must be set.");
        }

        // Setup the service client
        try {
            this.factory = MessagingFactory.createFromConnectionString(connectionString);
            this.sender = ClientFactory.createMessageSenderFromEntityPath(factory, queueName);
            this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, queueName, receiveMode);
        } catch (ServiceBusException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
