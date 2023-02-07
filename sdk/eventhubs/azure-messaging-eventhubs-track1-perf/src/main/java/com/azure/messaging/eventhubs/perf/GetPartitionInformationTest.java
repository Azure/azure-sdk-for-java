// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.perf.test.core.EventPerfTest;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionRuntimeInformation;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gets partition information.
 */
public class GetPartitionInformationTest extends EventPerfTest<EventHubsPartitionOptions> {
    private final EventHubsTestHelper<EventHubsPartitionOptions> testHelper;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private EventHubClient client;
    private CompletableFuture<EventHubClient> clientFuture;
    private Disposable subscription;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public GetPartitionInformationTest(EventHubsPartitionOptions options) {
        super(options);

        this.testHelper = new EventHubsTestHelper<>(options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> setupAsync() {
        return Mono.fromRunnable(() -> {
            if (isRunning.getAndSet(true)) {
                return;
            }

            if (options.isSync() && client == null) {
                this.client = testHelper.createEventHubClient();
                this.subscription = Mono.fromRunnable(() -> getPartitionInformation())
                    .repeat(() -> isRunning.get())
                    .subscribe();
            } else if (!options.isSync() && clientFuture == null) {
                this.clientFuture = testHelper.createEventHubClientAsync();

                this.subscription = Mono.defer(() -> getPartitionInformationAsync())
                    .repeat(() -> isRunning.get())
                    .subscribe();
            }
        });
    }

    @Override
    public Mono<Void> cleanupAsync() {
        if (!isRunning.getAndSet(false)) {
            return Mono.empty();
        }

        subscription.dispose();

        // Dispose of the scheduler at the very end.
        return testHelper.cleanupAsync(client, clientFuture)
            .doFinally(signal -> testHelper.close());
    }

    private void getPartitionInformation() {
        PartitionRuntimeInformation information;
        try {
            information = client.getPartitionRuntimeInformation(options.getPartitionId()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Unable to get partition information for: " + options.getPartitionId(), e);
        }

        printRuntimeInformation(information);
    }

    private Mono<Void> getPartitionInformationAsync() {
        return Mono.fromCompletionStage(clientFuture
            .thenComposeAsync(client -> client.getPartitionRuntimeInformation(options.getPartitionId()))
            .thenAccept(information -> printRuntimeInformation(information)));
    }

    private static void printRuntimeInformation(PartitionRuntimeInformation information) {
        Objects.requireNonNull(information, "'PartitionRuntimeInformation' cannot be null.");
    }
}
