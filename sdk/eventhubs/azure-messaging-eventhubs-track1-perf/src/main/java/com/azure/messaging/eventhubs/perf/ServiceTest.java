package com.azure.messaging.eventhubs.perf;

import com.azure.perf.test.core.PerfStressTest;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;

abstract class ServiceTest extends PerfStressTest<EventHubsOptions> {
    private final ScheduledExecutorService scheduler;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    ServiceTest(EventHubsOptions options) {
        super(options);

        this.scheduler = Executors.newScheduledThreadPool(DEFAULT_BOUNDED_ELASTIC_SIZE);
    }

    /**
     * Creates a new instance of {@link EventHubClient}.
     *
     * @return A Mono that completes with an {@link EventHubClient}.
     */
    Mono<EventHubClient> createEventHubClientAsync() {
        final ConnectionStringBuilder builder = new ConnectionStringBuilder(options.getConnectionString())
            .setEventHubName(options.getEventHubName());

        if (options.getTransportType() != null) {
            builder.setTransportType(options.getTransportType());
        }

        try {
            return Mono.fromCompletionStage(EventHubClient.createFromConnectionString(
                builder.toString(), scheduler));
        } catch (IOException e) {
            return Mono.error(new UncheckedIOException("Unable to create EventHubClient.", e));
        }
    }

    /**
     * Creates a new instance of {@link EventHubClient}.
     *
     * @return An {@link EventHubClient}.
     */
    EventHubClient createEventHubClient() {
        final ConnectionStringBuilder builder = new ConnectionStringBuilder(options.getConnectionString())
            .setEventHubName(options.getEventHubName());

        if (options.getTransportType() != null) {
            builder.setTransportType(options.getTransportType());
        }

        try {
            return EventHubClient.createFromConnectionStringSync(builder.toString(),
                scheduler);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to create EventHubClient.", e);
        } catch (EventHubException e) {
            throw new RuntimeException("Unable to create EventHubClient due to EventHubException.", e);
        }
    }

    @Override
    public Mono<Void> cleanupAsync() {
        this.scheduler.shutdown();
        return super.cleanupAsync();
    }
}
