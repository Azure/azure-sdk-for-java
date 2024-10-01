// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.amqp.implementation.ProtonSession.ProtonSessionClosedException;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ProtonSessionTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private AutoCloseable mocksCloseable;

    @BeforeEach
    void session() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    void shouldOpenQPidSession() {
        final Session qpidSession = mock(Session.class);
        doNothing().when(qpidSession).open();

        final Setup setup = Setup.create(qpidSession);
        final ProtonSession session = setup.getSession();

        StepVerifier.create(session.open()).verifyComplete();

        StepVerifier.create(session.open()).verifyComplete();

        // assert the open-only-once semantics.
        verify(qpidSession, times(1)).open();
        Assertions.assertEquals(1, setup.getDispatchCount());
    }

    @Test
    void shouldThrowQPidOpenError() {
        final Session qpidSession = mock(Session.class);
        final RejectedExecutionException dispatchError = new RejectedExecutionException("QPid Reactor was disposed.");
        final Map<Integer, RejectedExecutionException> dispatchErrors = new HashMap<>(1);
        dispatchErrors.put(0, dispatchError);

        final Setup setup = Setup.create(qpidSession, dispatchErrors);
        final ProtonSession session = setup.getSession();

        // assert the dispatch error on initial 'open' attempt.
        StepVerifier.create(session.open()).verifyErrorSatisfies(e -> {
            Assertions.assertInstanceOf(AmqpException.class, e);
            final AmqpException ae = (AmqpException) e;
            Assertions.assertNotNull(ae.getCause());
            Assertions.assertEquals(dispatchError, ae.getCause());
        });

        // assert the second call to 'open' replays the same dispatch error.
        StepVerifier.create(session.open()).verifyErrorSatisfies(e -> {
            Assertions.assertInstanceOf(AmqpException.class, e);
            final AmqpException ae = (AmqpException) e;
            Assertions.assertNotNull(ae.getCause());
            Assertions.assertEquals(dispatchError, ae.getCause());
        });
        Assertions.assertEquals(1, setup.getDispatchCount());
    }

    @Test
    void shouldThrowIfOpenAttemptedAfterDisposal() {
        final Session qpidSession = mock(Session.class);

        final Setup setup = Setup.create(qpidSession);
        final ProtonSession session = setup.getSession();

        session.beginClose(null);
        StepVerifier.create(session.open()).verifyErrorSatisfies(e -> {
            Assertions.assertInstanceOf(ProtonSessionClosedException.class, e);
        });
    }

    @Test
    void shouldThrowIfChannelRequestedBeforeOpen() {
        final Session qpidSession = mock(Session.class);

        final Setup setup = Setup.create(qpidSession);
        final ProtonSession session = setup.getSession();

        StepVerifier.create(session.channel("cbs", TIMEOUT)).verifyErrorSatisfies(e -> {
            Assertions.assertInstanceOf(IllegalStateException.class, e);
        });
    }

    @Test
    void shouldThrowIfChannelRequestedAfterDisposal() {
        final Session qpidSession = mock(Session.class);

        final Setup setup = Setup.create(qpidSession);
        final ProtonSession session = setup.getSession();

        StepVerifier.create(session.open()).verifyComplete();
        session.beginClose(null);
        StepVerifier.create(session.channel("cbs", TIMEOUT)).verifyErrorSatisfies(e -> {
            Assertions.assertInstanceOf(ProtonSessionClosedException.class, e);
        });
    }

    @Test
    void shouldCreateChannel() {
        final String channelName = "cbs";
        final String channelSenderName = channelName + ":sender";
        final String channelReceiverName = channelName + ":receiver";
        final Session qpidSession = mock(Session.class);
        final ArgumentCaptor<String> captor0 = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class);

        final Setup setup = Setup.create(qpidSession);
        final ProtonSession session = setup.getSession();

        StepVerifier.create(session.open()).verifyComplete();

        StepVerifier.create(session.channel(channelName, TIMEOUT)).expectNextCount(1).verifyComplete();

        verify(qpidSession).sender(captor0.capture());
        Assertions.assertEquals(channelSenderName, captor0.getValue());
        verify(qpidSession).receiver(captor1.capture());
        Assertions.assertEquals(channelReceiverName, captor1.getValue());
    }

    @Test
    void shouldBeginCloseClosesQPidSession() {
        final Session qpidSession = mock(Session.class);
        doNothing().when(qpidSession).open();
        doNothing().when(qpidSession).close();

        final Setup setup = Setup.create(qpidSession);
        final ProtonSession session = setup.getSession();

        StepVerifier.create(session.open()).verifyComplete();

        session.beginClose(null);

        verify(qpidSession, times(1)).close();
        Assertions.assertEquals(1, setup.getDispatchCount());
    }

    private static final class Setup {
        private static final String CONNECTION_ID = "contoso-connection-id";
        private static final String NAMESPACE = "contoso.servicebus.windows.net";
        private static final Duration OPEN_TIMEOUT = Duration.ZERO;
        private final int[] dispatchCount = new int[1];
        private final ProtonSession protonSession;

        static Setup create(Session qpidSession) {
            return new Setup(qpidSession, null);
        }

        static Setup create(Session qpidSession, Map<Integer, RejectedExecutionException> dispatchErrors) {
            return new Setup(qpidSession, dispatchErrors);
        }

        private Setup(Session qpidSession, Map<Integer, RejectedExecutionException> dispatchErrors) {
            final ReactorProvider reactorProvider = mock(ReactorProvider.class);
            final ReactorHandlerProvider handlerProvider = mock(ReactorHandlerProvider.class);
            final ReactorDispatcher reactorDispatcher = mock(ReactorDispatcher.class);
            final Connection connection = mock(Connection.class);
            final Record record = mock(Record.class);
            final String sessionName = "session0";
            try {
                doAnswer(invocation -> {
                    final int callCount = dispatchCount[0]++;
                    if (dispatchErrors != null && dispatchErrors.containsKey(callCount)) {
                        throw dispatchErrors.get(callCount);
                    } else {
                        final Runnable work = invocation.getArgument(0);
                        work.run();
                        return null;
                    }
                }).when(reactorDispatcher).invoke(any(Runnable.class));
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
            when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);
            when(handlerProvider.createSessionHandler(anyString(), anyString(), anyString(), any(Duration.class)))
                .thenReturn(new SessionHandler(CONNECTION_ID, NAMESPACE, sessionName, reactorDispatcher, OPEN_TIMEOUT,
                    AmqpMetricsProvider.noop()));
            when(connection.session()).thenReturn(qpidSession);
            when(qpidSession.attachments()).thenReturn(record);
            this.protonSession = new ProtonSession(CONNECTION_ID, NAMESPACE, connection, handlerProvider,
                reactorProvider, sessionName, OPEN_TIMEOUT, new ClientLogger(Setup.class));
        }

        ProtonSession getSession() {
            return protonSession;
        }

        int getDispatchCount() {
            return dispatchCount[0];
        }
    }
}
