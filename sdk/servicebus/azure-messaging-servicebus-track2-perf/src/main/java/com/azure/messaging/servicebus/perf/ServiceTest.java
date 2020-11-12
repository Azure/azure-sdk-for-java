// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Base class for performance test.
 *
 * @param <TOptions> for performance configuration.
 */
abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    static final String CONTENTS = "Performance Test";

    private static final String AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING =
        "AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";

    private final ServiceBusClientBuilder builder;
    private final String queueName;
    private final ClientLogger logger;

    /**
     * Creates a new instance of the service bus stress test.
     *
     * @param logger Client logger.
     * @param options to configure.
     *
     * @throws IllegalArgumentException if environment variable not being available.
     */
    ServiceTest(TOptions options, ClientLogger logger) {
        super(options);
        this.logger = logger;

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

        this.builder = new ServiceBusClientBuilder().connectionString(connectionString);
        this.queueName = queueName;
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
     * Gets the builder.
     *
     * @return Gets the builder.
     */
    ServiceBusClientBuilder getBuilder() {
        return builder;
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
     * @return A list of {@link ServiceBusMessage messages}.
     */
    List<ServiceBusMessage> getMessages(int count) {
        return IntStream.range(0, count)
            .mapToObj(index -> new ServiceBusMessage(CONTENTS).setMessageId(String.valueOf(index)))
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
