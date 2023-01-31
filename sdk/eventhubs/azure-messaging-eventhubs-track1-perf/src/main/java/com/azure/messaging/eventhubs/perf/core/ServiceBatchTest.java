// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.BatchPerfTest;
import com.azure.perf.test.core.PerfStressOptions;
import com.microsoft.azure.eventhubs.*;
import reactor.core.publisher.Flux;
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
import java.util.concurrent.atomic.AtomicLong;

public abstract class ServiceBatchTest<TOptions extends PerfStressOptions> extends BatchPerfTest<TOptions> {

    private final String eventHubName;
    private final String connectionString;
    private final String poolSize;
    protected EventHubClient eventHubClient;
    private ScheduledExecutorService executor;
    protected final List<EventData> events;
    protected byte[] eventDataBytes;


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

        executor = Executors
            .newScheduledThreadPool(poolSize != null ? Integer.valueOf(poolSize) : 4);
        try {
            eventHubClient = EventHubClient.createFromConnectionStringSync(
                new ConnectionStringBuilder(connectionString).setEventHubName(eventHubName).toString(), executor);
        } catch (EventHubException | IOException e) {
            throw new RuntimeException("Error creating EventHub client.", e);
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
        eventDataBytes = generateString(100).getBytes(StandardCharsets.UTF_8);

    }

    @Override
    public Mono<Void> setupAsync() {
        return Mono.empty();
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

    Mono<Void> preLoadEvents(EventHubClient client, String partitionId, int totalMessagesToSend) {
        final AtomicLong eventsToSend = new AtomicLong(totalMessagesToSend);
        final AtomicLong totalEvents = new AtomicLong(0);

        Mono<Void> partitionMono;
        if (CoreUtils.isNullOrEmpty(partitionId)) {
            partitionMono = Mono.fromFuture(client.getRuntimeInformation())
                .flatMap(eventHubRuntimeInformation -> {
                    String[] partitionIds = eventHubRuntimeInformation.getPartitionIds();
                    return Flux.fromArray(partitionIds)
                        .map(partId -> Mono.fromFuture(client.getPartitionRuntimeInformation(partId))
                            .map(partitionRuntimeInformation -> {
                                totalEvents.addAndGet(partitionRuntimeInformation.getLastEnqueuedSequenceNumber() - partitionRuntimeInformation.getBeginSequenceNumber());
                                return Mono.empty();
                            })).then();
                }).then();
        } else {
            partitionMono = Mono.fromFuture(client.getPartitionRuntimeInformation(partitionId))
                .map(partitionProperties -> {
                    totalEvents.addAndGet(partitionProperties.getLastEnqueuedSequenceNumber() - partitionProperties.getBeginSequenceNumber());
                    return Mono.empty();
                }).then();
        }

        if (!CoreUtils.isNullOrEmpty(partitionId)) {
            CompletableFuture<PartitionSender> createSenderFuture;
            try {
                createSenderFuture = client.createPartitionSender(partitionId);
            } catch (EventHubException e) {
                createSenderFuture = new CompletableFuture<>();
                createSenderFuture.completeExceptionally(
                    new RuntimeException("Unable to create partition sender: " + partitionId, e));
            }

            return partitionMono.then(Mono.usingWhen(
                Mono.fromCompletionStage(createSenderFuture),
                sender -> {
                    EventDataBatch currentBatch;

                    if (totalEvents.get() < eventsToSend.get()) {
                        eventsToSend.set(eventsToSend.get() - totalEvents.get());
                        while (eventsToSend.get() > 0) {
                            currentBatch = sender.createBatch();

                            EventData event = createEvent();
                            try {
                                while (currentBatch.tryAdd(event)) {
                                    eventsToSend.getAndDecrement();
                                }
                            } catch (PayloadSizeExceededException e) {
                                throw new RuntimeException("Event was too large for a single batch.", e);
                            }
                            try {
                                sender.sendSync(currentBatch);
                            } catch (EventHubException e) {
                                throw new RuntimeException("Could not send batch. Error: " + e);
                            }
                        }
                        System.out.printf("%s: Sent %d messages.%n", partitionId, totalMessagesToSend);
                        return Mono.empty();
                    } else {
                        return Mono.empty();
                    }
                },
                sender -> Mono.fromCompletionStage(sender.close())));
        } else {
            return partitionMono.then(Mono.defer(() -> {
                EventDataBatch currentBatch;

                if (totalEvents.get() < eventsToSend.get()) {
                    eventsToSend.set(eventsToSend.get() - totalEvents.get());
                    while (eventsToSend.get() > 0) {
                        try {
                            currentBatch = client.createBatch();
                        } catch (EventHubException e) {
                            throw new RuntimeException("Error creating Batch", e);
                        }

                        EventData event = createEvent();
                        try {
                            while (currentBatch.tryAdd(event)) {
                                eventsToSend.getAndDecrement();
                            }
                        } catch (PayloadSizeExceededException e) {
                            throw new RuntimeException("Event was too large for a single batch.", e);
                        }
                        try {
                            client.sendSync(currentBatch);
                        } catch (EventHubException e) {
                            throw new RuntimeException("Could not send batch. Error: " + e);
                        }
                    }
                    System.out.printf("%s: Sent %d messages.%n", partitionId, totalMessagesToSend);
                    return Mono.empty();
                } else {
                    return Mono.empty();
                }
            }));
        }
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

    protected EventData createEvent() {
        EventData eventData = EventData.create(eventDataBytes);
        return eventData;
    }
}
