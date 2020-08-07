// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.ReplayProcessor;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Request response tests.
 */
class RequestResponseChannelTest {
    private static final String CONNECTION_ID = "some-id";
    private static final String NAMESPACE = "test fqdn";
    private static final String LINK_NAME = "test-link-name";
    private static final String ENTITY_PATH = "test-entity-path";
    private static final Duration TIMEOUT = Duration.ofSeconds(23);

    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(TIMEOUT);
    private final DirectProcessor<Delivery> deliveryProcessor = DirectProcessor.create();
    private final FluxSink<Delivery> deliverySink = deliveryProcessor.sink();
    private final ReplayProcessor<EndpointState> endpointStateReplayProcessor = ReplayProcessor.cacheLast();

    @Mock
    private ReactorHandlerProvider handlerProvider;
    @Mock
    private ReactorProvider reactorProvider;
    @Mock
    private ReactorDispatcher reactorDispatcher;
    @Mock
    private ReceiveLinkHandler receiveLinkHandler;
    @Mock
    private SendLinkHandler sendLinkHandler;

    @Mock
    private Session session;
    @Mock
    private Sender sender;
    @Mock
    private Receiver receiver;
    @Mock
    private MessageSerializer serializer;
    @Captor
    private ArgumentCaptor<Runnable> dispatcherCaptor;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(10));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);

        when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);

        final Record record = mock(Record.class);
        when(sender.attachments()).thenReturn(record);
        when(receiver.attachments()).thenReturn(record);

        when(session.sender(LINK_NAME + ":sender")).thenReturn(sender);
        when(session.receiver(LINK_NAME + ":receiver")).thenReturn(receiver);

        when(handlerProvider.createReceiveLinkHandler(eq(CONNECTION_ID), eq(NAMESPACE), eq(LINK_NAME), eq(ENTITY_PATH)))
            .thenReturn(receiveLinkHandler);
        when(handlerProvider.createSendLinkHandler(CONNECTION_ID, NAMESPACE, LINK_NAME, ENTITY_PATH))
            .thenReturn(sendLinkHandler);

        FluxSink<EndpointState> sink1 = endpointStateReplayProcessor.sink();
        sink1.next(EndpointState.ACTIVE);
        when(receiveLinkHandler.getEndpointStates()).thenReturn(endpointStateReplayProcessor);
        when(receiveLinkHandler.getErrors()).thenReturn(Flux.never());
        when(receiveLinkHandler.getDeliveredMessages()).thenReturn(deliveryProcessor);

        when(sendLinkHandler.getEndpointStates()).thenReturn(endpointStateReplayProcessor);
        when(sendLinkHandler.getErrors()).thenReturn(Flux.never());
    }

    @AfterEach
    void afterEach() {
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Validate that this gets and sets properties correctly.
     */
    @Test
    void getsProperties() {
        // Arrange
        SenderSettleMode settleMode = SenderSettleMode.SETTLED;
        ReceiverSettleMode receiverSettleMode = ReceiverSettleMode.SECOND;
        AmqpErrorContext expected = new AmqpErrorContext("namespace-test");
        when(receiveLinkHandler.getErrorContext(receiver)).thenReturn(expected);

        // Act
        final RequestResponseChannel channel = new RequestResponseChannel(CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, settleMode,
            receiverSettleMode);
        final AmqpErrorContext errorContext = channel.getErrorContext();

        channel.dispose();

        // Assert
        assertEquals(expected, errorContext);

        verify(sender).setTarget(argThat(t -> t != null && ENTITY_PATH.equals(t.getAddress())));
        verify(sender).setSenderSettleMode(settleMode);

        verify(receiver).setTarget(argThat(t -> t != null && t.getAddress().startsWith(ENTITY_PATH)));
        verify(receiver).setSource(argThat(s -> s != null && ENTITY_PATH.equals(s.getAddress())));
        verify(receiver).setReceiverSettleMode(receiverSettleMode);
    }

    @Test
    void disposes() {
        // Arrange
        final RequestResponseChannel channel = new RequestResponseChannel(CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.SECOND);

        // Act
        channel.dispose();

        // Assert
        verify(sender).close();
        verify(receiver).close();
    }

    /**
     * Verifies error when sending with null
     */
    @Test
    void sendNull() {
        // Arrange
        final RequestResponseChannel channel = new RequestResponseChannel(CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.SECOND);

        // Act & Assert
        StepVerifier.create(channel.sendWithAck(null))
            .expectError(NullPointerException.class)
            .verify();
    }

    /**
     * Verifies error when sending with a reply to property set.
     */
    @Test
    void sendReplyToSet() {
        // Arrange
        final RequestResponseChannel channel = new RequestResponseChannel(CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.SECOND);
        final Message message = mock(Message.class);
        when(message.getReplyTo()).thenReturn("test-reply-to");

        // Act & Assert
        StepVerifier.create(channel.sendWithAck(message))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    /**
     * Verifies error when sending with message id is set.
     */
    @Test
    void sendMessageIdSet() {
        // Arrange
        final RequestResponseChannel channel = new RequestResponseChannel(CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.SECOND);
        final Message message = mock(Message.class);
        when(message.getMessageId()).thenReturn(10L);

        // Act & Assert
        StepVerifier.create(channel.sendWithAck(message))
            .expectError(IllegalArgumentException.class)
            .verify();
    }
    /**
     * Verifies a message is received.
     */
    @Test
    void sendMessageWithTransaction() throws IOException {
        // Arrange
        // This message was copied from one that was received.
        TransactionalState transactionalState = new TransactionalState();
        transactionalState.setTxnId(new Binary("1".getBytes()));

        final byte[] messageBytes = new byte[]{0, 83, 115, -64, 15, 13, 64, 64, 64, 64, 64, 83, 1, 64, 64, 64, 64, 64,
            64, 64, 0, 83, 116, -63, 49, 4, -95, 11, 115, 116, 97, 116, 117, 115, 45, 99, 111, 100, 101, 113, 0, 0, 0,
            -54, -95, 18, 115, 116, 97, 116, 117, 115, 45, 100, 101, 115, 99, 114, 105, 112, 116, 105, 111, 110, -95, 8,
            65, 99, 99, 101, 112, 116, 101, 100};
        final RequestResponseChannel channel = new RequestResponseChannel(CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.SECOND);
        final UnsignedLong messageId = UnsignedLong.valueOf(1);
        final Message message = mock(Message.class);
        final int encodedSize = 143;
        when(serializer.getSize(message)).thenReturn(150);
        when(message.encode(any(), eq(0), anyInt())).thenReturn(encodedSize);
        when(message.getCorrelationId()).thenReturn(messageId);
        // Creating delivery for sending.
        final Delivery deliveryToSend = mock(Delivery.class);
        doNothing().when(deliveryToSend).setMessageFormat(anyInt());
        doNothing().when(deliveryToSend).disposition(any(TransactionalState.class));
        when(sender.delivery(any(byte[].class))).thenReturn(deliveryToSend);

        // Creating a received message because we decodeDelivery calls implementation details for proton-j.
        final Delivery delivery = mock(Delivery.class);
        when(delivery.pending()).thenReturn(messageBytes.length);
        when(receiver.recv(any(), eq(0), eq(messageBytes.length))).thenAnswer(invocation -> {
            final byte[] buffer = invocation.getArgument(0);
            System.arraycopy(messageBytes, 0, buffer, 0, messageBytes.length);
            return messageBytes.length;
        });

        // Act
        StepVerifier.create(channel.sendWithAck(message, transactionalState))
            .then(() -> deliverySink.next(delivery))
            .assertNext(received -> assertEquals(messageId, received.getCorrelationId()))
            .verifyComplete();

        // Getting the runnable so we can manually invoke it and verify contents are correct.
        verify(reactorDispatcher, atLeastOnce()).invoke(dispatcherCaptor.capture());
        dispatcherCaptor.getAllValues().forEach(work -> {
            assertNotNull(work);
            work.run();
        });

        // Assert
        verify(message).setMessageId(argThat(e -> e instanceof UnsignedLong && messageId.equals(e)));
        verify(message).setReplyTo(argThat(path -> path != null && path.startsWith(ENTITY_PATH)));

        verify(deliveryToSend).disposition(same(transactionalState));

        verify(receiver).flow(1);
        verify(sender).delivery(any());
        verify(sender).send(any(), eq(0), eq(encodedSize));
        verify(sender).advance();
    }

    /**
     * Verifies a message is received.
     */
    @Test
    void sendMessage() throws IOException {
        // Arrange
        // This message was copied from one that was received.
        final byte[] messageBytes = new byte[]{0, 83, 115, -64, 15, 13, 64, 64, 64, 64, 64, 83, 1, 64, 64, 64, 64, 64,
            64, 64, 0, 83, 116, -63, 49, 4, -95, 11, 115, 116, 97, 116, 117, 115, 45, 99, 111, 100, 101, 113, 0, 0, 0,
            -54, -95, 18, 115, 116, 97, 116, 117, 115, 45, 100, 101, 115, 99, 114, 105, 112, 116, 105, 111, 110, -95, 8,
            65, 99, 99, 101, 112, 116, 101, 100};
        final RequestResponseChannel channel = new RequestResponseChannel(CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.SECOND);
        final UnsignedLong messageId = UnsignedLong.valueOf(1);
        final Message message = mock(Message.class);
        final int encodedSize = 143;
        when(serializer.getSize(message)).thenReturn(150);
        when(message.encode(any(), eq(0), anyInt())).thenReturn(encodedSize);
        when(message.getCorrelationId()).thenReturn(messageId);

        // Creating delivery for sending.
        final Delivery deliveryToSend = mock(Delivery.class);
        doNothing().when(deliveryToSend).setMessageFormat(anyInt());
        when(sender.delivery(any(byte[].class))).thenReturn(deliveryToSend);

        // Creating a received message because we decodeDelivery calls implementation details for proton-j.
        final Delivery delivery = mock(Delivery.class);
        when(delivery.pending()).thenReturn(messageBytes.length);
        when(receiver.recv(any(), eq(0), eq(messageBytes.length))).thenAnswer(invocation -> {
            final byte[] buffer = invocation.getArgument(0);
            System.arraycopy(messageBytes, 0, buffer, 0, messageBytes.length);
            return messageBytes.length;
        });

        // Act
        StepVerifier.create(channel.sendWithAck(message))
            .then(() -> deliverySink.next(delivery))
            .assertNext(received -> assertEquals(messageId, received.getCorrelationId()))
            .verifyComplete();

        // Getting the runnable so we can manually invoke it and verify contents are correct.
        verify(reactorDispatcher, atLeastOnce()).invoke(dispatcherCaptor.capture());
        dispatcherCaptor.getAllValues().forEach(work -> {
            assertNotNull(work);
            work.run();
        });

        // Assert
        verify(message).setMessageId(argThat(e -> e instanceof UnsignedLong && messageId.equals(e)));
        verify(message).setReplyTo(argThat(path -> path != null && path.startsWith(ENTITY_PATH)));

        verify(receiver).flow(1);
        verify(sender).delivery(any());
        verify(sender).send(any(), eq(0), eq(encodedSize));
        verify(sender).advance();
    }

    @Test
    void clearMessagesOnError() {
        // Arrange
        final RequestResponseChannel channel = new RequestResponseChannel(CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.SECOND);
        final AmqpException error = new AmqpException(true, "Message", new AmqpErrorContext("some-context"));
        final Message message = mock(Message.class);
        when(serializer.getSize(message)).thenReturn(150);
        when(message.encode(any(), eq(0), anyInt())).thenReturn(143);

        // Act
        StepVerifier.create(channel.sendWithAck(message))
            .then(() -> endpointStateReplayProcessor.sink().error(error))
            .verifyError(AmqpException.class);

        // Assert
        assertTrue(channel.isDisposed());
    }

}
