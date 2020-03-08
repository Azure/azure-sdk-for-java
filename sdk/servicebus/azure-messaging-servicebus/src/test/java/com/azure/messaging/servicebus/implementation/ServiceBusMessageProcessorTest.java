// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static com.azure.messaging.servicebus.TestUtils.createMessageSink;

class ServiceBusMessageProcessorTest {
    @Mock
    private ServiceBusReceivedMessage message1;
    @Mock
    private ServiceBusReceivedMessage message2;
    @Mock
    private ServiceBusReceivedMessage message3;
    @Mock
    private ServiceBusReceivedMessage message4;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Verifies that all messages are emitted downstream.
     */
    @Test
    void emitsAndAutoCompletes() {
        // Arrange
        final Set<ServiceBusReceivedMessage> expected = new HashSet<>();
        expected.add(message1);
        expected.add(message2);
        expected.add(message3);
        expected.add(message4);

        final Function<ServiceBusReceivedMessage, Mono<Void>> onCompleteMethod = (item) -> {
            final boolean removed = expected.remove(item);
            Assertions.assertTrue(removed, "Should have been able to remove item from set.");
            return Mono.empty();
        };

        final ServiceBusMessageProcessor processor = createMessageSink(message1, message2, message3, message4)
            .subscribeWith(new ServiceBusMessageProcessor(true, onCompleteMethod));

        // Act & Assert
        StepVerifier.create(processor)
            .expectNext(message1, message2, message3, message4)
            .verifyComplete();

        Assertions.assertTrue(expected.isEmpty(), "There should be no more values in the expected set.");
    }

    /**
     * Verifies that all messages are emitted downstream and auto complete is not invoked.
     */
    @Test
    void emitsDoesNotAutoComplete() {
        // Arrange
        final Function<ServiceBusReceivedMessage, Mono<Void>> onCompleteMethod = (item) -> {
            Assertions.fail("Should not have called complete() method. item:" + item);
            return Mono.empty();
        };

        final ServiceBusMessageProcessor processor = createMessageSink(message1, message2, message3, message4)
            .subscribeWith(new ServiceBusMessageProcessor(false, onCompleteMethod));

        // Act & Assert
        StepVerifier.create(processor)
            .expectNext(message1, message2, message3, message4)
            .verifyComplete();
    }
}
