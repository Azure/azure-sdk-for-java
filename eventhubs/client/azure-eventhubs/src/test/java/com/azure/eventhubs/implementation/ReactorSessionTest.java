// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.eventhubs.implementation.handler.SessionHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReactorSessionTest {
    private static final String ID = "test-connection-id";
    private static final String HOST = "test-host";
    private static final String ENTITY_PATH = "test-entity-path";
    private static final String NAME = "test-session-name";
    private static final Duration TIMEOUT = Duration.ofSeconds(45);

    private ReactorDispatcher dispatcher;
    private SessionHandler handler;
    private ReactorSession reactorSession;

    @Mock
    private Session session;
    @Mock
    private Reactor reactor;
    @Mock
    private Selectable selectable;
    @Mock
    private Event event;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(reactor.selectable()).thenReturn(selectable);
        when(event.getSession()).thenReturn(session);

        this.dispatcher = new ReactorDispatcher(reactor);
        this.handler = new SessionHandler(ID, HOST, ENTITY_PATH, dispatcher, Duration.ofSeconds(60));
        this.reactorSession = new ReactorSession(session, handler, NAME, dispatcher, TIMEOUT);

    }

    @Test
    public void verifyConstructor() {
        // Assert
        verify(session, times(1)).open();

        Assert.assertSame(session, reactorSession.session());
        Assert.assertEquals(NAME, reactorSession.getSessionName());
        Assert.assertEquals(TIMEOUT, reactorSession.getOpenTimeout());
    }

    @Test
    public void verifyEndpointStates() {
        when(session.getLocalState()).thenReturn(EndpointState.ACTIVE);

        StepVerifier.create(reactorSession.getConnectionStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> handler.onSessionRemoteOpen(event))
            .expectNext(AmqpEndpointState.ACTIVE)
            .then(() -> handler.close())
            // Expect two close notifications. One for getErrors() subscription and getEndpointStates();
            .expectNext(AmqpEndpointState.CLOSED, AmqpEndpointState.CLOSED)
            .then(() -> reactorSession.close())
            .verifyComplete();
    }
}
