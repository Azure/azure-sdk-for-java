// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.jedis.JedisRedisCheckpointStore;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.perf.test.core.MockErrorContext;
import com.azure.perf.test.core.MockEventContext;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisPool;
import com.azure.perf.test.core.EventPerfTest;

import java.time.Duration;
import java.util.function.Consumer;


public class EventProcessorJedisTest extends EventPerfTest<EventProcessorJedisOptions> {

    private EventProcessorClientBuilder builder;

    //TODO 1: Add expected behavior to all consumer methods

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public EventProcessorJedisTest(EventProcessorJedisOptions options) {
        super(options);
        Duration errorAfter = options.getErrorAfterInSeconds() > 0
            ? Duration.ofSeconds(options.getErrorAfterInSeconds()) : null;

        JedisPool jedisPool = new JedisPool(options.getHostName(), 6380, options.getUserName(), options.getPassword());
        JedisRedisCheckpointStore checkpointStore = new JedisRedisCheckpointStore(jedisPool);

        Consumer<ErrorContext> errorProcessor = errorContext -> {
            System.out.println("Placeholder for processing errors.");
        };
        Consumer<EventContext> eventProcessor = eventContext -> {
            System.out.println("Placeholder for processing events.");
        };

        builder = new EventProcessorClientBuilder()
            .connectionString(options.getConnectionString(), options.getEventHubName())
            .consumerGroup(options.getConsumerGroup())
            .checkpointStore(checkpointStore)
            .processError(errorProcessor)
            .processEvent(eventProcessor);

    }

    //TODO 2: setUpAsync

    //TODO 3: cleanUpAsync

}
