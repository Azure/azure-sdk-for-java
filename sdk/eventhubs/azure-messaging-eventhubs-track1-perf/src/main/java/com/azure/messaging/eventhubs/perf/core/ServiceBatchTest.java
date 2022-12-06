// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.BatchPerfTest;
import com.azure.perf.test.core.PerfStressOptions;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class ServiceBatchTest<TOptions extends PerfStressOptions> extends BatchPerfTest<TOptions> {

    private final String eventHubName;
    private final String connectionString;
    private final String poolSize;
    protected EventHubClient eventHubClient;
    private ScheduledExecutorService executor;


    public ServiceBatchTest(TOptions options) throws IOException, EventHubException {
        super(options);
        connectionString = System.getenv("EVENTHUBS_CONNECTION_STRING");
        eventHubName = System.getenv("EVENTHUB_NAME");
        poolSize = System.getenv("EVENTHUB_POOL_SIZE");

        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalStateException("Environment variable EVENTHUBS_CONNECTION_STRING must be set");
        }

        if (CoreUtils.isNullOrEmpty(eventHubName)) {
            System.out.println("Environment variable EVENTHUB_NAME must be set");
            System.exit(1);
        }
//        executor = Executors
//            .newScheduledThreadPool(poolSize != null ? Integer.valueOf(poolSize) : 4);
//        eventHubClient = EventHubClient.createSync(
//            new ConnectionStringBuilder(connectionString).setEventHubName(eventHubName).toString(), executor);

    }

    @Override
    public Mono<Void> setupAsync() {
        return Mono.fromCallable(() -> {
            executor = Executors
                .newScheduledThreadPool(poolSize != null ? Integer.valueOf(poolSize) : 4);
            eventHubClient = EventHubClient.createFromConnectionStringSync(
                new ConnectionStringBuilder(connectionString).setEventHubName(eventHubName).toString(), executor);
            return 1;
        }).then();
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.fromCallable(() -> {
            eventHubClient.close();
            executor.shutdownNow();
            return 1;
        }).then();
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
