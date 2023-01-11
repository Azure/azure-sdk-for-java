// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.perf.test.core.BatchPerfTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents the EventHubs Service Test.
 * @param <TOptions> the options bag to use for running performance tests.
 */
public abstract class ServiceBatchTest<TOptions extends PerfStressOptions> extends BatchPerfTest<TOptions> {
    protected final String connectionString;
    protected final String eventHubName;
    protected EventHubClientBuilder eventHubClientBuilder;
    protected EventHubProducerAsyncClient eventHubProducerAsyncClient;
    protected EventHubProducerClient eventHubProducerClient;
    protected final List<EventData> events;


    /**
     * Instantiates instance of the Service Test.
     * @param options The options bag to use to run performance test.
     * @throws IllegalStateException when expected configuration of environment variables is not found.
     */
    public ServiceBatchTest(TOptions options) throws IllegalStateException {
        super(options);
        connectionString = System.getenv("EVENTHUBS_CONNECTION_STRING");
        eventHubName = System.getenv("EVENTHUB_NAME");

        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalStateException("Environment variable EVENTHUBS_CONNECTION_STRING must be set");
        }

        if (CoreUtils.isNullOrEmpty(eventHubName)) {
            throw new IllegalStateException("Environment variable EVENTHUB_NAME must be set");
        }

        byte[] eventBytes = generateString(100).getBytes(StandardCharsets.UTF_8);

        final ArrayList<EventData> eventsList = new ArrayList<>();
        for (int number = 0; number < options.getCount(); number++) {
            final EventData eventData = new EventData(eventBytes);
            eventData.getProperties().put("index", number);
            eventsList.add(eventData);
        }
        this.events = Collections.unmodifiableList(eventsList);
    }

    @Override
    public Mono<Void> setupAsync() {
        return Mono.fromCallable(() -> {
            // Setup the service client
            eventHubClientBuilder = new EventHubClientBuilder().connectionString(connectionString, eventHubName);
            eventHubProducerAsyncClient = eventHubClientBuilder.buildAsyncProducerClient();
            eventHubProducerClient = eventHubClientBuilder.buildProducerClient();
            return 1;
        }).then();
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.fromCallable(() -> {
            eventHubProducerAsyncClient.close();
            eventHubProducerClient.close();
            return 1;
        }).then();
    }

    Mono<Void> sendMessages(EventHubProducerAsyncClient client, String partitionId, int totalMessagesToSend) {
        final CreateBatchOptions options = partitionId != null
            ? new CreateBatchOptions().setPartitionId(partitionId)
            : new CreateBatchOptions();

        final AtomicInteger number = new AtomicInteger(totalMessagesToSend);
        return Mono.defer(() -> client.createBatch(options)
                .flatMap(batch -> {
                    EventData event = events.get(0);
                    while (batch.tryAdd(event)) {
                        final int index = number.getAndDecrement() % events.size();
                        if (index < 0) {
                            break;
                        }

                        event = events.get(index);
                    }

                    return client.send(batch);
                }))
            .repeat(() -> number.get() > 0)
            .then()
            .doFinally(signal ->
                System.out.printf("%s: Sent %d messages.%n", partitionId, totalMessagesToSend));
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
}
