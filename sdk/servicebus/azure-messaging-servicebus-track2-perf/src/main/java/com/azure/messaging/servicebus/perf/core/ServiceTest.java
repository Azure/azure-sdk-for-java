package com.azure.messaging.servicebus.perf.core;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.CoreUtils;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ReceiveMode;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    protected static final Duration TIMEOUT = Duration.ofSeconds(60);
    protected static final AmqpRetryOptions RETRY_OPTIONS = new AmqpRetryOptions().setTryTimeout(TIMEOUT);
    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";
    private static final String AZURE_SERVICEBUS_TOPIC_NAME = "AZURE_SERVICEBUS_TOPIC_NAME";
    private static final String AZURE_SERVICEBUS_SUBSCRIPTION_NAME = "AZURE_SERVICEBUS_SUBSCRIPTION_NAME";


    protected final ServiceBusReceiverClient receiver;
    protected final ServiceBusReceiverAsyncClient receiverAsync;
    protected final ServiceBusSenderClient sender;
    protected final ServiceBusSenderAsyncClient senderAsync;

    protected final String CONTENTS = "Performance Test Message";

    public ServiceTest(TOptions options) {
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
        final ServiceBusClientBuilder baseBuilder = new ServiceBusClientBuilder()
            .proxyOptions(ProxyOptions.SYSTEM_DEFAULTS)
            .retryOptions(RETRY_OPTIONS)
            .transportType(AmqpTransportType.AMQP)
            .connectionString(connectionString);

        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder = baseBuilder
            .receiver()
           // .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
            .queueName(queueName);

        ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderBuilder = baseBuilder
            .sender()
            .queueName(queueName);

        receiver = receiverBuilder.buildClient();
        receiverAsync = receiverBuilder.buildAsyncClient();

        sender = senderBuilder.buildClient();
        senderAsync = senderBuilder.buildAsyncClient();
    }
}
