// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubConnectionProcessor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.when;

class EventHubAsyncClientTest {
    private static final String NAMESPACE = "test-namespace.eventhubs.com";
    private static final String EVENT_HUB_NAME = "test-event-hub-name";
    private static final AmqpRetryOptions AMQP_RETRY_OPTIONS = new AmqpRetryOptions().setMaxRetries(2)
        .setDelay(Duration.ofSeconds(10));

    @Mock
    private EventHubAmqpConnection connection;
    @Mock
    private EventHubAmqpConnection connection2;
    @Mock
    private EventHubAmqpConnection connection3;

    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final FluxSink<AmqpEndpointState> endpointProcessorSink = endpointProcessor.sink();
    private final DirectProcessor<AmqpShutdownSignal> shutdownSignalProcessor = DirectProcessor.create();
    private final EventHubConnectionProcessor eventHubConnectionProcessor = new EventHubConnectionProcessor(NAMESPACE,
        EVENT_HUB_NAME, AMQP_RETRY_OPTIONS);

    private final TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());

    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private AmqpReceiveLink receiveLink;
    @Mock
    private AmqpSendLink sendLink;


    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
    }

    @AfterEach
    void teardown() {
        eventHubConnectionProcessor.dispose();
        shutdownSignalProcessor.dispose();
        endpointProcessor.dispose();
    }


    @Test
    void getsPartitionId() {
        endpointProcessorSink.next(AmqpEndpointState.ACTIVE);
    }

    private static Flux<EventHubAmqpConnection> createSink(EventHubAmqpConnection... connections) {
        return Flux.create(emitter -> {
            final AtomicInteger counter = new AtomicInteger();

            emitter.onRequest(request -> {
                for (int i = 0; i < request; i++) {
                    final int index = counter.getAndIncrement();

                    if (index == connections.length) {
                        emitter.error(new RuntimeException(String.format(
                            "Cannot emit more. Index: %s. # of Connections: %s",
                            index, connections.length)));
                        break;
                    }

                    emitter.next(connections[index]);
                }
            });
        }, FluxSink.OverflowStrategy.BUFFER);
    }
}
