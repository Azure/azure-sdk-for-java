package com.microsoft.azure.servicebus.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.BatchPerfTest;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

import java.util.concurrent.ExecutionException;

public abstract class ServiceBatchTest<TOptions extends ServiceBusStressOptions> extends BatchPerfTest<TOptions> {

    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";
    protected static final int TOTAL_MESSAGE_MULTIPLIER = 300;

    private final MessagingFactory factory;

    protected IMessageSender sender;
    protected IMessageReceiver receiver;

    /**
     * Creates an instance of Batch performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public ServiceBatchTest(TOptions options) {
        super(options);
        String connectionString = System.getenv(AZURE_SERVICE_BUS_CONNECTION_STRING);
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalArgumentException(
                String.format("Environment variable %s must be set", AZURE_SERVICE_BUS_CONNECTION_STRING));
        }

        String queueName = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
        if (CoreUtils.isNullOrEmpty(queueName)) {
            throw new IllegalArgumentException(
                String.format("Environment variable %s must be set", AZURE_SERVICEBUS_QUEUE_NAME));
        }

        ReceiveMode receiveMode = options.getIsDeleteMode() ? ReceiveMode.RECEIVEANDDELETE : ReceiveMode.PEEKLOCK;
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
