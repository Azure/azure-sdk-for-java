// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.EndpointState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class HandlerTest {
    private Handler handler;

    @BeforeEach
    public void setup() {
        handler = new TestHandler();
    }

    @Test
    public void initialHandlerState() {
        // Act & Assert
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .then(handler::close)
            .expectNext(EndpointState.CLOSED)
            .verifyComplete();
    }

    @Test
    public void propagatesStates() {
        // Act & Assert
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .then(() -> {
                handler.onNext(EndpointState.ACTIVE);
                handler.onNext(EndpointState.ACTIVE);
            })
            .expectNext(EndpointState.ACTIVE)
            .then(handler::close)
            .expectNext(EndpointState.CLOSED)
            .verifyComplete();
    }

    @Test
    public void propagatesErrors() {
        // Arrange
        final AmqpErrorContext context = new AmqpErrorContext("test namespace.");
        final Throwable exception = new AmqpException(false, "Some test message.", context);

        // Act & Assert
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .then(() -> handler.onError(exception))
            .expectErrorMatches(e -> e.equals(exception))
            .verify();
    }

    @Test
    public void propagatesErrorsOnce() {
        // Arrange
        final AmqpErrorContext context = new AmqpErrorContext("test namespace.");
        final Throwable exception = new AmqpException(false, "Some test message.", context);
        final Throwable exception2 = new AmqpException(false, "Some test message2.", context);

        // Act & Assert
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .then(() -> {
                handler.onError(exception);
                handler.onError(exception2);
            })
            .expectErrorMatches(e -> e.equals(exception))
            .verify();

        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .expectErrorMatches(e -> e.equals(exception))
            .verify();
    }

    @Test
    public void completesOnce() {
        // Act & Assert
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .then(() -> handler.onNext(EndpointState.ACTIVE))
            .expectNext(EndpointState.ACTIVE)
            .then(() -> handler.close())
            .expectNext(EndpointState.CLOSED)
            .expectComplete()
            .verify();

        // The last state is always replayed before it is closed.
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.CLOSED)
            .expectComplete()
            .verify();
    }

    private static class TestHandler extends Handler {
        TestHandler() {
            super("test-connection-id", "test-hostname",
                new ClientLogger(TestHandler.class));
        }
    }
}
