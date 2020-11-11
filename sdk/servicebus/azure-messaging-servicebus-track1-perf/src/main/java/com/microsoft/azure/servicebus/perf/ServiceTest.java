// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.perf;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
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
abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    static final String CONTENTS = "Performance Test";
    static final int TOTAL_MESSAGE_MULTIPLIER = 300;

    private static final String AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING =
        "AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";

    private final IMessageSender sender;
    private final IMessageReceiver receiver;

    /**
     * Creates a new instance of the service bus stress test.
     *
     * @param options to configure.
     * @param receiveMode to receive messages.
     * @throws IllegalArgumentException for environment variable not being available.
     */
    ServiceTest(TOptions options, ReceiveMode receiveMode) {
        super(options);
        final ClientLogger logger = new ClientLogger(ServiceTest.class);

        final String connectionString = System.getenv(AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING);
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Environment variable "
                + AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING + " must be set."));
        }

        final String queueName = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
        if (CoreUtils.isNullOrEmpty(queueName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Environment variable "
                + AZURE_SERVICEBUS_QUEUE_NAME + " must be set."));
        }

        // Setup the service client
        try {
            final MessagingFactory factory = MessagingFactory.createFromConnectionString(connectionString);

            this.sender = ClientFactory.createMessageSenderFromEntityPath(factory, queueName);
            this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, queueName, receiveMode);
        } catch (ServiceBusException | InterruptedException | ExecutionException e) {
            throw logger.logExceptionAsWarning(new RuntimeException(e));
        }
    }

    public IMessageSender getSender() {
        return sender;
    }

    public IMessageReceiver getReceiver() {
        return receiver;
    }
}
