// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.perf;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Base class for performance etest.
 *
 * @param <TOptions> for performance configuration.
 */
abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    static final String CONTENTS = "Performance Test";

    private static final String AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING =
        "AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";

    private final ClientLogger logger;
    private final MessagingFactory factory;
    private final String queueName;

    /**
     * Creates a new instance of the service bus stress test.
     *
     * @param logger Client logger.
     * @param options to configure.
     *
     * @throws IllegalArgumentException for environment variable not being available.
     * @throws RuntimeException if the {@link MessagingFactory} cannot be created.
     */
    ServiceTest(TOptions options, ClientLogger logger) {
        super(options);
        this.logger = logger;

        final String connectionString = System.getenv(AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING);
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Environment variable "
                + AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING + " must be set."));
        }

        this.queueName = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
        if (CoreUtils.isNullOrEmpty(queueName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Environment variable "
                + AZURE_SERVICEBUS_QUEUE_NAME + " must be set."));
        }

        try {
            this.factory = MessagingFactory.createFromConnectionString(connectionString);
        } catch (InterruptedException | ExecutionException e) {
            throw logger.logExceptionAsWarning(new RuntimeException("Unable to create messaging factory.", e));
        }
    }

    /**
     * Gets the client logger.
     *
     * @return The logger.
     */
    ClientLogger getLogger() {
        return logger;
    }

    /**
     * Gets the underlying AMQP connection to a service bus namespace.
     *
     * @return The factory to create to a service bus namespace.
     */
    MessagingFactory getMessagingFactory() {
        return factory;
    }

    /**
     * Gets the name of the queue to send/receive messages to/from.
     *
     * @return Name of the queue to send/receive messages to/from.
     */
    String getQueueName() {
        return queueName;
    }

    /**
     * Gets the given number of Service Bus messages with {@link #CONTENTS}.
     *
     * @param count Number of messages to emit.
     *
     * @return A list of {@link Message messages}.
     */
    List<IMessage> getMessages(int count) {
        return IntStream.range(0, count)
            .mapToObj(index -> {
                final Message message = new Message(CONTENTS);
                message.setMessageId(String.valueOf(index));
                return message;
            })
            .collect(Collectors.toList());
    }

    void dispose(AutoCloseable... closeables) {
        for (int i = 0; i < closeables.length; i++) {
            final AutoCloseable closeable = closeables[i];
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (Exception e) {
                logger.warning("Unable to dispose of {}.", closeable.getClass(), e);
            }
        }
    }
}
