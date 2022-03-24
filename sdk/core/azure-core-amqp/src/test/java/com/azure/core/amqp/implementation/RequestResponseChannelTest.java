// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpShutdownSignal;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Request response tests.
 */
class RequestResponseChannelTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(10);
    private static final String CONNECTION_ID = "some-id";
    private static final String NAMESPACE = "test fqdn";
    private static final String LINK_NAME = "test-link-name";
    private static final String ENTITY_PATH = "test-entity-path";
    private static final Duration TRY_TIMEOUT = Duration.ofSeconds(23);

    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(TRY_TIMEOUT);
    private final TestPublisher<Delivery> deliveryProcessor = TestPublisher.createCold();
    private final TestPublisher<EndpointState> receiveEndpoints = TestPublisher.createCold();
    private final TestPublisher<EndpointState> sendEndpoints = TestPublisher.createCold();
    private final TestPublisher<AmqpShutdownSignal> shutdownSignals = TestPublisher.create();

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
    private AmqpConnection amqpConnection;

    @Mock
    private Session session;
    @Mock
    private Sender sender;
    @Mock
    private Receiver receiver;
    @Mock
    private MessageSerializer serializer;
    @Mock
    private Delivery delivery;

    private AutoCloseable mocksCloseable;

    @BeforeEach
    void beforeEach() throws IOException {
        mocksCloseable = MockitoAnnotations.openMocks(this);

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

        when(receiveLinkHandler.getEndpointStates()).thenReturn(receiveEndpoints.flux());
        when(receiveLinkHandler.getDeliveredMessages()).thenReturn(deliveryProcessor.flux());
        when(sendLinkHandler.getEndpointStates()).thenReturn(sendEndpoints.flux());

        when(amqpConnection.getShutdownSignals()).thenReturn(shutdownSignals.flux());

        when(sender.delivery(any())).thenReturn(delivery);

        doAnswer(invocation -> {
            final Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));
    }

    @AfterEach
    void afterEach() throws Exception {
        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
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

        receiveEndpoints.next(EndpointState.ACTIVE);
        sendEndpoints.next(EndpointState.ACTIVE);

        // Act
        final RequestResponseChannel channel = new RequestResponseChannel(amqpConnection, CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, settleMode,
            receiverSettleMode);
        final AmqpErrorContext errorContext = channel.getErrorContext();

        StepVerifier.create(channel.closeAsync())
            .then(() -> {
                sendEndpoints.complete();
                receiveEndpoints.complete();
            })
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        // Assert
        assertEquals(expected, errorContext);

        verify(sender).setTarget(argThat(t -> t != null && ENTITY_PATH.equals(t.getAddress())));
        verify(sender).setSenderSettleMode(settleMode);

        verify(receiver).setTarget(argThat(t -> t != null && t.getAddress().startsWith(ENTITY_PATH)));
        verify(receiver).setSource(argThat(s -> s != null && ENTITY_PATH.equals(s.getAddress())));
        verify(receiver).setReceiverSettleMode(receiverSettleMode);
    }

    @Test
    void disposeAsync() {
        // Arrange
        final RequestResponseChannel channel = new RequestResponseChannel(amqpConnection, CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.SECOND);

        receiveEndpoints.next(EndpointState.ACTIVE);
        sendEndpoints.next(EndpointState.ACTIVE);

        // Act
        StepVerifier.create(channel.closeAsync())
            .then(() -> {
                sendEndpoints.complete();
                receiveEndpoints.complete();
            })
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        // Assert
        verify(sender).close();
        verify(receiver).close();

        assertTrue(channel.isDisposed());
    }

    @Test
    void dispose() throws IOException {
        // Arrange
        final RequestResponseChannel channel = new RequestResponseChannel(amqpConnection, CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.SECOND);

        receiveEndpoints.next(EndpointState.ACTIVE);
        sendEndpoints.next(EndpointState.ACTIVE);

        doAnswer(invocation -> {
            sendEndpoints.complete();
            return null;
        }).when(sender).close();

        doAnswer(invocation -> {
            receiveEndpoints.complete();
            return null;
        }).when(receiver).close();

        doAnswer(invocation -> {
            final Runnable work = invocation.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        // Act
        channel.closeAsync().block();

        // Assert
        verify(sender).close();
        verify(receiver).close();

        assertTrue(channel.isDisposed());
    }

    /**
     * Verifies error when sending with null
     */
    @Test
    void sendNull() {
        // Arrange
        final RequestResponseChannel channel = new RequestResponseChannel(amqpConnection, CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.SECOND);

        receiveEndpoints.next(EndpointState.ACTIVE);
        sendEndpoints.next(EndpointState.ACTIVE);

        // Act & Assert
        StepVerifier.create(channel.sendWithAck(null))
            .expectError(NullPointerException.class)
            .verify(VERIFY_TIMEOUT);
    }

    /**
     * Verifies error when sending with a reply to property set.
     */
    @Test
    void sendReplyToSet() {
        // Arrange
        final RequestResponseChannel channel = new RequestResponseChannel(amqpConnection, CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.SECOND);
        final Message message = mock(Message.class);
        when(message.getReplyTo()).thenReturn("test-reply-to");

        // Act & Assert
        StepVerifier.create(channel.sendWithAck(message))
            .expectError(IllegalArgumentException.class)
            .verify(VERIFY_TIMEOUT);
    }

    /**
     * Verifies error when sending with message id is set.
     */
    @Test
    void sendMessageIdSet() {
        // Arrange
        final RequestResponseChannel channel = new RequestResponseChannel(amqpConnection, CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.SECOND);
        final Message message = mock(Message.class);
        when(message.getMessageId()).thenReturn(10L);

        receiveEndpoints.next(EndpointState.ACTIVE);
        sendEndpoints.next(EndpointState.ACTIVE);

        // Act & Assert
        StepVerifier.create(channel.sendWithAck(message))
            .expectError(IllegalArgumentException.class)
            .verify(VERIFY_TIMEOUT);
    }

    /**
     * Verifies a message is received.
     */
    @Test
    void sendMessageWithTransaction() {
        // Arrange
        // This message was copied from one that was received.
        TransactionalState transactionalState = new TransactionalState();
        transactionalState.setTxnId(new Binary("1".getBytes()));

        final byte[] messageBytes = new byte[]{0, 83, 115, -64, 15, 13, 64, 64, 64, 64, 64, 83, 1, 64, 64, 64, 64, 64,
            64, 64, 0, 83, 116, -63, 49, 4, -95, 11, 115, 116, 97, 116, 117, 115, 45, 99, 111, 100, 101, 113, 0, 0, 0,
            -54, -95, 18, 115, 116, 97, 116, 117, 115, 45, 100, 101, 115, 99, 114, 105, 112, 116, 105, 111, 110, -95, 8,
            65, 99, 99, 101, 112, 116, 101, 100};
        final RequestResponseChannel channel = new RequestResponseChannel(amqpConnection, CONNECTION_ID, NAMESPACE,
            LINK_NAME, ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer,
            SenderSettleMode.SETTLED, ReceiverSettleMode.SECOND);
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

        receiveEndpoints.next(EndpointState.ACTIVE);
        sendEndpoints.next(EndpointState.ACTIVE);

        // Act
        StepVerifier.create(channel.sendWithAck(message, transactionalState))
            .then(() -> deliveryProcessor.next(delivery))
            .assertNext(received -> assertEquals(messageId, received.getCorrelationId()))
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

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
    void sendMessage() {
        // Arrange
        // This message was copied from one that was received.
        final byte[] messageBytes = new byte[]{0, 83, 115, -64, 15, 13, 64, 64, 64, 64, 64, 83, 1, 64, 64, 64, 64, 64,
            64, 64, 0, 83, 116, -63, 49, 4, -95, 11, 115, 116, 97, 116, 117, 115, 45, 99, 111, 100, 101, 113, 0, 0, 0,
            -54, -95, 18, 115, 116, 97, 116, 117, 115, 45, 100, 101, 115, 99, 114, 105, 112, 116, 105, 111, 110, -95, 8,
            65, 99, 99, 101, 112, 116, 101, 100};
        final RequestResponseChannel channel = new RequestResponseChannel(amqpConnection, CONNECTION_ID, NAMESPACE,
            LINK_NAME, ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer,
            SenderSettleMode.SETTLED, ReceiverSettleMode.SECOND);
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

        receiveEndpoints.next(EndpointState.ACTIVE);
        sendEndpoints.next(EndpointState.ACTIVE);

        // Act
        StepVerifier.create(channel.sendWithAck(message))
            .then(() -> deliveryProcessor.next(delivery))
            .assertNext(received -> assertEquals(messageId, received.getCorrelationId()))
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

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
        final RequestResponseChannel channel = new RequestResponseChannel(amqpConnection, CONNECTION_ID, NAMESPACE, LINK_NAME,
            ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.SECOND);
        final AmqpException error = new AmqpException(true, "Message", new AmqpErrorContext("some-context"));
        final Message message = mock(Message.class);
        when(serializer.getSize(message)).thenReturn(150);
        when(message.encode(any(), eq(0), anyInt())).thenReturn(143);

        receiveEndpoints.next(EndpointState.ACTIVE);
        sendEndpoints.next(EndpointState.ACTIVE);

        // Act
        StepVerifier.create(channel.sendWithAck(message))
            .then(() -> sendEndpoints.error(error))
            .expectError(AmqpException.class)
            .verify(VERIFY_TIMEOUT);

        // Assert
        assertTrue(channel.isDisposed());
    }

    /**
     * Verifies that when an exception occurs in the parent, the connection is also closed.
     */
    @Test
    void parentDisposesConnection() {
        // Arrange
        final RequestResponseChannel channel = new RequestResponseChannel(amqpConnection, CONNECTION_ID, NAMESPACE,
            LINK_NAME, ENTITY_PATH, session, retryOptions, handlerProvider, reactorProvider, serializer,
            SenderSettleMode.SETTLED, ReceiverSettleMode.SECOND);
        final AmqpShutdownSignal shutdownSignal = new AmqpShutdownSignal(false, false, "Test-shutdown-signal");

        doAnswer(invocationOnMock -> {
            sendEndpoints.complete();
            return null;
        }).when(sender).close();

        doAnswer(invocationOnMock -> {
            receiveEndpoints.complete();
            return null;
        }).when(receiver).close();

        // Act
        shutdownSignals.next(shutdownSignal);

        // We are in the process of disposing.
        assertTrue(channel.isDisposed());

        // This turns it into a synchronous operation so we know that it is disposed completely.
        channel.closeAsync().block();

        // Assert
        verify(receiver).close();
        verify(sender).close();

        receiveEndpoints.assertNoSubscribers();
        sendEndpoints.assertNoSubscribers();
        shutdownSignals.assertNoSubscribers();
    }

    /**
     * Verifies that closing times out and does not wait indefinitely.
     */
    @Test
    public void closeAsyncTimeout() {
        // Arrange
        final AmqpRetryOptions retry = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(1)).setMaxRetries(0);
        final RequestResponseChannel channel = new RequestResponseChannel(amqpConnection, CONNECTION_ID, NAMESPACE,
            LINK_NAME, ENTITY_PATH, session, retry, handlerProvider, reactorProvider, serializer,
            SenderSettleMode.SETTLED, ReceiverSettleMode.SECOND);

        // Act & Assert
        StepVerifier.create(channel.closeAsync())
            .thenAwait(retry.getTryTimeout())
            .expectComplete()
            .verify(Duration.ofSeconds(30));

        // Calling closeAsync() returns the same completed status.
        StepVerifier.create(channel.closeAsync())
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        // The last state would be uninitialised because we did not emit any state.
        StepVerifier.create(channel.getEndpointStates())
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        assertTrue(channel.isDisposed());
    }

    /**
     * Verifies that closing does not wait indefinitely.
     */
    @Test
    public void closeAsync() {
        // Arrange
        final AmqpRetryOptions retry = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(1)).setMaxRetries(0);
        final RequestResponseChannel channel = new RequestResponseChannel(amqpConnection, CONNECTION_ID, NAMESPACE,
            LINK_NAME, ENTITY_PATH, session, retry, handlerProvider, reactorProvider, serializer,
            SenderSettleMode.SETTLED, ReceiverSettleMode.SECOND);

        sendEndpoints.next(EndpointState.ACTIVE);
        receiveEndpoints.next(EndpointState.ACTIVE);

        doAnswer(invocationOnMock -> {
            sendEndpoints.complete();
            return null;
        }).when(sender).close();

        doAnswer(invocationOnMock -> {
            receiveEndpoints.complete();
            return null;
        }).when(receiver).close();

        // Act & Assert
        StepVerifier.create(channel.closeAsync())
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        // Calling closeAsync() returns the same completed status.
        StepVerifier.create(channel.closeAsync())
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        // The last endpoint we saw was active.
        StepVerifier.create(channel.getEndpointStates())
            .expectNext(AmqpEndpointState.ACTIVE)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        assertTrue(channel.isDisposed());
    }
}
