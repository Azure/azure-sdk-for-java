// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.exception.OperationCancelledException;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReactorSender}
 */
public class ReactorSenderTest {
    private static final String ENTITY_PATH = "entity-path";
    private final TestPublisher<AmqpShutdownSignal> shutdownSignals = TestPublisher.createCold();
    private final TestPublisher<EndpointState> endpointStatePublisher = TestPublisher.createCold();

    @Mock
    private AmqpConnection amqpConnection;
    @Mock
    private Sender sender;
    @Mock
    private SendLinkHandler handler;
    @Mock
    private ReactorProvider reactorProvider;
    @Mock
    private TokenManager tokenManager;
    @Mock
    private Reactor reactor;
    @Mock
    private Selectable selectable;
    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private TransactionalState transactionalState;
    @Mock
    private ReactorDispatcher reactorDispatcher;
    @Mock
    private Scheduler scheduler;

    @Captor
    private  ArgumentCaptor<DeliveryState> deliveryStateArgumentCaptor;

    private final TestPublisher<AmqpResponseCode> authorizationResults = TestPublisher.createCold();
    private Message message;
    private AmqpRetryOptions options;
    private AutoCloseable mocksCloseable;
    private ReactorSender reactorSender;

    @BeforeEach
    public void setup() throws IOException {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        doAnswer(invocation -> {
            final Runnable argument = invocation.getArgument(0);
            argument.run();
            return null;
        }).when(scheduler).schedule(any(Runnable.class));
        when(amqpConnection.getShutdownSignals()).thenReturn(shutdownSignals.flux());

        Delivery delivery = mock(Delivery.class);
        when(delivery.getRemoteState()).thenReturn(Accepted.getInstance());
        when(delivery.getTag()).thenReturn("tag".getBytes());
        when(handler.getDeliveredMessages()).thenReturn(Flux.just(delivery));
        when(reactor.selectable()).thenReturn(selectable);

        when(handler.getLinkCredits()).thenReturn(Flux.just(100));

        when(handler.getEndpointStates()).thenReturn(endpointStatePublisher.flux());
        endpointStatePublisher.next(EndpointState.ACTIVE);

        when(tokenManager.getAuthorizationResults()).thenReturn(authorizationResults.flux());
        authorizationResults.next(AmqpResponseCode.ACCEPTED);

        when(sender.getCredit()).thenReturn(100);
        when(sender.advance()).thenReturn(true);
        doNothing().when(selectable).setChannel(any());
        doNothing().when(selectable).onReadable(any());
        doNothing().when(selectable).onFree(any());
        doNothing().when(selectable).setReading(true);
        doNothing().when(reactor).update(selectable);

        when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);
        when(sender.getRemoteMaxMessageSize()).thenReturn(UnsignedLong.valueOf(1000));

        options = new AmqpRetryOptions()
            .setTryTimeout(Duration.ofSeconds(2))
            .setMode(AmqpRetryMode.EXPONENTIAL);

        message = Proton.message();
        message.setMessageId("id");
        message.setBody(new AmqpValue("hello"));
    }

    @AfterEach
    public void teardown() throws Exception {
        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMocks();

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @BeforeAll
    public static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(10));
    }

    @AfterAll
    public static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @Test
    public void testLinkSize() {
        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);

        StepVerifier.create(reactorSender.getLinkSize())
            .expectNext(1000)
            .verifyComplete();
        StepVerifier.create(reactorSender.getLinkSize())
            .expectNext(1000)
            .verifyComplete();

        verify(sender).getRemoteMaxMessageSize();
    }

    @Test
    public void testSendWithTransactionFailed() {
        // Arrange
        final String exceptionString = "fake exception";

        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);
        final ReactorSender spyReactorSender = spy(reactorSender);

        final Throwable exception = new RuntimeException(exceptionString);
        doReturn(Mono.error(exception)).when(spyReactorSender).send(any(byte[].class), anyInt(), anyInt(),
            eq(transactionalState));

        // Act
        StepVerifier.create(spyReactorSender.send(message, transactionalState))
            .verifyErrorMessage(exceptionString);

        // Assert
        verify(sender, times(1)).getRemoteMaxMessageSize();
        verify(spyReactorSender).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT),
            eq(transactionalState));
    }

    /**
     * Testing that we can send message with transaction.
     */
    @Test
    public void testSendWithTransaction() {
        // Arrange
        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);
        final ReactorSender spyReactorSender = spy(reactorSender);

        doReturn(Mono.empty()).when(spyReactorSender).send(any(byte[].class), anyInt(), anyInt(),
            eq(transactionalState));

        // Act
        StepVerifier.create(spyReactorSender.send(message, transactionalState))
            .verifyComplete();
        StepVerifier.create(spyReactorSender.send(message, transactionalState))
            .verifyComplete();

        // Assert
        verify(sender).getRemoteMaxMessageSize();
        verify(spyReactorSender, times(2)).send(any(byte[].class), anyInt(),
            eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), eq(transactionalState));
    }

    /**
     * Testing that we can send message with transaction.
     */
    @Test
    public void testSendWithTransactionDeliverySet() throws IOException {
        // Arrange
        // This is specific to this message and needs to align with this message.
        when(sender.send(any(byte[].class), anyInt(), anyInt())).thenReturn(26);
        doAnswer(invocationOnMock -> {
            final Runnable argument = invocationOnMock.getArgument(0);
            argument.run();
            return null;
        }).when(scheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);

        // Creating delivery for sending.
        final Delivery deliveryToSend = mock(Delivery.class);
        doNothing().when(deliveryToSend).setMessageFormat(anyInt());
        doNothing().when(deliveryToSend).disposition(deliveryStateArgumentCaptor.capture());
        when(sender.delivery(any(byte[].class))).thenReturn(deliveryToSend);

        doAnswer(invocationOnMock -> {
            final Runnable work = invocationOnMock.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        // Act
        StepVerifier.create(reactorSender.send(message, transactionalState))
            .expectError(AmqpException.class) // Because we did not process a "delivered message", it'll timeout.
            .verify();

        // Assert
        DeliveryState deliveryState = deliveryStateArgumentCaptor.getValue();
        Assertions.assertSame(transactionalState, deliveryState);
        verify(sender).getRemoteMaxMessageSize();
        verify(sender).advance();
    }

    @Test
    public void testSend() {
        // Arrange
        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);
        final ReactorSender spyReactorSender = spy(reactorSender);

        doReturn(Mono.empty()).when(spyReactorSender).send(any(byte[].class), anyInt(), anyInt(), isNull());

        // Act
        StepVerifier.create(spyReactorSender.send(message))
            .verifyComplete();
        StepVerifier.create(spyReactorSender.send(message))
            .verifyComplete();

        // Assert
        verify(sender).getRemoteMaxMessageSize();
        verify(spyReactorSender, times(2)).send(any(byte[].class), anyInt(), anyInt(), isNull());
    }

    @Test
    public void testSendBatch() {
        // Arrange
        final Message message2 = Proton.message();
        message2.setMessageId("id2");
        message2.setBody(new AmqpValue("world"));

        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);
        final ReactorSender spyReactorSender = spy(reactorSender);

        doReturn(Mono.empty()).when(spyReactorSender).send(any(byte[].class), anyInt(), anyInt(), isNull());

        // Act
        StepVerifier.create(spyReactorSender.send(Arrays.asList(message, message2)))
            .verifyComplete();
        StepVerifier.create(spyReactorSender.send(Arrays.asList(message, message2)))
            .verifyComplete();

        // Assert
        verify(sender, times(1)).getRemoteMaxMessageSize();
        verify(spyReactorSender, times(2)).send(any(byte[].class), anyInt(), anyInt(), isNull());
    }

    @Test
    public void testLinkSizeSmallerThanMessageSize() {
        // Arrange
        when(sender.getRemoteMaxMessageSize()).thenReturn(UnsignedLong.valueOf(10));

        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);
        final ReactorSender spyReactorSender = spy(reactorSender);

        doReturn(Mono.empty()).when(spyReactorSender).send(any(byte[].class), anyInt(), anyInt(), isNull());

        // Act
        StepVerifier.create(spyReactorSender.send(message))
            .verifyErrorSatisfies(throwable -> {
                Assertions.assertTrue(throwable instanceof AmqpException);
                Assertions.assertTrue(throwable.getMessage().startsWith("Error sending. Size of the payload exceeded "
                    + "maximum message size"));
            });

        // Assert
        verify(sender, times(1)).getRemoteMaxMessageSize();
        verify(spyReactorSender, times(0)).send(any(byte[].class), anyInt(), anyInt(), isNull());
    }

    /**
     * Verifies that when an exception occurs in the parent, the connection is also closed.
     */
    @Test
    void parentDisposesConnection() throws IOException {
        // Arrange
        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);
        final AmqpShutdownSignal shutdownSignal = new AmqpShutdownSignal(false, false, "Test-shutdown-signal");

        doAnswer(invocationOnMock -> {
            endpointStatePublisher.complete();
            return null;
        }).when(sender).close();

        doAnswer(invocationOnMock -> {
            final Runnable work = invocationOnMock.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        // Act
        shutdownSignals.next(shutdownSignal);

        // Assert
        assertTrue(reactorSender.isDisposed());

        verify(sender).close();

        endpointStatePublisher.assertNoSubscribers();
        shutdownSignals.assertNoSubscribers();
    }

    /**
     * Verifies that when an exception occurs in the parent, the endpoints are also disposed.
     */
    @Test
    void parentClosesEndpoint() throws IOException {
        // Arrange
        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);
        final AmqpShutdownSignal shutdownSignal = new AmqpShutdownSignal(false, false, "Test-shutdown-signal");

        doAnswer(invocationOnMock -> {
            endpointStatePublisher.complete();
            return null;
        }).when(sender).close();

        doAnswer(invocationOnMock -> {
            final Runnable work = invocationOnMock.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        // Act
        StepVerifier.create(reactorSender.getEndpointStates())
            .expectNext(AmqpEndpointState.ACTIVE)
            .then(() -> shutdownSignals.next(shutdownSignal))
            .expectComplete()
            .verify();

        // Assert
        assertTrue(reactorSender.isDisposed());

        verify(sender).close();

        endpointStatePublisher.assertNoSubscribers();
        shutdownSignals.assertNoSubscribers();
    }

    /**
     * An error in the handler will also close the sender.
     */
    @Test
    void disposesOnHandlerError() {
        // Arrange
        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);
        final UnsupportedOperationException testException = new UnsupportedOperationException("test-exception");

        // Act and Assert
        StepVerifier.create(reactorSender.getEndpointStates())
            .expectNext(AmqpEndpointState.ACTIVE)
            .then(() -> {
                endpointStatePublisher.error(testException);
            })
            .expectError(UnsupportedOperationException.class)
            .verify();

        // Expect that this Mono has completed.
        StepVerifier.create(reactorSender.isClosed())
            .expectComplete()
            .verify();

        assertTrue(reactorSender.isDisposed());

        endpointStatePublisher.assertNoSubscribers();
        shutdownSignals.assertNoSubscribers();
    }

    /**
     * A complete in the handler will also close the sender.
     */
    @Test
    void disposesOnHandlerComplete() throws IOException {
        // Arrange
        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);

        doAnswer(invocationOnMock -> {
            final Runnable work = invocationOnMock.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        // Act and Assert
        StepVerifier.create(reactorSender.getEndpointStates())
            .expectNext(AmqpEndpointState.ACTIVE)
            .then(() -> endpointStatePublisher.complete())
            .verifyComplete();

        // Expect that this Mono has completed.
        StepVerifier.create(reactorSender.isClosed())
            .expectComplete()
            .verify();

        assertTrue(reactorSender.isDisposed());

        verify(tokenManager).close();

        endpointStatePublisher.assertNoSubscribers();
        shutdownSignals.assertNoSubscribers();
    }

    @Test
    void disposeCompletes() throws IOException {
        // Arrange
        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);
        final String message = "some-message";
        final AmqpErrorCondition errorCondition = AmqpErrorCondition.UNAUTHORIZED_ACCESS;
        final ErrorCondition condition = new ErrorCondition(Symbol.getSymbol(errorCondition.getErrorCondition()),
            "Test-users");

        doAnswer(invocationOnMock -> {
            endpointStatePublisher.complete();
            return null;
        }).when(sender).close();

        doAnswer(invocationOnMock -> {
            final Runnable work = invocationOnMock.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        // Act
        StepVerifier.create(reactorSender.closeAsync(message, condition))
            .expectComplete()
            .verify();

        // Expect the same outcome.
        StepVerifier.create(reactorSender.closeAsync("something", null))
            .expectComplete()
            .verify();

        // Assert
        assertTrue(reactorSender.isDisposed());

        verify(sender).setCondition(condition);
        verify(sender).close();
        verify(tokenManager).close();

        endpointStatePublisher.assertNoSubscribers();
        shutdownSignals.assertNoSubscribers();
    }

    /**
     * When errors happen on the send link handler, any pending sends error out.
     */
    @Test
    void pendingMessagesError() throws IOException {
        // Arrange
        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);
        final UnsupportedOperationException testException = new UnsupportedOperationException("test-exception");
        final Message message = Proton.message();
        final UnsignedLong size = new UnsignedLong(2048L);
        when(sender.getRemoteMaxMessageSize()).thenReturn(size);

        doAnswer(invocationOnMock -> {
            endpointStatePublisher.error(testException);
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        // Act and Assert
        StepVerifier.create(reactorSender.send(message))
            .then(() -> {
            })
            .expectError(UnsupportedOperationException.class)
            .verify();

        // Expect that this Mono has completed.
        StepVerifier.create(reactorSender.isClosed())
            .expectComplete()
            .verify();

        assertTrue(reactorSender.isDisposed());

        endpointStatePublisher.assertNoSubscribers();
        shutdownSignals.assertNoSubscribers();
    }

    /**
     * When errors happen on the send link handler, any pending sends error out.
     */
    @Test
    void pendingMessagesErrorWithShutdown() throws IOException {
        // Arrange
        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);
        final Message message = Proton.message();
        final UnsignedLong size = new UnsignedLong(2048L);
        when(sender.getRemoteMaxMessageSize()).thenReturn(size);

        final AmqpShutdownSignal shutdownSignal = new AmqpShutdownSignal(false, false, "Test-shutdown-signal");

        final AtomicBoolean isEmitted = new AtomicBoolean();
        doAnswer(invocationOnMock -> {
            if (isEmitted.getAndSet(true)) {
                final Runnable runnable = invocationOnMock.getArgument(0);
                runnable.run();
            } else {
                shutdownSignals.next(shutdownSignal);
            }
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        doAnswer(invocationOnMock -> {
            endpointStatePublisher.complete();
            return null;
        }).when(sender).close();

        // Act and Assert
        StepVerifier.create(reactorSender.send(message))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof AmqpException);

                final AmqpException amqpException = (AmqpException) error;
                assertTrue(amqpException.isTransient());
                assertTrue(amqpException.getMessage().contains("not complete sends"));
            })
            .verify();

        assertTrue(reactorSender.isDisposed());

        endpointStatePublisher.assertNoSubscribers();
        shutdownSignals.assertNoSubscribers();
    }

    @Test
    void closesWhenNoLongerAuthorized() throws IOException {
        // Arrange
        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);
        final AmqpException error = new AmqpException(false, AmqpErrorCondition.ILLEGAL_STATE, "not-allowed",
            new AmqpErrorContext("foo-bar"));

        final Message message = mock(Message.class);

        doAnswer(invocationOnMock -> {
            final Runnable work = invocationOnMock.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        // Act

        // Assert and Act
        StepVerifier.create(reactorSender.send(message))
            .then(() -> authorizationResults.error(error))
            .expectError(OperationCancelledException.class)
            .verify();
    }

    @Test
    void closesWhenAuthorizationResultsComplete() throws IOException {
        // Arrange
        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, options, scheduler);

        doAnswer(invocationOnMock -> {
            final Runnable work = invocationOnMock.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        doAnswer(invocationOnMock -> {
            endpointStatePublisher.complete();
            return null;
        }).when(sender).close();

        // Assert and Act
        StepVerifier.create(reactorSender.send(message))
            .then(() -> authorizationResults.complete())
            .expectError(OperationCancelledException.class)
            .verify();
    }

    @Test
    public void sendWorkTimeout() throws IOException {
        // Arrange
        final AmqpRetryOptions noRetryOptions = new AmqpRetryOptions()
            .setTryTimeout(Duration.ofSeconds(2))
            .setMaxRetries(0);
        final long milliseconds = noRetryOptions.getTryTimeout().toMillis();

        doAnswer(invocationOnMock -> {
            final Runnable runnable = invocationOnMock.getArgument(0);
            runnable.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        doAnswer(invocation -> {
            System.out.println("Running send timeout work.");

            final Runnable argument = invocation.getArgument(0);
            argument.run();
            return null;
        }).when(scheduler).schedule(any(Runnable.class), eq(milliseconds), eq(TimeUnit.MILLISECONDS));

        final Delivery delivery = mock(Delivery.class);
        when(sender.delivery(any())).thenReturn(delivery);
        when(sender.advance()).thenReturn(true);
        when(sender.send(any(), eq(0), anyInt())).thenAnswer(invocation -> invocation.getArgument(2));


        reactorSender = new ReactorSender(amqpConnection, ENTITY_PATH, sender, handler,
            reactorProvider, tokenManager, messageSerializer, noRetryOptions, scheduler);

        // Act
        StepVerifier.create(reactorSender.send(message))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof AmqpException);
                assertEquals(AmqpErrorCondition.TIMEOUT_ERROR, ((AmqpException) error).getErrorCondition());
            })
            .verify();

        // Assert
        verify(sender).getRemoteMaxMessageSize();
        verify(scheduler).schedule(any(Runnable.class), eq(milliseconds), eq(TimeUnit.MILLISECONDS));
    }
}
