// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventDataBatch;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.PayloadSizeExceededException;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Base class that tests Event Hubs.
 */
abstract class ServiceTest<T extends EventHubsOptions> extends PerfStressTest<T> {
    private final ScheduledExecutorService scheduler;

    protected final List<EventData> events;
    protected CompletableFuture<EventHubClient> clientFuture;
    protected EventHubClient client;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    ServiceTest(T options) {
        super(options);

        final InputStream randomInputStream = TestDataCreationHelper.createRandomInputStream(options.getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] eventBytes;
        try {
            int bytesRead;
            final byte[] data = new byte[4096];

            while ((bytesRead = randomInputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            eventBytes = buffer.toByteArray();
        } catch (IOException e) {
            System.err.println("Unable to read input bytes." + e);
            final int size = Long.valueOf(options.getSize()).intValue();
            eventBytes = new byte[size];
            Arrays.fill(eventBytes, Integer.valueOf(95).byteValue());
        } finally {
            try {
                buffer.close();
            } catch (IOException e) {
                System.err.println("Unable to close bytebuffer. Error:" + e);
            }
        }

        final ArrayList<EventData> eventsList = new ArrayList<>();
        for (int number = 0; number < options.getCount(); number++) {
            final EventData eventData = EventData.create(eventBytes);
            eventData.getProperties().put("index", number);
            eventsList.add(eventData);
        }

        this.events = Collections.unmodifiableList(eventsList);
        this.scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 4);
    }

    /**
     * Gets a new instance of the connection string builder.
     *
     * @return A new instance the connection string builder.
     */
    ConnectionStringBuilder getConnectionStringBuilder() {
        final ConnectionStringBuilder builder = new ConnectionStringBuilder(options.getConnectionString())
            .setEventHubName(options.getEventHubName());

        if (options.getTransportType() != null) {
            builder.setTransportType(options.getTransportType());
        }

        return builder;
    }

    /**
     * Gets the scheduler that the EventHubClient uses.
     *
     * @return The scheduler.
     */
    ScheduledExecutorService getScheduler() {
        return scheduler;
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

    /**
     * Creates a new instance of {@link EventHubClient}.
     *
     * @return An {@link EventHubClient}.
     */
    EventHubClient createEventHubClient() {
        final ConnectionStringBuilder builder = getConnectionStringBuilder();

        try {
            return EventHubClient.createFromConnectionStringSync(builder.toString(),
                scheduler);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to create EventHubClient.", e);
        } catch (EventHubException e) {
            throw new RuntimeException("Unable to create EventHubClient due to EventHubException.", e);
        }
    }

    /**
     * Creates a new instance of {@link EventHubClient}.
     *
     * @return A Mono that completes with an {@link EventHubClient}.
     */
    CompletableFuture<EventHubClient> createEventHubClientAsync() {
        final ConnectionStringBuilder builder = new ConnectionStringBuilder(options.getConnectionString())
            .setEventHubName(options.getEventHubName());

        if (options.getTransportType() != null) {
            builder.setTransportType(options.getTransportType());
        }

        try {
            return EventHubClient.createFromConnectionString(builder.toString(), scheduler);
        } catch (IOException e) {
            final CompletableFuture<EventHubClient> future = new CompletableFuture<>();
            future.completeExceptionally(new UncheckedIOException("Unable to create EventHubClient.", e));

            return future;
        }
    }

    /**
     * Creates a new {@link EventDataBatch} using the given client and adds the number of messages to it.
     *
     * @param client Client used to create batch.
     * @param numberOfMessages Number of messages to add.
     *
     * @return The filled {@link EventDataBatch}.
     *
     * @throws RuntimeException if an {@link EventHubException} was encountered when creating batch. Or, if a {@link
     *     PayloadSizeExceededException} was thrown when adding the event.
     */
    EventDataBatch createEventDataBatch(EventHubClient client, int numberOfMessages) {
        final EventDataBatch batch;
        try {
            batch = client.createBatch();
        } catch (EventHubException e) {
            throw new RuntimeException("Unable to create EventDataBatch.", e);
        }

        addEvents(batch, numberOfMessages);

        return batch;
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
     * Closes the client or clientFuture if they are set.
     */
    @Override
    public Mono<Void> cleanupAsync() {
        if (client != null) {
            try {
                client.closeSync();
                return Mono.empty();
            } catch (EventHubException e) {
                return Mono.error(new RuntimeException("Unable to close synchronous client.", e));
            }
        }

        if (clientFuture != null) {
            final CompletableFuture<Void> future = clientFuture.thenComposeAsync(client -> client.close());
            return Mono.fromCompletionStage(future);
        }

        return Mono.empty();
    }

    /**
     * Shutdown the scheduler.
     *
     * @return A Mono that completes.
     */
    @Override
    public Mono<Void> globalCleanupAsync() {
        scheduler.shutdown();
        return Mono.empty();
    }
}
