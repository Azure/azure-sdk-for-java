// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.PartitionProperties;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Gets partition information.
 */
public class GetPartitionInformationTest extends ServiceTest<EventHubsPartitionOptions> {
    private EventHubProducerClient client;
    private EventHubProducerAsyncClient asyncClient;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public GetPartitionInformationTest(EventHubsPartitionOptions options) {
        super(options);
    }

    @Override
    public void run() {
        if (client == null) {
            client = createEventHubClientBuilder().buildProducerClient();
        }

        final PartitionProperties information = client.getPartitionProperties(options.getPartitionId());
        printRuntimeInformation(information);
    }

    @Override
    public Mono<Void> runAsync() {
        if (asyncClient == null) {
            asyncClient = createEventHubClientBuilder().buildAsyncProducerClient();
        }

        return asyncClient.getPartitionProperties(options.getPartitionId())
            .map(information -> {
                printRuntimeInformation(information);
                return information;
            })
            .then();
    }

    @Override
    public Mono<Void> cleanupAsync() {
        if (options.isSync() && client != null) {
            return Mono.fromRunnable(() -> client.close());
        } else if (asyncClient != null) {
            return Mono.fromRunnable(() -> asyncClient.close());
        } else {
            return super.cleanupAsync();
        }
    }

    private static void printRuntimeInformation(PartitionProperties information) {
        Objects.requireNonNull(information, "'PartitionProperties' cannot be null.");
    }
}
