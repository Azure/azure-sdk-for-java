package com.azure.messaging.servicebus.perf;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.*;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.perf.test.core.BatchPerfTest;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ReceiveMessagesTest extends BatchPerfTest<ServiceBusStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(ReceiveMessagesTest.class);

    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";

    private final ServiceBusSenderAsyncClient senderAsync;
    private final ServiceBusReceiverAsyncClient receiverAsync;
    private final ServiceBusReceiverClient receiver;

    /**
     * Creates an instance of Batch performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public ReceiveMessagesTest(ServiceBusStressOptions options) {
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
        senderAsync = builder.sender().queueName(queueName).buildAsyncClient();
        ServiceBusReceiveMode receiveMode = options.getIsDeleteMode() ? ServiceBusReceiveMode.RECEIVE_AND_DELETE : ServiceBusReceiveMode.PEEK_LOCK;
        receiver = builder.receiver().queueName(queueName).receiveMode(receiveMode).buildClient();
        receiverAsync = builder.receiver().queueName(queueName).receiveMode(receiveMode).buildAsyncClient();

    }

    @Override
    public int runBatch() {
        IterableStream<ServiceBusReceivedMessage> messages = receiver.receiveMessages(options.getMessagesToReceive());
        int count = 0;

        for (ServiceBusReceivedMessage message : messages) {
            if (!options.getIsDeleteMode()) {
                receiver.complete(message);
                count++;
            }
        }
        if (count <= 0) {
            throw LOGGER.logExceptionAsWarning(new RuntimeException("Error. Should have received some messages."));
        }
        return count;
    }

    @Override
    public Mono<Integer> runBatchAsync() {
        AtomicInteger count = new AtomicInteger();
        return receiverAsync
            .receiveMessages()
            .take(options.getMessagesToReceive())
            .flatMap(message -> {
                count.getAndIncrement();
                return receiverAsync.complete(message);
            }, 1).then().thenReturn(count.get());

    }

    @Override
    public Mono<Void> setupAsync() {
        return Mono.defer(() -> {
            List<ServiceBusMessage> messages = new ArrayList<>();
            for (int i = 0; i < options.getMessagesToReceive(); i++) {
                ServiceBusMessage message = new ServiceBusMessage(
                    TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend()));
                message.setMessageId(UUID.randomUUID().toString());
                messages.add(message);
            }
            return senderAsync.sendMessages(messages);
        });
    }

}
