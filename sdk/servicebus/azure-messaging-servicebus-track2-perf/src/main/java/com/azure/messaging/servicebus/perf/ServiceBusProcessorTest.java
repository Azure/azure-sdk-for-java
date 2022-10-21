package com.azure.messaging.servicebus.perf;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.*;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.perf.test.core.EventPerfTest;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessorTest extends EventPerfTest<ServiceBusStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(ProcessorTest.class);

    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";

    private final String CONNECTION_STRING;
    private final String QUEUE_NAME;

    private final AtomicInteger total = new AtomicInteger();

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public ProcessorTest(ServiceBusStressOptions options) {
        super(options);

        CONNECTION_STRING = System.getenv(AZURE_SERVICE_BUS_CONNECTION_STRING);
        if (CoreUtils.isNullOrEmpty(CONNECTION_STRING)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("Environment variable %s must be set", AZURE_SERVICE_BUS_CONNECTION_STRING)));
        }

        QUEUE_NAME = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
        if (CoreUtils.isNullOrEmpty(QUEUE_NAME)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("Environment variable %s must be set", AZURE_SERVICEBUS_QUEUE_NAME)));
        }

    }

    @Override
    public Mono<Void> setupAsync() {
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .connectionString(CONNECTION_STRING)
            .sender()
            .queueName(QUEUE_NAME)
            .buildClient();

        for (int i = 0; i < options.getMessagesToSend(); i++) {
            ServiceBusMessage message = new ServiceBusMessage(
                TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend()));
            message.setMessageId(UUID.randomUUID().toString());
            sender.sendMessage(message);
        }
        return Mono.empty();
    }


    @Override
    public void runAll(long endNanoTime) {
        runAllAsync(endNanoTime).block();
    }

    @Override
    public Mono<Void> runAllAsync(long endNanoTime) {
        return Mono.usingWhen(Mono.fromCallable(() -> {
            final ServiceBusProcessorClient processor = new ServiceBusClientBuilder()
                .connectionString(CONNECTION_STRING)
                .processor()
                .queueName(QUEUE_NAME)
                .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                .maxConcurrentCalls(options.getMaxConcurrentCalls())
                .processMessage(messageContext -> {
                    total.incrementAndGet();
                })
                .processError(errorContext -> {
                    LOGGER.error("Process message error: {}", errorContext.getException());
                })
                .buildProcessorClient();

            return processor;
        }), processor -> {
            processor.start();
            return Mono.delay(Duration.ofNanos(endNanoTime - System.nanoTime())).then();
        }, processor -> Mono.delay(Duration.ofMillis(500), Schedulers.boundedElastic())
            .then(Mono.fromRunnable(processor::stop)));

    }


    @Override
    public Mono<Void> cleanupAsync() {
        LOGGER.info("Total messages: {}", total.get());
        return Mono.empty();
    }
}
