// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Test for ProcessorReceiveAndLockMessageTest.
 */
public class ProcessorReceiveAndLockMessageTest extends ServiceTest<ServiceBusStressOptions> {

    private static final ClientLogger LOGGER = new ClientLogger(ProcessorReceiveAndLockMessageTest.class);
    private final ServiceBusStressOptions options;
    private final String messageContent;
    private final Duration testDuration;
    final AtomicInteger messagesReceived = new AtomicInteger();

    private final Consumer<ServiceBusErrorContext> processError = errorContext -> {
        LOGGER.verbose("Error occurred while receiving message: " + errorContext.getException());
    };
    private final Consumer<ServiceBusReceivedMessageContext> processMessage = message -> {
        messagesReceived.incrementAndGet();
    };

    private ServiceBusProcessorClient processorClient;


    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public ProcessorReceiveAndLockMessageTest(ServiceBusStressOptions options) {
        super(options, ServiceBusReceiveMode.PEEK_LOCK);
        this.options = options;
        this.messageContent = TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend());
        this.testDuration = Duration.ofSeconds(options.getDuration() - 1);
    }

    @Override
    public Mono<Void> setupAsync() {
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        return Mono.defer(() -> {
            int total = options.getMessagesToSend() * TOTAL_MESSAGE_MULTIPLIER;

            List<ServiceBusMessage> messages = new ArrayList<>();
            for (int i = 0; i < total; ++i) {
                ServiceBusMessage message = new ServiceBusMessage(messageContent);
                message.setMessageId(UUID.randomUUID().toString());
                messages.add(message);
            }
            return senderAsync.sendMessages(messages);
        });
    }

    @Override
    public void run() {
        AtomicReference<CountDownLatch> countdownLatch = new AtomicReference<>();
        countdownLatch.set(new CountDownLatch(1));
        processorClient.start();
    }

    @Override
    public Mono<Void> runAsync() {
        final Mono<ServiceBusProcessorClient> processorClientMono = Mono.defer(() -> Mono.just(
            processorClient = baseBuilder
                .processor()
                .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                .processMessage(processMessage)
                .processError(processError)
                .queueName(queueName)
                .buildProcessorClient()));

        return processorClientMono
            .flatMap(serviceBusProcessorClient -> {
                serviceBusProcessorClient.start();
                return Mono.delay(testDuration)
                    .map(aLong -> {
                        LOGGER.verbose("Processor Client stop and close.");
                        serviceBusProcessorClient.stop();
                        serviceBusProcessorClient.close();
                        updateResults(this.getClass().getName(), messagesReceived.get(), testDuration);
                        return aLong;
                    }).then();
            });
    }
}
