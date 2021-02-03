// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

    @Captor
    private ArgumentCaptor<Runnable> dispatcherCaptor;
    @Captor
    private  ArgumentCaptor<DeliveryState> deliveryStateArgumentCaptor;

    private Message message;
    private AmqpRetryOptions options;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        Delivery delivery = mock(Delivery.class);
        when(delivery.getRemoteState()).thenReturn(Accepted.getInstance());
        when(delivery.getTag()).thenReturn("tag".getBytes());
        when(handler.getDeliveredMessages()).thenReturn(Flux.just(delivery));
        when(reactor.selectable()).thenReturn(selectable);

        when(handler.getLinkCredits()).thenReturn(Flux.just(100));

        final ReplayProcessor<EndpointState> endpointStateReplayProcessor = ReplayProcessor.cacheLast();
        when(handler.getEndpointStates()).thenReturn(endpointStateReplayProcessor);
        FluxSink<EndpointState> sink1 = endpointStateReplayProcessor.sink();
        sink1.next(EndpointState.ACTIVE);

        when(tokenManager.getAuthorizationResults()).thenReturn(Flux.just(AmqpResponseCode.ACCEPTED));
        when(sender.getCredit()).thenReturn(100);
        when(sender.advance()).thenReturn(true);
        doNothing().when(selectable).setChannel(any());
        doNothing().when(selectable).onReadable(any());
        doNothing().when(selectable).onFree(any());
        doNothing().when(selectable).setReading(true);
        doNothing().when(reactor).update(selectable);

        ReactorDispatcher reactorDispatcher = new ReactorDispatcher(reactor);
        when(reactor.attachments()).thenReturn(new Record() {
            @Override
            public <T> T get(Object o, Class<T> aClass) {
                return null;
            }

            @Override
            public <T> void set(Object o, Class<T> aClass, T t) {
            }

            @Override
            public void clear() {

            }
        });
        when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);
        when(sender.getRemoteMaxMessageSize()).thenReturn(UnsignedLong.valueOf(1000));

        options = new AmqpRetryOptions()
            .setTryTimeout(Duration.ofSeconds(1))
            .setMode(AmqpRetryMode.EXPONENTIAL);

        message = Proton.message();
        message.setMessageId("id");
        message.setBody(new AmqpValue("hello"));
    }

    @AfterEach
    void teardown() {
        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void testLinkSize() {
        final ReactorSender reactorSender = new ReactorSender(ENTITY_PATH, sender, handler, reactorProvider,
            tokenManager, messageSerializer, options);

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

        final ReactorSender reactorSender = new ReactorSender(ENTITY_PATH, sender, handler, reactorProvider,
            tokenManager, messageSerializer, options);
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
        final ReactorSender reactorSender = new ReactorSender(ENTITY_PATH, sender, handler, reactorProvider,
            tokenManager, messageSerializer, options);
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

        final ReactorSender reactorSender = new ReactorSender(ENTITY_PATH, sender, handler, reactorProvider,
            tokenManager, messageSerializer, options);

        final ReactorDispatcher reactorDispatcherMock = mock(ReactorDispatcher.class);
        when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcherMock);
        doNothing().when(reactorDispatcherMock).invoke(any(Runnable.class));

        // Creating delivery for sending.
        final Delivery deliveryToSend = mock(Delivery.class);
        doNothing().when(deliveryToSend).setMessageFormat(anyInt());
        doNothing().when(deliveryToSend).disposition(deliveryStateArgumentCaptor.capture());
        when(sender.delivery(any(byte[].class))).thenReturn(deliveryToSend);

        // Act
        reactorSender.send(message, transactionalState).subscribe();

        verify(reactorDispatcherMock).invoke(dispatcherCaptor.capture());

        List<Runnable> invocations = dispatcherCaptor.getAllValues();

        // Apply the invocation.
        invocations.get(0).run();

        // Assert
        DeliveryState deliveryState = deliveryStateArgumentCaptor.getValue();
        Assertions.assertSame(transactionalState, deliveryState);
        verify(sender).getRemoteMaxMessageSize();
        verify(sender).advance();
    }

    @Test
    public void testSend() {
        // Arrange
        final ReactorSender reactorSender = new ReactorSender(ENTITY_PATH, sender, handler, reactorProvider,
            tokenManager, messageSerializer, options);
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

        final ReactorSender reactorSender = new ReactorSender(ENTITY_PATH, sender, handler, reactorProvider,
            tokenManager, messageSerializer, options);
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

        final ReactorSender reactorSender = new ReactorSender(ENTITY_PATH, sender, handler, reactorProvider,
            tokenManager, messageSerializer, options);
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
}
