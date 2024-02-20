// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import org.apache.qpid.proton.engine.EndpointState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link Handler}.
 */
public class HandlerTest {
    private Handler handler;

    @BeforeEach
    public void setup() {
        handler = new TestHandler();
    }

    @AfterEach
    public void teardown() {
        if (handler != null) {
            handler.close();
        }

        Mockito.framework().clearInlineMock(this);
    }

    @Test
    public void constructor() {
        // Arrange
        final String connectionId = "id";
        final String hostname = "hostname";

        // Act
        assertThrows(NullPointerException.class, () -> new TestHandler(null, hostname));
        assertThrows(NullPointerException.class, () -> new TestHandler(connectionId, null));
    }

    @Test
    public void initialHandlerState() {
        // Act & Assert
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .then(handler::close)
            .expectNext(EndpointState.CLOSED)
            .verifyComplete();

        assertEquals(TestHandler.CONNECTION_ID, handler.getConnectionId());
        assertEquals(TestHandler.HOSTNAME, handler.getHostname());
    }

    @Test
    public void propagatesDistinctStates() {
        // Act & Assert
        StepVerifier.create(handler.getEndpointStates()).expectNext(EndpointState.UNINITIALIZED).then(() -> {
            // Verify that it only propagates the UNINITIALIZED state once.
            // In previous incarnation of distinct, it hashed all the previous values and would only push values
            // that were not seen yet.
            handler.onNext(EndpointState.ACTIVE);
            handler.onNext(EndpointState.UNINITIALIZED);
            handler.onNext(EndpointState.UNINITIALIZED);
            handler.onNext(EndpointState.CLOSED);
        })
            .expectNext(EndpointState.ACTIVE, EndpointState.UNINITIALIZED, EndpointState.CLOSED)
            .then(handler::close)
            .verifyComplete();
    }

    @Test
    public void propagatesStates() {
        // Act & Assert
        StepVerifier.create(handler.getEndpointStates()).expectNext(EndpointState.UNINITIALIZED).then(() -> {
            // Verify that it only propagates the active state once.
            handler.onNext(EndpointState.ACTIVE);
            handler.onNext(EndpointState.ACTIVE);
        }).expectNext(EndpointState.ACTIVE).then(handler::close).expectNext(EndpointState.CLOSED).verifyComplete();
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
        StepVerifier.create(handler.getEndpointStates()).expectNext(EndpointState.UNINITIALIZED).then(() -> {
            handler.onError(exception);
            handler.onError(exception2);
        }).expectErrorMatches(e -> e.equals(exception)).verify();

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
        StepVerifier.create(handler.getEndpointStates()).expectNext(EndpointState.CLOSED).expectComplete().verify();
    }

    /**
     * Test that we are handling signals serially. Otherwise, we get an exception:
     * EmissionException: Spec. Rule 1.3 encountered when trying to recover receiver.
     *
     * @see <a href="https://github.com/Azure/azure-sdk-for-java/issues/24762">#24762</a>
     */
    @Test
    public void serialSignalling() {
        final int parallelism = Runtime.getRuntime().availableProcessors();
        final Disposable subscription = handler.getEndpointStates().subscribe();

        Flux.range(0, 500).parallel(parallelism).runOn(Schedulers.parallel()).flatMap(index -> {
            final EndpointState state;
            final int current = index % 3;

            switch (current) {
                case 0:
                    state = EndpointState.ACTIVE;
                    break;

                case 1:
                    state = EndpointState.CLOSED;
                    break;

                case 2:
                    state = EndpointState.UNINITIALIZED;
                    break;

                default:
                    throw new IllegalStateException("This shouldn't have happened. value:" + index);
            }

            if (index == 500) {
                handler.onError(new RuntimeException("Test Error."));
            } else {
                handler.onNext(state);
            }

            return Mono.empty();
        }).then().block();

        subscription.dispose();
    }

    private static class TestHandler extends Handler {
        static final String CONNECTION_ID = "test-connection-id";
        static final String HOSTNAME = "test-hostname";

        TestHandler() {
            super(CONNECTION_ID, HOSTNAME);
        }

        TestHandler(String connectionId, String hostname) {
            super(connectionId, hostname);
        }
    }
}
