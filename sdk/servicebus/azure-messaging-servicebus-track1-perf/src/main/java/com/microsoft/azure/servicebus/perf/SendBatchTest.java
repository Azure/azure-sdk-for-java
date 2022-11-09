package com.microsoft.azure.servicebus.perf;

import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.perf.core.ServiceBatchTest;
import com.microsoft.azure.servicebus.perf.core.ServiceBusStressOptions;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SendBatchTest extends ServiceBatchTest<ServiceBusStressOptions> {
    private final List<IMessage> messages;

    /**
     * Creates an instance of Batch performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public SendBatchTest(ServiceBusStressOptions options) {
        super(options);
        String messageContent = TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend());

        messages = new ArrayList<>();
        for (int i = 0; i < options.getMessagesToSend(); ++i) {
            Message message = new Message(messageContent);
            message.setMessageId(UUID.randomUUID().toString());
            messages.add(message);
        }
    }

    @Override
    public int runBatch() {
        try {
            sender.sendBatch(messages);
        } catch (InterruptedException | ServiceBusException e) {
            throw new RuntimeException(e);
        }
        return messages.size();
    }

    @Override
    public Mono<Integer> runBatchAsync() {
        return Mono.fromFuture(sender.sendBatchAsync(messages)).thenReturn(messages.size());
    }
}
