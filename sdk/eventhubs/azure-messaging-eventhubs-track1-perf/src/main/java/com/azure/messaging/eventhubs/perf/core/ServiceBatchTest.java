// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.BatchPerfTest;
import com.azure.perf.test.core.PerfStressOptions;
import com.microsoft.azure.eventhubs.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class ServiceBatchTest<TOptions extends PerfStressOptions> extends BatchPerfTest<TOptions> {

    private final String eventHubName;
    private final String connectionString;
    private final String poolSize;
    protected EventHubClient eventHubClient;
    private ScheduledExecutorService executor;
    protected final List<EventData> events;


    public ServiceBatchTest(TOptions options) {
        super(options);
        connectionString = System.getenv("EVENTHUBS_CONNECTION_STRING");
        eventHubName = System.getenv("EVENTHUB_NAME");
        poolSize = System.getenv("EVENTHUB_POOL_SIZE");

        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalStateException("Environment variable EVENTHUBS_CONNECTION_STRING must be set");
        }

        if (CoreUtils.isNullOrEmpty(eventHubName)) {
            System.out.println("Environment variable EVENTHUB_NAME must be set");
            System.exit(1);
        }
//        executor = Executors
//            .newScheduledThreadPool(poolSize != null ? Integer.valueOf(poolSize) : 4);
//        eventHubClient = EventHubClient.createSync(
//            new ConnectionStringBuilder(connectionString).setEventHubName(eventHubName).toString(), executor);

        byte[] eventBytes = generateString(100).getBytes(StandardCharsets.UTF_8);
        final ArrayList<EventData> eventsList = new ArrayList<>();
        for (int number = 0; number < options.getCount(); number++) {
            final EventData eventData = EventData.create(eventBytes);
            eventData.getProperties().put("index", number);
            eventsList.add(eventData);
        }
        this.events = Collections.unmodifiableList(eventsList);
    }

    @Override
    public Mono<Void> setupAsync() {
        return Mono.fromCallable(() -> {
            executor = Executors
                .newScheduledThreadPool(poolSize != null ? Integer.valueOf(poolSize) : 4);
            eventHubClient = EventHubClient.createFromConnectionStringSync(
                new ConnectionStringBuilder(connectionString).setEventHubName(eventHubName).toString(), executor);
            return 1;
        }).then();
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.fromCallable(() -> {
            eventHubClient.close();
            executor.shutdownNow();
            return 1;
        }).then();
    }

    protected String generateString(int targetLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
            .limit(targetLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
        return generatedString;
    }

    /**
     * Sends the number of messages to {@code partitionId}.
     *
     * @param client Client used to send message.
     * @param partitionId Destination partition id.
     * @param totalMessagesToSend Number of messages to send.
     *
     * @return A Mono that completes when all messages are sent.
     *
     * @throws RuntimeException if the partition sender could not be created. Or an exception occurred while sending
     *     the messages.
     */
    Mono<Void> sendMessages(EventHubClient client, String partitionId, int totalMessagesToSend) {
        CompletableFuture<PartitionSender> createSenderFuture;
        try {
            createSenderFuture = client.createPartitionSender(partitionId);
        } catch (EventHubException e) {
            createSenderFuture = new CompletableFuture<>();
            createSenderFuture.completeExceptionally(
                new RuntimeException("Unable to create partition sender: " + partitionId, e));
        }

        return Mono.usingWhen(
            Mono.fromCompletionStage(createSenderFuture),
            sender -> {
                EventDataBatch currentBatch;

                int numberOfMessages = totalMessagesToSend;
                while (numberOfMessages > 0) {
                    currentBatch = sender.createBatch();
                    addEvents(currentBatch, numberOfMessages);
                    try {
                        sender.sendSync(currentBatch);
                        numberOfMessages = numberOfMessages - currentBatch.getSize();
                    } catch (EventHubException e) {
                        System.err.println("Could not send batch. Error: " + e);
                    }
                }

                System.out.printf("%s: Sent %d messages.%n", partitionId, totalMessagesToSend);
                return Mono.empty();
            },
            sender -> Mono.fromCompletionStage(sender.close()));
    }

    /**
     * Adds the number of messages to the batch. The size of the message is set using {@link
     * PerfStressOptions#getSize()}.
     *
     * @param batch The batch to add messages to.
     * @param numberOfMessages Number of messages to add.
     */
    void addEvents(EventDataBatch batch, int numberOfMessages) {
        for (int i = 0; i < numberOfMessages; i++) {
            final int index = numberOfMessages % events.size();
            final EventData event = events.get(index);

            try {
                if (!batch.tryAdd(event)) {
                    System.out.printf("Only added %s of %s events.%n", i, numberOfMessages);
                    break;
                }
            } catch (PayloadSizeExceededException e) {
                throw new RuntimeException("Event was too large for a single batch.", e);
            }
        }
    }
}
