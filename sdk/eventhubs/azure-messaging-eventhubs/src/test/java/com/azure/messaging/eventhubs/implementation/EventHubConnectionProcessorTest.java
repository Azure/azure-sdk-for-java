/*
 * Copyright (c) 2011-2017 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpShutdownSignal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.Mockito.when;

/**
 * Tests for {@link EventHubConnectionProcessor}.
 */
public class EventHubConnectionProcessorTest {
    @Mock
    private EventHubAmqpConnection connection;
    @Mock
    private EventHubAmqpConnection connection2;
    @Mock
    private EventHubAmqpConnection connection3;

    private final Duration timeout = Duration.ofSeconds(10);
    private DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private DirectProcessor<AmqpShutdownSignal> shutdownSignalProcessor = DirectProcessor.create();

    private EventHubConnectionProcessor eventHubConnectionProcessor = new EventHubConnectionProcessor();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        when(connection.getShutdownSignals()).thenReturn(shutdownSignalProcessor);
    }

    /**
     * Verifies that we can get a new connection.
     */
    @Test
    public void createsNewConnection() {
        EventHubConnectionProcessor processor = Mono.fromCallable(() -> connection).repeat()
            .subscribeWith(eventHubConnectionProcessor);

        StepVerifier.create(processor)
            .expectNext(connection)
            .expectComplete()
            .verify(timeout);
    }


    /**
     * Verifies that we can get a new connection.
     */
    @Test
    public void sameConnectionReturned() {
        EventHubConnectionProcessor processor = Mono.fromCallable(() -> connection).repeat()
            .subscribeWith(eventHubConnectionProcessor);

        StepVerifier.create(processor)
            .expectNext(connection)
            .expectComplete()
            .verify(timeout);

        System.out.println("Logger.");
        StepVerifier.create(processor)
            .expectNext(connection)
            .expectComplete()
            .verify(timeout);
    }
}
