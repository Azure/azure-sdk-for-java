package com.azure.messaging.servicebus.perf.core;

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

    public ServiceTest(TOptions options, ReceiveMode receiveMode) throws ExecutionException, InterruptedException, ServiceBusException {
        super(options);
        String connectionString = System.getenv(AZURE_SERVICE_BUS_CONNECTION_STRING);
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            System.out.println("Environment variable AZURE_SERVICE_BUS_CONNECTION_STRING must be set");
            System.exit(1);
        }

        String queueName = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
        if (CoreUtils.isNullOrEmpty(queueName)) {
            System.out.println("Environment variable AZURE_SERVICEBUS_QUEUE_NAME must be set");
            System.exit(1);
        }

        // Setup the service client
        this.factory = MessagingFactory.createFromConnectionString(connectionString);
        this.sender = ClientFactory.createMessageSenderFromEntityPath(this.factory, queueName);
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, queueName, receiveMode);
    }
}
