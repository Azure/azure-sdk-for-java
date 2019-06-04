// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation.handler;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorContext;
import org.apache.qpid.proton.engine.EndpointState;
import org.junit.Before;
import org.junit.Test;
import reactor.test.StepVerifier;

public class HandlerTest {
    private Handler handler;

    @Before
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
    public void initialErrors() {
        // Act & Assert
        StepVerifier.create(handler.getErrors())
            .then(handler::close)
            .verifyComplete();
    }

    @Test
    public void propagatesStates() {
        // Act & Assert
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .then(() -> handler.onNext(EndpointState.ACTIVE))
            .expectNext(EndpointState.ACTIVE)
            .then(() -> handler.onNext(EndpointState.ACTIVE))
            .then(handler::close)
            .expectNext(EndpointState.CLOSED)
            .verifyComplete();
    }

    @Test
    public void propagatesErrors() {
        // Arrange
        final Throwable exception = new AmqpException(false, "Some test message.");
        final ErrorContext context = new ErrorContext(exception, "test namespace.");

        // Act & Assert
        StepVerifier.create(handler.getErrors())
            .then(() -> handler.onNext(context))
            .expectNext(context)
            .then(handler::close)
            .verifyComplete();
    }

    private static class TestHandler extends Handler {
        TestHandler() {
        }
    }
}
