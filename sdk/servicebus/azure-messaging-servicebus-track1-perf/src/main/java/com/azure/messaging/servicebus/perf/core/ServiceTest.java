package com.azure.messaging.servicebus.perf.core;
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

import java.util.concurrent.ExecutionException;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";
    protected  static final String CONTENTS = "Track 1 AMQP message - Perf Test";

    private MessagingFactory factory;
    protected IMessageSender sender;
    protected IMessageReceiver receiver;

    public ServiceTest(TOptions options, ReceiveMode receiveMode) {
        super(options);
        String connectionString = System.getenv(AZURE_SERVICE_BUS_CONNECTION_STRING);
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalArgumentException("Environment variable "+AZURE_SERVICE_BUS_CONNECTION_STRING+" must be set");
        }

        String queueName = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
        if (CoreUtils.isNullOrEmpty(queueName)) {
            throw new IllegalArgumentException("Environment variable "+AZURE_SERVICEBUS_QUEUE_NAME+" must be set");
        }

        // Setup the service client
        try {
            this.factory = MessagingFactory.createFromConnectionString(connectionString);
            this.sender = ClientFactory.createMessageSenderFromEntityPath(factory, queueName);
            this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, queueName, receiveMode);
        } catch (Exception e) {
            throw new RuntimeException("Problem in creating client. " + e.getMessage());
        }
    }
}
