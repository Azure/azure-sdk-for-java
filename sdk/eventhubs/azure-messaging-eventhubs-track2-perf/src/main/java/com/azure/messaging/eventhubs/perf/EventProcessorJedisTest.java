// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.perf;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.jedis.JedisRedisCheckpointStore;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisPool;
import com.azure.perf.test.core.EventPerfTest;

import java.time.Duration;
import java.util.function.Consumer;


public class EventProcessorJedisTest extends EventPerfTest<EventProcessorJedisOptions> {

    private EventProcessorClient eventProcessorClient;
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

        Consumer<ErrorContext> errorProcessor = errorContext -> super.errorRaised(errorContext.getThrowable());
        Consumer<EventContext> eventProcessor = eventContext -> {
            super.eventRaised();
            eventContext.updateCheckpoint();
        };
        eventProcessorClient = new EventProcessorClientBuilder()
            .connectionString(options.getConnectionString(), options.getEventHubName())
            .consumerGroup(options.getConsumerGroup())
            .checkpointStore(checkpointStore)
            .processError(errorProcessor)
            .processEvent(eventProcessor)
            .buildEventProcessorClient();
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then( Mono.defer(() -> {
            eventProcessorClient.start();
            return Mono.empty();
            }));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.defer(() -> {
            eventProcessorClient.stop();
            return Mono.empty();
        }).then(super.cleanupAsync());
    }
}
