// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.Source;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReceiveLinkHandlerTest {
    private static final String CONNECTION_ID = "connection-id";
    private static final String HOSTNAME = "test-hostname";
    private static final String LINK_NAME = "test-link-name";
    private static final String ENTITY_PATH = "test-entity-path";

    @Mock
    private Delivery delivery;
    @Mock
    private Event event;
    @Mock
    private Receiver receiver;
    @Mock
    private Source source;

    private final ReceiveLinkHandler handler = new ReceiveLinkHandler(CONNECTION_ID, HOSTNAME, LINK_NAME, ENTITY_PATH);

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

        when(event.getLink()).thenReturn(receiver);
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMocks();

        handler.close();

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    /**
     * Tests onLinkRemoteOpen.
     */
    @Test
    public void onRemoteOpen() {
        when(receiver.getRemoteSource()).thenReturn(source);

        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.UNINITIALIZED)
            .then(() -> handler.onLinkRemoteOpen(event))
            .expectNext(EndpointState.ACTIVE)
            .then(() -> handler.onLinkRemoteOpen(event)) // We only expect the active state to be emitted once.
            .thenCancel()
            .verify();

        assertEquals(LINK_NAME, handler.getLinkName());
    }

    /**
     * Tests onLinkRemoteClose.
     */
    @Test
    public void onRemoteClose() {
        when(receiver.getLocalState()).thenReturn(EndpointState.CLOSED);

        StepVerifier.create(handler.getDeliveredMessages())
            .then(() -> handler.onLinkRemoteClose(event))
            .verifyComplete();

        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.CLOSED)
            .expectComplete()
            .verify();

        assertEquals(LINK_NAME, handler.getLinkName());
    }

    /**
     * Tests that partial deliveries are not published and the first status is emitted.
     */
    @Test
    public void onDeliveryPartialDelivery() {
        when(event.getDelivery()).thenReturn(delivery);

        when(delivery.getLink()).thenReturn(receiver);
        when(delivery.isPartial()).thenReturn(true);

        StepVerifier.create(handler.getDeliveredMessages())
            .then(() -> handler.onDelivery(event))
            .expectNoEvent(Duration.ofSeconds(1))
            .thenCancel()
            .verify();

        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.ACTIVE)
            .thenCancel()
            .verify();
    }

    /**
     * Tests that deliveries are published and the first status is emitted.
     */
    @Test
    public void onDelivery() {
        final Event closeEvent = mock(Event.class);
        when(closeEvent.getLink()).thenReturn(receiver);

        when(event.getDelivery()).thenReturn(delivery);

        when(delivery.getLink()).thenReturn(receiver);
        when(delivery.isPartial()).thenReturn(false);
        when(delivery.isSettled()).thenReturn(false);

        when(receiver.getLocalState()).thenReturn(EndpointState.ACTIVE);

        StepVerifier.create(handler.getDeliveredMessages())
            .then(() -> handler.onDelivery(event))
            .expectNext(delivery)
            .then(() -> handler.onLinkRemoteClose(closeEvent))
            .expectComplete()
            .verify();

        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.CLOSED)
            .expectComplete()
            .verify();
    }

    /**
     * Tests that deliveries are published and the first status is emitted.
     */
    @Test
    public void onDeliveryClosedLink() {
        final Event closeEvent = mock(Event.class);
        when(closeEvent.getLink()).thenReturn(receiver);

        when(event.getDelivery()).thenReturn(delivery);

        when(delivery.getLink()).thenReturn(receiver);
        when(delivery.isPartial()).thenReturn(false);
        when(delivery.isSettled()).thenReturn(false);

        when(receiver.getLocalState()).thenReturn(EndpointState.CLOSED);

        StepVerifier.create(handler.getDeliveredMessages())
            .then(() -> handler.onDelivery(event))
            .expectNoEvent(Duration.ofSeconds(1))
            .thenCancel()
            .verify();

        verify(delivery).disposition(argThat(state -> state.getType() == DeliveryState.DeliveryStateType.Modified));
        verify(delivery).settle();
    }
}
