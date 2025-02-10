// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.AmqpMetricsProvider;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.Source;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReceiveLinkHandlerTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(10);
    private static final String CONNECTION_ID = "connection-id";
    private static final String HOSTNAME = "test-hostname";
    private static final String LINK_NAME = "test-link-name";
    private static final String ENTITY_PATH = "test-entity-path";
    private static final AmqpRetryOptions RETRY_OPTIONS = new AmqpRetryOptions();
    private static final byte[] GUID_ENCODED_DELIVERY_TAG
        = { -67, 35, 17, -88, -27, -60, 74, -111, -107, 46, -76, -78, 29, 19, -37, -115 };
    private static final UUID UUID_DECODED_FROM_ENCODED_GUID = UUID.fromString("a81123bd-c4e5-914a-952e-b4b21d13db8d");

    @Mock
    private Delivery delivery;
    @Mock
    private Event event;
    @Mock
    private Receiver receiver;
    @Mock
    private Source source;
    @Mock
    private ReactorDispatcher dispatcher;

    private final ReceiveLinkHandler2 handler = new ReceiveLinkHandler2(CONNECTION_ID, HOSTNAME, LINK_NAME, ENTITY_PATH,
        DeliverySettleMode.SETTLE_ON_DELIVERY, dispatcher, new AmqpRetryOptions(), true, AmqpMetricsProvider.noop());

    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        when(event.getLink()).thenReturn(receiver);
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
            .verify(VERIFY_TIMEOUT);

        assertEquals(LINK_NAME, handler.getLinkName());
    }

    /**
     * Tests onLinkRemoteClose.
     */
    @Test
    public void onRemoteClose() {
        when(receiver.getLocalState()).thenReturn(EndpointState.CLOSED);

        StepVerifier.create(handler.getMessages())
            .then(() -> handler.onLinkRemoteClose(event))
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.CLOSED)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

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

        StepVerifier.create(handler.getEndpointStates())
            .then(() -> handler.onDelivery(event))
            .expectNoEvent(Duration.ofSeconds(1))
            .thenCancel()
            .verify(VERIFY_TIMEOUT);

        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.ACTIVE)
            .thenCancel()
            .verify(VERIFY_TIMEOUT);
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
        when(delivery.getTag()).thenReturn(GUID_ENCODED_DELIVERY_TAG);

        when(receiver.getLocalState()).thenReturn(EndpointState.ACTIVE);

        StepVerifier.create(handler.getMessages()).then(() -> handler.onDelivery(event)).assertNext(message -> {
            assertInstanceOf(MessageWithDeliveryTag.class, message);
            final MessageWithDeliveryTag messageWithDeliveryTag = (MessageWithDeliveryTag) message;
            assertEquals(UUID_DECODED_FROM_ENCODED_GUID, messageWithDeliveryTag.getDeliveryTag());
        }).then(() -> handler.onLinkRemoteClose(closeEvent)).expectComplete().verify(VERIFY_TIMEOUT);

        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.CLOSED)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);
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

        StepVerifier.create(handler.getMessages())
            .then(() -> handler.onDelivery(event))
            .expectNoEvent(Duration.ofSeconds(1))
            .thenCancel()
            .verify(VERIFY_TIMEOUT);

        verify(delivery).disposition(argThat(state -> state.getType() == DeliveryState.DeliveryStateType.Modified));
        verify(delivery).settle();
    }

    /**
     * Verifies {@link NullPointerException}.
     */
    @Test
    public void constructor() {
        // Act
        assertThrows(NullPointerException.class, () -> new ReceiveLinkHandler2(null, HOSTNAME, LINK_NAME, ENTITY_PATH,
            DeliverySettleMode.SETTLE_ON_DELIVERY, dispatcher, RETRY_OPTIONS, true, AmqpMetricsProvider.noop()));
        assertThrows(NullPointerException.class,
            () -> new ReceiveLinkHandler2(CONNECTION_ID, null, LINK_NAME, ENTITY_PATH,
                DeliverySettleMode.SETTLE_ON_DELIVERY, dispatcher, RETRY_OPTIONS, true, AmqpMetricsProvider.noop()));
        assertThrows(NullPointerException.class,
            () -> new ReceiveLinkHandler2(CONNECTION_ID, HOSTNAME, null, ENTITY_PATH,
                DeliverySettleMode.SETTLE_ON_DELIVERY, dispatcher, RETRY_OPTIONS, true, AmqpMetricsProvider.noop()));
        assertThrows(NullPointerException.class, () -> new ReceiveLinkHandler2(CONNECTION_ID, HOSTNAME, LINK_NAME, null,
            DeliverySettleMode.SETTLE_ON_DELIVERY, dispatcher, RETRY_OPTIONS, true, AmqpMetricsProvider.noop()));
    }

    /**
     * Tests that the close operation completes the fluxes and then emits a close.
     */
    @Test
    public void close() {
        // Act & Assert
        StepVerifier.create(handler.getMessages()).then(() -> handler.close()).expectComplete().verify(VERIFY_TIMEOUT);

        // The only thing we should be doing here is emitting a close state. We are waiting for
        // the remote close event.
        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.CLOSED)
            .expectNoEvent(Duration.ofMillis(500))
            .thenCancel()
            .verify(VERIFY_TIMEOUT);

        assertEquals(LINK_NAME, handler.getLinkName());
    }

    /**
     * Tests that if the link was never active, then it will be immediately closed.
     */
    @Test
    public void onLinkLocalCloseNotRemoteOpened() {
        when(receiver.getLocalState()).thenReturn(EndpointState.CLOSED);

        StepVerifier.create(handler.getEndpointStates())
            .then(() -> handler.onLinkLocalClose(event))
            .expectNext(EndpointState.UNINITIALIZED, EndpointState.CLOSED)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.CLOSED)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);
    }

    /**
     * Tests the normal case where the link was active and then the user called sender.close().
     */
    @Test
    public void onLinkLocalClose() {
        when(receiver.getRemoteSource()).thenReturn(source);

        final Event closeEvent = mock(Event.class);
        final Receiver closedReceiver = mock(Receiver.class);
        when(closeEvent.getLink()).thenReturn(closedReceiver);
        when(closedReceiver.getLocalState()).thenReturn(EndpointState.CLOSED);
        when(closedReceiver.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        final Event remoteCloseEvent = mock(Event.class);
        final Receiver remoteClosedReceiver = mock(Receiver.class);
        when(remoteCloseEvent.getLink()).thenReturn(closedReceiver);
        when(remoteClosedReceiver.getLocalState()).thenReturn(EndpointState.CLOSED);
        when(remoteClosedReceiver.getRemoteState()).thenReturn(EndpointState.CLOSED);

        StepVerifier.create(handler.getEndpointStates())
            .then(() -> handler.onLinkRemoteOpen(event))
            .expectNext(EndpointState.UNINITIALIZED, EndpointState.ACTIVE)
            .then(() -> handler.onLinkLocalClose(closeEvent))
            .expectNoEvent(Duration.ofMillis(500))
            .then(() -> handler.onLinkRemoteClose(remoteCloseEvent))
            .expectNext(EndpointState.CLOSED)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);
    }
}
