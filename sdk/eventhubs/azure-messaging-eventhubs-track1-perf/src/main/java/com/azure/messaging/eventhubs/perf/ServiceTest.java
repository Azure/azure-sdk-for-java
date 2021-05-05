package com.azure.messaging.eventhubs.perf;

import com.azure.perf.test.core.PerfStressTest;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;

abstract class ServiceTest extends PerfStressTest<EventHubsOptions> {
    private static final String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "AZURE_EVENTHUBS_CONNECTION_STRING";

    private static final String AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME = "AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME";
    private static final String AZURE_EVENTHUBS_EVENT_HUB_NAME = "AZURE_EVENTHUBS_EVENT_HUB_NAME";
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
     * Gets the name of the Event Hub to connect to.
     *
     * @return The name of the Event Hub to connect to.
     */
    String getEventHubName() {
        return System.getenv(AZURE_EVENTHUBS_EVENT_HUB_NAME);
    }

    /**
     * Gets the name of the fully qualified domain name for the Event Hubs namespace.
     *
     * @return The name of the fully qualified domain name for the Event Hubs namespace.
     */
    String getFullyQualifiedDomainName() {
        return System.getenv(AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME);
    }

    /**
     * Gets the Event Hubs namespace connection string.
     *
     * @return The Event Hubs namespace connection string.
     */
    String getConnectionString() {
        return System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME);
    }

    /**
     * Creates a new instance of {@link EventHubClient}.
     *
     * @return A Mono that completes with an {@link EventHubClient}.
     */
    Mono<EventHubClient> createEventHubClientAsync() {
        final ConnectionStringBuilder builder = new ConnectionStringBuilder(getConnectionString())
            .setEventHubName(getEventHubName());

        if (options.getTransportType() != null) {
            builder.setTransportType(options.getTransportType());
        }

        final CompletableFuture<EventHubClient> client;
        try {
            client = EventHubClient.createFromConnectionString(builder.toString(),
                scheduler);
        } catch (IOException e) {
            return Mono.error(new UncheckedIOException("Unable to create EventHubClient.", e));
        }

        return Mono.fromCompletionStage(client);
    }

    /**
     * Creates a new instance of {@link EventHubClient}.
     *
     * @return An {@link EventHubClient}.
     */
    EventHubClient createEventHubClient() {
        final ConnectionStringBuilder builder = new ConnectionStringBuilder(getConnectionString())
            .setEventHubName(getEventHubName());

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
