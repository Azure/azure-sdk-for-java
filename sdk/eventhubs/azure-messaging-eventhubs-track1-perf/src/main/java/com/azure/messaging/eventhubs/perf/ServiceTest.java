package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.perf.models.EventHubsOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventDataBatch;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PayloadSizeExceededException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;

/**
 * Base class that tests Event Hubs.
 */
abstract class ServiceTest extends PerfStressTest<EventHubsOptions> {
    private final ScheduledExecutorService scheduler;

    protected final List<EventData> events;
    protected CompletableFuture<EventHubClient> clientFuture;
    protected EventHubClient client;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    ServiceTest(EventHubsOptions options) {
        super(options);

        final InputStream randomInputStream = TestDataCreationHelper.createRandomInputStream(options.getSize());
        byte[] eventBytes;
        try {
            eventBytes = randomInputStream.readAllBytes();
        } catch (IOException e) {
            System.err.println("Unable to read input bytes." + e);
            final int size = Long.valueOf(options.getSize()).intValue();
            eventBytes = "a".repeat(size).getBytes(StandardCharsets.UTF_8);
        }

        final ArrayList<EventData> eventsList = new ArrayList<>();
        for (int number = 0; number < options.getCount(); number++) {
            final EventData eventData = EventData.create(eventBytes);
            eventData.getProperties().put("index", number);
            eventsList.add(eventData);
        }

        this.events = Collections.unmodifiableList(eventsList);
        this.scheduler = Executors.newScheduledThreadPool(DEFAULT_BOUNDED_ELASTIC_SIZE);
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
            return CompletableFuture.failedFuture(new UncheckedIOException("Unable to create EventHubClient.", e));
        }
    }

    ConnectionStringBuilder getConnectionStringBuilder() {
        final ConnectionStringBuilder builder = new ConnectionStringBuilder(options.getConnectionString())
            .setEventHubName(options.getEventHubName());

        if (options.getTransportType() != null) {
            builder.setTransportType(options.getTransportType());
        }

        return builder;
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

    EventDataBatch createBatch(EventHubClient client) {
        final EventDataBatch batch;
        try {
            batch = client.createBatch();
        } catch (EventHubException e) {
            throw new RuntimeException("Unable to create EventDataBatch.", e);
        }

        for (int i = 0; i < events.size(); i++) {
            final EventData event = events.get(i);

            try {
                if (!batch.tryAdd(event)) {
                    System.out.printf("Only added %s of %s events.%n", i, events.size());
                    break;
                }
            } catch (PayloadSizeExceededException e) {
                throw new RuntimeException("Event was too large for a single batch.", e);
            }
        }

        return batch;
    }

    @Override
    public Mono<Void> cleanupAsync() {
        if (options.isSync()) {
            try {
                client.closeSync();
                return Mono.empty();
            } catch (EventHubException e) {
                return Mono.error(new RuntimeException("Unable to close synchronous client.", e));
            } finally {
                scheduler.shutdown();
            }
        } else if (clientFuture != null) {
            final CompletableFuture<Void> future = clientFuture
                .thenComposeAsync(client -> client.close())
                .whenComplete((result, error) -> {
                    if (error != null) {
                        System.err.printf("Error closing async client. %s%n", error);
                    }
                    scheduler.shutdown();
                });

            return Mono.fromCompletionStage(future);
        }

        return Mono.empty();
    }
}
