package com.azure.messaging.servicebus.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final ServiceBusReceiverClient receiver;
    protected final ServiceBusReceiverAsyncClient receiverAsync;

    protected final ServiceBusSenderClient sender;
    protected final ServiceBusSenderAsyncClient senderAsync;

    public ServiceTest(TOptions options) {
        super(options);
        String connectionString = System.getenv("SERVICEBUS_CONNECTION_STRING");
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            System.out.println("Environment variable STORAGE_CONNECTION_STRING must be set");
            System.exit(1);
        }

        // Setup the service client
        ServiceBusClientBuilder baseBuilder = new ServiceBusClientBuilder()
            .connectionString(connectionString);

        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder = baseBuilder.receiver();

        ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderBuilder = baseBuilder.sender();

        receiver = receiverBuilder.buildClient();
        receiverAsync = receiverBuilder.buildAsyncClient();

        sender = senderBuilder.buildClient();
        senderAsync = senderBuilder.buildAsyncClient();
    }
}
