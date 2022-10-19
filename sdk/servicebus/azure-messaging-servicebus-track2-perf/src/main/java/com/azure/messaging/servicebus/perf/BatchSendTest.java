package com.azure.messaging.servicebus.perf;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.perf.test.core.BatchPerfTest;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BatchSendTest extends BatchPerfTest<ServiceBusStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(BatchSendTest.class);

    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";

    private final ServiceBusSenderClient sender;
    private final ServiceBusSenderAsyncClient senderAsync;

    /**
     * Creates an instance of Batch performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public BatchSendTest(ServiceBusStressOptions options) {
        super(options);

        String connectionString = System.getenv(AZURE_SERVICE_BUS_CONNECTION_STRING);
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("Environment variable %s must be set", AZURE_SERVICE_BUS_CONNECTION_STRING)));
        }

        ServiceBusClientBuilder builder = new ServiceBusClientBuilder().connectionString(connectionString);
        String queueName = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
        if (CoreUtils.isNullOrEmpty(queueName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("Environment variable %s must be set", AZURE_SERVICEBUS_QUEUE_NAME)));
        }
        sender = builder.sender().queueName(queueName).buildClient();
        senderAsync = builder.sender().queueName(queueName).buildAsyncClient();

    }

    @Override
    public int runBatch() {
        List<ServiceBusMessage> messages = getSendMessages();
        sender.sendMessages(messages);
        return messages.size();
    }

    @Override
    public Mono<Integer> runBatchAsync() {
        List<ServiceBusMessage> messages = getSendMessages();
        return senderAsync.sendMessages(messages).thenReturn(messages.size());
    }

    private List<ServiceBusMessage> getSendMessages() {
        List<ServiceBusMessage> messages = new ArrayList<>();
        for (int i = 0; i < options.getMessagesToSend(); i++) {
            ServiceBusMessage message = new ServiceBusMessage(
                TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend()));
            message.setMessageId(UUID.randomUUID().toString());
            messages.add(message);
        }
        return messages;
    }

}
