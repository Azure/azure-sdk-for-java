package com.azure.messaging.servicebus.perf;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.perf.test.core.EventPerfTest;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class ServiceBusEventTest<TOptions extends ServiceBusStressOptions> extends EventPerfTest<TOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusBatchTest.class);

    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";
    final ServiceBusProcessorClient processor;
    final ServiceBusSenderClient sender;
    final ServiceBusReceiverAsyncClient receiverAsync;
    Disposable receiveMessages;


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
        String messageContent = TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend());
        for (int i = 0; i < options.getMessageBatchSendTimes(); i++) {
            ServiceBusMessageBatch batch = sender.createMessageBatch();
            for (int j = 0; j < options.getMessagesToSend(); j++) {
                ServiceBusMessage message = new ServiceBusMessage(messageContent);
                message.setMessageId(UUID.randomUUID().toString());
                batch.tryAddMessage(message);
            }
            sender.sendMessages(batch);
        }
        return Mono.empty();
    }
}
