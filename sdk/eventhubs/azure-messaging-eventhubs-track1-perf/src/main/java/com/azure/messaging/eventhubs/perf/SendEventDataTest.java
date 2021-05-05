// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import io.netty.util.concurrent.CompleteFuture;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Sends a number of {@link EventData} to Event Hub.
 */
public class SendEventDataTest extends ServiceTest {
    private final ArrayList<EventData> events = new ArrayList<>();

    private Mono<EventHubClient> clientMono;
    private EventHubClient client;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public SendEventDataTest(EventHubsOptions options) {
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

        for (int number = 0; number < options.getCount(); number++) {
            final EventData eventData = EventData.create(eventBytes);
            eventData.getProperties().put("index", number);
            events.add(eventData);
        }
    }

    @Override
    public Mono<Void> setupAsync() {
        final Mono<EventHubClient> createClientMono;
        if (options.isSync()) {
            client = createEventHubClient();
            createClientMono = Mono.empty();
        } else {
            clientMono = createEventHubClientAsync().cache(e -> Duration.ofMillis(Long.MAX_VALUE),
                error -> Duration.ZERO,
                () -> Duration.ZERO);

            createClientMono = clientMono;
        }

        return createClientMono.then();
    }

    @Override
    public Mono<Void> cleanupAsync() {
        if (options.isSync()) {
            try {
                client.closeSync();
                return Mono.empty();
            } catch (EventHubException e) {
                return Mono.whenDelayError(
                    super.cleanupAsync(),
                    Mono.error(new RuntimeException("Unable to close synchronous client.", e)));
            }
        } else {
            return Mono.whenDelayError(super.cleanupAsync(), clientMono);
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < events.size(); i++) {
            final EventData event = events.get(i);

            try {
                client.sendSync(event);
            } catch (EventHubException e) {
                throw new RuntimeException("Unable to send event at index: " + i, e);
            }
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return clientMono.flatMap(client -> {
            final CompletableFuture<?>[] completableFutures = events.stream()
                .map(client::send)
                .toArray(CompletableFuture<?>[]::new);
            return Mono.fromCompletionStage(CompletableFuture.allOf(completableFutures));
        });
    }
}
