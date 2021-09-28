// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Sender;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SendLinkHandler}.
 */
public class SendLinkHandlerTest {
    private static final String CONNECTION_ID = "connection-id";
    private static final String HOSTNAME = "test-hostname";
    private static final String LINK_NAME = "test-link-name";
    private static final String ENTITY_PATH = "test-entity-path";

    @Mock
    private Delivery delivery;
    @Mock
    private Event event;
    @Mock
    private Sender sender;
    @Mock
    private Target target;

    private final SendLinkHandler handler = new SendLinkHandler(CONNECTION_ID, HOSTNAME, LINK_NAME, ENTITY_PATH);

    private AutoCloseable mocksCloseable;

    @BeforeAll
    public static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(10));
    }

    @AfterAll
    public static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    public void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        when(event.getLink()).thenReturn(sender);
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);

        handler.close();

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    /**
     * Verifies {@link NullPointerException}.
     */
    @Test
    public void constructor() {
        // Act
        assertThrows(NullPointerException.class,
            () -> new SendLinkHandler(null, HOSTNAME, LINK_NAME, ENTITY_PATH));
        assertThrows(NullPointerException.class,
            () -> new SendLinkHandler(CONNECTION_ID, null, LINK_NAME, ENTITY_PATH));
        assertThrows(NullPointerException.class,
            () -> new SendLinkHandler(CONNECTION_ID, HOSTNAME, null, ENTITY_PATH));
        assertThrows(NullPointerException.class,
            () -> new SendLinkHandler(CONNECTION_ID, HOSTNAME, LINK_NAME, null));
    }

    /**
     * Tests that the close operation completes the fluxes and then emits a close.
     */
    @Test
    public void close() {
        // Act & Assert
        StepVerifier.create(handler.getLinkCredits())
            .then(() -> handler.close())
            .verifyComplete();

        StepVerifier.create(handler.getDeliveredMessages())
            .verifyComplete();

        // The only thing we should be doing here is emitting a close state. We are waiting for
        // the remote close event.
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.CLOSED)
            .expectNoEvent(Duration.ofMillis(500))
            .thenCancel()
            .verify();

        assertEquals(LINK_NAME, handler.getLinkName());
    }

    /**
     * Tests that if the link was never active, then it will be immediately closed.
     */
    @Test
    public void onLinkLocalCloseNotRemoteActive() {
        when(sender.getLocalState()).thenReturn(EndpointState.CLOSED);

        StepVerifier.create(handler.getEndpointStates())
            .then(() -> handler.onLinkLocalClose(event))
            .expectNext(EndpointState.UNINITIALIZED, EndpointState.CLOSED)
            .verifyComplete();

        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.CLOSED)
            .expectComplete()
            .verify();
    }

    /**
     * Tests the normal case where the link was active and then the user called sender.close().
     */
    @Test
    public void onLinkLocalClose() {
        when(sender.getRemoteTarget()).thenReturn(target);

        final Event closeEvent = mock(Event.class);
        final Sender closedSender = mock(Sender.class);
        when(closeEvent.getLink()).thenReturn(closedSender);
        when(closedSender.getLocalState()).thenReturn(EndpointState.CLOSED);
        when(closedSender.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        final Event remoteCloseEvent = mock(Event.class);
        final Sender remoteClosedSender = mock(Sender.class);
        when(remoteCloseEvent.getLink()).thenReturn(closedSender);
        when(remoteClosedSender.getLocalState()).thenReturn(EndpointState.CLOSED);
        when(remoteClosedSender.getRemoteState()).thenReturn(EndpointState.CLOSED);

        StepVerifier.create(handler.getEndpointStates())
            .then(() -> handler.onLinkRemoteOpen(event))
            .expectNext(EndpointState.UNINITIALIZED, EndpointState.ACTIVE)
            .then(() -> handler.onLinkLocalClose(closeEvent))
            .expectNoEvent(Duration.ofMillis(500))
            .then(() -> handler.onLinkRemoteClose(remoteCloseEvent))
            .expectNext(EndpointState.CLOSED)
            .verifyComplete();
    }

    /**
     * Tests onLinkRemoteOpen.
     */
    @Test
    public void onLinkRemoteOpen() {
        when(sender.getRemoteTarget()).thenReturn(target);

        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .then(() -> handler.onLinkRemoteOpen(event))
            .expectNext(EndpointState.ACTIVE)
            .then(() -> handler.onLinkRemoteOpen(event)) // We only expect the active state to be emitted once.
            .thenCancel()
            .verify();
    }

    /**
     * Tests onLinkFlow for the first flow frame.
     */
    @Test
    public void onLinkFlow() {
        final int credits = 15;
        when(event.getSender()).thenReturn(sender);
        when(sender.getRemoteCredit()).thenReturn(credits);

        StepVerifier.create(handler.getEndpointStates())
            .then(() -> handler.onLinkFlow(event))
            .expectNext(EndpointState.UNINITIALIZED, EndpointState.ACTIVE)
            .thenCancel()
            .verify();

        StepVerifier.create(handler.getLinkCredits())
            .expectNext(credits)
            .then(() -> handler.close())
            .expectComplete()
            .verify();
    }

    /**
     * Tests that deliveries are published and it keeps publishing deliveries while there are more.
     */
    @Test
    public void onDelivery() {
        when(event.getDelivery()).thenReturn(delivery);

        when(delivery.getLink()).thenReturn(sender);
        when(delivery.getTag()).thenReturn("hello".getBytes(StandardCharsets.UTF_8));
        when(delivery.isPartial()).thenReturn(false);
        when(delivery.isSettled()).thenReturn(false);

        final Delivery delivery2 = mock(Delivery.class);
        when(delivery2.getLink()).thenReturn(sender);
        when(delivery2.getTag()).thenReturn("hello2".getBytes(StandardCharsets.UTF_8));
        final Delivery delivery3 = mock(Delivery.class);
        when(delivery3.getLink()).thenReturn(sender);
        when(delivery3.getTag()).thenReturn("hello3".getBytes(StandardCharsets.UTF_8));

        when(sender.current()).thenReturn(delivery2, delivery3, null);

        StepVerifier.create(handler.getDeliveredMessages())
            .then(() -> handler.onDelivery(event))
            .expectNext(delivery, delivery2, delivery3)
            .thenCancel()
            .verify();

        verify(delivery).settle();
        verify(delivery2).settle();
        verify(delivery3).settle();
    }
}
