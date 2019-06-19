// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorContext;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.EndpointState;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.test.StepVerifier;

public class EndpointStateNotifierBaseTest {
    private EndpointStateNotifierBase notifier;

    @Before
    public void setup() {
        notifier = new TestEndpointStateNotifierBase();
    }

    @After
    public void teardown() {
        notifier.close();
    }

    /**
     * Verify ErrorContexts are propagated to subscribers.
     */
    @Test
    public void notifyError() {
        // Arrange
        final ErrorContext context = new ErrorContext(new IllegalStateException("bad state"), "test-namespace");
        final ErrorContext context2 = new ErrorContext(new AmqpException(false, "test error"), "test-namespace2");

        // Act & Assert
        StepVerifier.create(notifier.getErrors())
            .then(() -> notifier.notifyError(context))
            .expectNext(context)
            .then(() -> notifier.notifyError(context2))
            .expectNext(context2)
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
        Assert.assertEquals(AmqpEndpointState.UNINITIALIZED, notifier.getCurrentState());

        StepVerifier.create(notifier.getConnectionStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> notifier.notifyEndpointState(EndpointState.ACTIVE))
            .assertNext(state -> {
                Assert.assertEquals(AmqpEndpointState.ACTIVE, state);
                Assert.assertEquals(AmqpEndpointState.ACTIVE, notifier.getCurrentState());
            })
            .then(() -> {
                notifier.notifyEndpointState(EndpointState.CLOSED);
                notifier.notifyEndpointState(EndpointState.UNINITIALIZED);
            })
            .expectNext(AmqpEndpointState.CLOSED, AmqpEndpointState.UNINITIALIZED)
            .then(() -> notifier.close())
            .verifyComplete();
    }

    @Test(expected = NullPointerException.class)
    public void notifyErrorNull() {
        notifier.notifyError(null);
    }

    @Test(expected = NullPointerException.class)
    public void notifyShutdownNull() {
        notifier.notifyShutdown(null);
    }

    @Test(expected = NullPointerException.class)
    public void notifyEndpointStateStateNull() {
        notifier.notifyEndpointState(null);
    }

    private static class TestEndpointStateNotifierBase extends EndpointStateNotifierBase {
        TestEndpointStateNotifierBase() {
            super(new ClientLogger(TestEndpointStateNotifierBase.class));
        }
    }
}
