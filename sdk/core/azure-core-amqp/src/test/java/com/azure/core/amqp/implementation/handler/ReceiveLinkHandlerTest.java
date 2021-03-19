// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReceiveLinkHandlerTest {
    private static final String CONNECTION_ID = "connection-id";
    private static final String HOSTNAME = "test-hostname";
    private static final String LINK_NAME = "link-name";
    private static final String ENTITY_PATH = "test-entity-path";

    @Mock
    private Event event;
    @Mock
    private Receiver receiver;
    @Mock
    private Delivery delivery;

    private AutoCloseable mocksCloseable;

    private final ReceiveLinkHandler linkHandler = new ReceiveLinkHandler(CONNECTION_ID, HOSTNAME, LINK_NAME,
        ENTITY_PATH);

    @BeforeAll
    public static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(5));
    }

    @AfterAll
    public static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    public void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        when(event.getLink()).thenReturn(receiver);
        when(event.getReceiver()).thenReturn(receiver);
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMocks();

        linkHandler.close();

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void constructor() {
        assertEquals(LINK_NAME, linkHandler.getLinkName());
        assertEquals(CONNECTION_ID, linkHandler.getConnectionId());
        assertEquals(HOSTNAME, linkHandler.getHostname());
    }

    /**
     * We publish credit updates when they are different from the previous one.
     */
    @Test
    public void linkFlowUpdatesCredits() {
        final int credit1 = 10;
        final int credit2 = 5;
        final int credit3 = 5;
        final int credit4 = 2;

        when(receiver.getRemoteCredit()).thenReturn(credit1, credit2, credit3, credit4);

        StepVerifier.create(linkHandler.getLinkCredits())
            .then(() -> {
                linkHandler.onLinkFlow(event);
                linkHandler.onLinkFlow(event);
            })
            .expectNext(credit1, credit2)
            .then(() -> {
                linkHandler.onLinkFlow(event);
                linkHandler.onLinkFlow(event);
                linkHandler.close();
            })
            .expectNext(credit4)
            .verifyComplete();
    }

    /**
     * Link is active when we get a remote open signal.
     */
    @Test
    public void linkRemoteOpenActive() {
        final Source source = mock(Source.class);
        when(receiver.getRemoteSource()).thenReturn(source);

        StepVerifier.create(linkHandler.getEndpointStates())
            .then(() -> {
                linkHandler.onLinkRemoteOpen(event);
                linkHandler.close();
            })
            .expectNext(EndpointState.ACTIVE)
            .then(() -> linkHandler.onLinkRemoteOpen(event)) // We don't expect another emission.
            .verifyComplete();
    }

    /**
     * First delivery, we will receive an on active in addition to a delivery.
     */
    @Test
    public void onDeliveryFirst() {
        when(event.getDelivery()).thenReturn(delivery);
        when(receiver.getLocalState()).thenReturn(EndpointState.ACTIVE);

        when(delivery.getLink()).thenReturn(receiver);
        when(delivery.isPartial()).thenReturn(false);
        when(delivery.isSettled()).thenReturn(false);

        StepVerifier.create(linkHandler.getDeliveredMessages())
            .then(() -> linkHandler.onDelivery(event))
            .expectNext(delivery)
            .thenCancel()
            .verify();

        StepVerifier.create(linkHandler.getEndpointStates())
            .expectNext(EndpointState.ACTIVE)
            .then(() -> {
                linkHandler.close();
                linkHandler.close();
            })
            .verifyComplete();
    }
}

