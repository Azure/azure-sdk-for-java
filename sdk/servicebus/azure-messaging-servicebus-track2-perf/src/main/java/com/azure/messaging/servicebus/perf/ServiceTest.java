// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.models.ReceiveMode;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

/**
 * Base class for performance test.
 * @param <TOptions> for performance configuration.
 */
abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    static final String CONTENTS = "Performance Test";
    static final int TOTAL_MESSAGE_MULTIPLIER = 300;

    private static final String AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING =
        "AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";

    private final ServiceBusClientBuilder builder;
    private final String queueName;
    private final ReceiveMode receiveMode;

    /**
     * Creates a new instance of the service bus stress test.
     *
     * @param options to configure.
     * @param receiveMode to receive messages.
     * @throws IllegalArgumentException if environment variable not being available.
     */
    ServiceTest(TOptions options, ServiceBusReceiveMode receiveMode) {
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

        this.receiveMode = receiveMode;
        this.builder = new ServiceBusClientBuilder().connectionString(connectionString);
        this.queueName = queueName;
    }

    /**
     * Gets the builder.
     *
     * @return Gets the builder.
     */
    ServiceBusClientBuilder getBuilder() {
        return builder;
    }

    String getQueueName() {
        return queueName;
    }

    ReceiveMode getReceiveMode() {
        return receiveMode;
    }
}
