package com.azure.messaging.servicebus.perf;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.perf.test.core.EventPerfTest;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.List;

public class ServiceBusEventTest<TOptions extends ServiceBusStressOptions> extends EventPerfTest<TOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusBatchTest.class);

    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";
    protected static final int TOTAL_MESSAGE_MULTIPLIER = 300;

    final ServiceBusProcessorClient processor;
    final ServiceBusSenderClient sender;
    final ServiceBusReceiverAsyncClient receiverAsync;


    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public ServiceBusEventTest(TOptions options) {
        super(options);

        String connectionString = System.getenv(AZURE_SERVICE_BUS_CONNECTION_STRING);
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("Environment variable %s must be set", AZURE_SERVICE_BUS_CONNECTION_STRING)));
        }

        String queueName = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
        if (CoreUtils.isNullOrEmpty(queueName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("Environment variable %s must be set", AZURE_SERVICEBUS_QUEUE_NAME)));
        }

        processor = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .processor()
            .queueName(queueName)
            .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            .maxConcurrentCalls(options.getMaxConcurrentCalls())
            .prefetchCount(options.getPrefetchCount())
            .processMessage(messageContext -> {
                eventRaised();
            })
            .processError(errorContext -> {
                errorRaised(errorContext.getException());
            })
            .buildProcessorClient();

        sender = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildClient();

        receiverAsync = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName(queueName)
            .buildAsyncClient();
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        for (int i = 0; i < TOTAL_MESSAGE_MULTIPLIER; i++) {
            List<ServiceBusMessage> messages = ServiceBusTestUtil.geMessagesToSend(options.getMessagesSizeBytesToSend(), options.getMessagesToSend());
            sender.sendMessages(messages);
        }
        return Mono.empty();
    }
}
