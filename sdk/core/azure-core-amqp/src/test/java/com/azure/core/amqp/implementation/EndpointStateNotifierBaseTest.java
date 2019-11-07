// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorContext;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.EndpointState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class EndpointStateNotifierBaseTest {
    private EndpointStateNotifierBase notifier;

    @BeforeEach
    public void setup() {
        notifier = new TestEndpointStateNotifierBase();
    }

    @AfterEach
    public void teardown() {
        notifier.close();
    }

    /**
     * Verify ErrorContexts are propagated to subscribers.
     */
    @Test
    public void notifyError() {
        // Arrange
        final Throwable error1 = new IllegalStateException("bad state");
        final Throwable error2 = new AmqpException(false, "test error", new ErrorContext("test-namespace2"));

        // Act & Assert
        StepVerifier.create(notifier.getErrors())
            .then(() -> notifier.notifyError(error1))
            .expectNext(error1)
            .then(() -> notifier.notifyError(error2))
            .expectNext(error2)
            .then(() -> notifier.close())
            .verifyComplete();
    }

    /**
     * Verify AmqpShutdownSignals are propagated to subscribers.
     */
    @Test
    public void notifyShutdown() {
        // Arrange
        final AmqpShutdownSignal shutdownSignal = new AmqpShutdownSignal(false, true, "test-shutdown");
        final AmqpShutdownSignal shutdownSignal2 = new AmqpShutdownSignal(true, false, "test-shutdown2");

        // Act & Assert
        StepVerifier.create(notifier.getShutdownSignals())
            .then(() -> {
                notifier.notifyShutdown(shutdownSignal);
                notifier.notifyShutdown(shutdownSignal2);
            })
            .expectNext(shutdownSignal, shutdownSignal2)
            .then(() -> notifier.close())
            .verifyComplete();
    }

    /**
     * Verify endpoint states are propagated to subscribers and the connection state property is updated.
     */
    @Test
    public void notifyEndpointState() {
        Assertions.assertEquals(AmqpEndpointState.UNINITIALIZED, notifier.getCurrentState());

        StepVerifier.create(notifier.getConnectionStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> notifier.notifyEndpointState(EndpointState.ACTIVE))
            .assertNext(state -> {
                Assertions.assertEquals(AmqpEndpointState.ACTIVE, state);
                Assertions.assertEquals(AmqpEndpointState.ACTIVE, notifier.getCurrentState());
            })
            .then(() -> {
                notifier.notifyEndpointState(EndpointState.CLOSED);
                notifier.notifyEndpointState(EndpointState.UNINITIALIZED);
            })
            .expectNext(AmqpEndpointState.CLOSED, AmqpEndpointState.UNINITIALIZED)
            .then(() -> notifier.close())
            .verifyComplete();
    }

    @Test
    public void notifyErrorNull() {
        assertThrows(NullPointerException.class, () -> notifier.notifyError(null));
    }

    @Test
    public void notifyShutdownNull() {
        assertThrows(NullPointerException.class, () -> notifier.notifyShutdown(null));
    }

    @Test
    public void notifyEndpointStateStateNull() {
        assertThrows(NullPointerException.class, () -> notifier.notifyEndpointState(null));
    }

    private static class TestEndpointStateNotifierBase extends EndpointStateNotifierBase {
        TestEndpointStateNotifierBase() {
            super(new ClientLogger(TestEndpointStateNotifierBase.class));
        }
    }
}
