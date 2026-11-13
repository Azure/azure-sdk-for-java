// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.implementation.EventHubConnectionProcessor;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.implementation.EventHubReactorAmqpConnection;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Isolated
@Execution(ExecutionMode.SAME_THREAD)
class EventHubProducerAsyncClientRetryIsolatedTest {
    private static final String HOSTNAME = "my-host-name";
    private static final String EVENT_HUB_NAME = "my-event-hub-name";

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void getPropertiesWithRetries(boolean partition) {
        AmqpRetryOptions retryOptions = new AmqpRetryOptions().setDelay(Duration.ofMillis(200))
            .setMode(AmqpRetryMode.FIXED)
            .setTryTimeout(Duration.ofMillis(100));
        EventHubReactorAmqpConnection connection = mock(EventHubReactorAmqpConnection.class);
        EventHubManagementNode managementNode = mock(EventHubManagementNode.class);
        when(connection.getEndpointStates()).thenReturn(Flux.concat(Flux.just(AmqpEndpointState.ACTIVE), Flux.never()));
        when(connection.closeAsync()).thenReturn(Mono.empty());

        EventHubProperties expectedHub = new EventHubProperties(EVENT_HUB_NAME, Instant.now(), new String[] { "0" });
        PartitionProperties expectedPartition = new PartitionProperties(EVENT_HUB_NAME, "0", 1L, 2L,
            OffsetDateTime.now().toString(), Instant.now(), false);
        when(managementNode.getPartitionProperties(anyString())).thenReturn(Mono.just(expectedPartition));
        when(managementNode.getEventHubProperties()).thenReturn(Mono.just(expectedHub));

        AtomicInteger attempts = new AtomicInteger();
        AtomicBoolean timeoutCancelled = new AtomicBoolean();
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.create();
        AtomicReference<EventHubProducerAsyncClient> client = new AtomicReference<>();
        when(connection.getManagementNode()).thenAnswer(invocation -> {
            int attempt = attempts.getAndIncrement();
            if (attempt == 0) {
                return Mono.error(new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-message",
                    new AmqpErrorContext("test-namespace")));
            } else if (attempt == 1) {
                return Mono.delay(Duration.ofSeconds(100), scheduler)
                    .then(Mono.error(new AssertionError("The timed-out attempt must be cancelled.")))
                    .doOnCancel(() -> timeoutCancelled.set(true));
            }
            return Mono.just(managementNode);
        });

        Supplier<Mono<?>> operation = () -> {
            EventHubConnectionProcessor processor = Mono.fromCallable(() -> connection)
                .repeat(10)
                .subscribeWith(new EventHubConnectionProcessor(HOSTNAME, EVENT_HUB_NAME, retryOptions));
            EventHubProducerAsyncClient producer
                = new EventHubProducerAsyncClient(HOSTNAME, EVENT_HUB_NAME, new ConnectionCacheWrapper(processor),
                    retryOptions, new EventHubMessageSerializer(), scheduler, false, mock(Runnable.class),
                    "test-client", new EventHubsProducerInstrumentation(null, null, HOSTNAME, EVENT_HUB_NAME));
            client.set(producer);
            return partition ? producer.getPartitionProperties("0") : producer.getEventHubProperties();
        };

        try {
            StepVerifier.withVirtualTime(operation, () -> scheduler, 1)
                .expectSubscription()
                .thenAwait(Duration.ofSeconds(15))
                .assertNext(properties -> assertSame(partition ? expectedPartition : expectedHub, properties))
                .expectComplete()
                .verify(Duration.ofSeconds(5));
            assertEquals(3, attempts.get());
            assertTrue(timeoutCancelled.get(), "The second attempt must time out before the third succeeds");
        } finally {
            if (client.get() != null) {
                client.get().close();
            }
            scheduler.dispose();
        }
    }
}
