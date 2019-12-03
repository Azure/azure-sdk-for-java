// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
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
    @Mock
    private ClaimsBasedSecurityNode cbsNode;
    @Mock
    private MessageSerializer serializer;
    @Mock
    private ReactorProvider reactorProvider;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(reactor.selectable()).thenReturn(selectable);
        when(event.getSession()).thenReturn(session);

        ReactorDispatcher dispatcher = new ReactorDispatcher(reactor);
        this.handler = new SessionHandler(ID, HOST, ENTITY_PATH, dispatcher, Duration.ofSeconds(60));

        when(reactorProvider.getReactor()).thenReturn(reactor);
        when(reactorProvider.getReactorDispatcher()).thenReturn(dispatcher);

        MockReactorHandlerProvider handlerProvider = new MockReactorHandlerProvider(reactorProvider, null, handler, null, null);
        AzureTokenManagerProvider azureTokenManagerProvider = new AzureTokenManagerProvider(
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, HOST, "a-test-scope");
        this.reactorSession = new ReactorSession(session, handler, NAME, reactorProvider, handlerProvider,
            Mono.just(cbsNode), azureTokenManagerProvider, serializer, TIMEOUT);
    }

    @AfterEach
    public void teardown() {
        session = null;
        reactor = null;
        selectable = null;
        event = null;
        cbsNode = null;

        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void verifyConstructor() {
        // Assert
        verify(session, times(1)).open();

        Assertions.assertSame(session, reactorSession.session());
        Assertions.assertEquals(NAME, reactorSession.getSessionName());
        Assertions.assertEquals(TIMEOUT, reactorSession.getOperationTimeout());
    }

    @Test
    public void verifyEndpointStates() {
        when(session.getLocalState()).thenReturn(EndpointState.ACTIVE);

        StepVerifier.create(reactorSession.getEndpointStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> handler.onSessionRemoteOpen(event))
            .expectNext(AmqpEndpointState.ACTIVE)
            .then(() -> handler.close())
            .expectNext(AmqpEndpointState.CLOSED)
            .then(() -> reactorSession.close())
            .expectComplete()
            .verify(Duration.ofSeconds(10));
    }
}
