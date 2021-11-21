// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventhubs.PartitionRuntimeInformation;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Gets partition information.
 */
public class GetPartitionInformationTest extends ServiceTest<EventHubsPartitionOptions> {
    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public GetPartitionInformationTest(EventHubsPartitionOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> setupAsync() {
        if (options.isSync() && client == null) {
            client = createEventHubClient();
        } else if (!options.isSync() && clientFuture == null) {
            clientFuture = createEventHubClientAsync();
        }

        return super.setupAsync();
    }

    @Override
    public void run() {
        PartitionRuntimeInformation information;
        try {
            information = client.getPartitionRuntimeInformation(options.getPartitionId()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Unable to get partition information for: " + options.getPartitionId(), e);
        }

        printRuntimeInformation(information);
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.fromCompletionStage(clientFuture
            .thenComposeAsync(client -> client.getPartitionRuntimeInformation(options.getPartitionId()))
            .thenAccept(information -> printRuntimeInformation(information)));
    }

    private static void printRuntimeInformation(PartitionRuntimeInformation information) {
        Objects.requireNonNull(information, "'PartitionRuntimeInformation' cannot be null.");
    }
}
