// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.microsoft.azure.eventhubs.PartitionRuntimeInformation;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutionException;

/**
 * Gets partition information.
 */
public class GetPartitionInformationTest extends ServiceTest {
    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public GetPartitionInformationTest(EventHubsOptions options) {
        super(options);

        if (options.getPartitionId() == null) {
            throw new RuntimeException("options.getPartitionId() cannot be null");
        }
    }

    @Override
    public void run() {
        if (client == null) {
            client = createEventHubClient();
        }

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
        if (clientFuture == null) {
            clientFuture = createEventHubClientAsync();
        }

        if (options.getPartitionId() == null) {
            throw new RuntimeException("options.getPartitionId() cannot be null");
        }

        return Mono.fromCompletionStage(clientFuture
            .thenComposeAsync(client -> client.getPartitionRuntimeInformation(options.getPartitionId()))
            .thenAccept(information -> printRuntimeInformation(information)));
    }

    private static void printRuntimeInformation(PartitionRuntimeInformation information) {
        System.out.printf("Id: %s. Last Seq: %s. Last Offset: %s. Last Enqueued: %s%n",
            information.getPartitionId(),
            information.getLastEnqueuedSequenceNumber(),
            information.getLastEnqueuedOffset(),
            information.getLastEnqueuedTimeUtc());
    }
}
