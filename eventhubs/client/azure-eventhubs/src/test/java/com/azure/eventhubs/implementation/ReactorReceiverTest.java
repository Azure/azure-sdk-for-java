// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.CBSNode;
import com.azure.eventhubs.implementation.handler.ReceiveLinkHandler;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReactorReceiverTest {
    @Mock
    private Receiver receiver;
    @Mock
    private CBSNode cbsNode;
    @Mock
    private Event event;

    private ReceiveLinkHandler receiverHandler;
    private ActiveClientTokenManager tokenManager;
    private ReactorReceiver reactorReceiver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(cbsNode.authorize(any())).thenReturn(Mono.empty());

        when(event.getLink()).thenReturn(receiver);
        when(receiver.getRemoteSource()).thenReturn(new Source());

        receiverHandler = new ReceiveLinkHandler("test-connection-id", "test-host", "test-receiver-name");
        tokenManager = new ActiveClientTokenManager(Mono.just(cbsNode), "test-tokenAudience", Duration.ofSeconds(30));
        reactorReceiver = new ReactorReceiver("test-entityPath", receiver, receiverHandler, tokenManager);
    }

    @After
    public void teardown() {
        Mockito.framework().clearInlineMocks();

        receiver = null;
        cbsNode = null;
        event = null;
    }

    /**
     * Verify we can add credits to the link.
     */
    @Test
    public void addCredits() {
        final int credits = 15;
        reactorReceiver.addCredits(credits);

        verify(receiver, times(1)).flow(credits);
    }

    /**
     * Verifies EndpointStates are propagated.
     */
    @Test
    public void updateEndpointState() {
        StepVerifier.create(reactorReceiver.getConnectionStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> receiverHandler.onLinkRemoteOpen(event))
            .expectNext(AmqpEndpointState.ACTIVE)
            .then(() -> receiverHandler.close())
            .expectNext(AmqpEndpointState.CLOSED)
            .then(() -> reactorReceiver.close())
            .verifyComplete();
    }
}
