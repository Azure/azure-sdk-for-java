// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.LinkErrorContext;
import com.azure.core.amqp.implementation.handler.DeliverySettleMode;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler2;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.test.utils.metrics.TestMeasurement;
import com.azure.core.test.utils.metrics.TestMeter;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    private static final String ENTITY_NAME = "test";
    private static final String ENTITY_PATH = ENTITY_NAME + "/entity/path";
    private AutoCloseable mocksCloseable;

    @BeforeEach
    void beforeEach() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
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
        try (MockChannel innerChannel = new MockChannel(null, null)) {
            innerChannel.arrange();
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel();

            // Act  & Assert
            innerChannel.setActive();

            StepVerifier.create(channel.closeAsync()).expectComplete().verify(VERIFY_TIMEOUT);

            final AmqpErrorContext context = channel.getErrorContext();
            assertInstanceOf(LinkErrorContext.class, context);
            final LinkErrorContext linkErrorContext = (LinkErrorContext) context;
            assertEquals(LINK_NAME + ":receiver", linkErrorContext.getTrackingId());

            final Sender senderLink = innerChannel.getRequestLink();
            final Receiver receiverLink = innerChannel.getResponseLink();
            verify(senderLink).setTarget(argThat(t -> t != null && ENTITY_PATH.equals(t.getAddress())));
            verify(senderLink).setSenderSettleMode(SenderSettleMode.SETTLED);
            verify(receiverLink).setTarget(argThat(t -> t != null && t.getAddress().startsWith(ENTITY_PATH)));
            verify(receiverLink).setSource(argThat(s -> s != null && ENTITY_PATH.equals(s.getAddress())));
            verify(receiverLink).setReceiverSettleMode(ReceiverSettleMode.SECOND);
        }
    }

    @Test
    void disposeAsync() {
        try (MockChannel innerChannel = new MockChannel(null, null)) {
            innerChannel.arrange();
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel();

            // Act  & Assert
            innerChannel.setActive();

            StepVerifier.create(channel.closeAsync()).expectComplete().verify(VERIFY_TIMEOUT);

            final Sender senderLink = innerChannel.getRequestLink();
            final Receiver receiverLink = innerChannel.getResponseLink();
            verify(senderLink).close();
            verify(receiverLink).close();
            assertTrue(channel.isDisposed());
        }
    }

    @Test
    void dispose() throws IOException {
        try (MockChannel innerChannel = new MockChannel(null, null)) {
            innerChannel.arrange();
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel();

            // Act  & Assert
            innerChannel.setActive();

            channel.closeAsync().block();

            final Sender senderLink = innerChannel.getRequestLink();
            final Receiver receiverLink = innerChannel.getResponseLink();
            verify(senderLink).close();
            verify(receiverLink).close();
            assertTrue(channel.isDisposed());
        }
    }

    /**
     * Verifies error when sending with null
     */
    @Test
    void sendNull() {
        try (MockChannel innerChannel = new MockChannel(null, null)) {
            final Message message = mock(Message.class);
            when(message.getReplyTo()).thenReturn("test-reply-to");
            innerChannel.arrange();
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel();

            // Act & Assert
            innerChannel.setActive();

            StepVerifier.create(channel.sendWithAck(null))
                .expectError(NullPointerException.class)
                .verify(VERIFY_TIMEOUT);
        }
    }

    /**
     * Verifies error when sending with a reply to property set.
     */
    @Test
    void sendReplyToSet() {
        try (MockChannel innerChannel = new MockChannel(null, null)) {
            final Message message = mock(Message.class);
            when(message.getReplyTo()).thenReturn("test-reply-to");
            innerChannel.arrange();
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel();

            // Act & Assert
            innerChannel.setActive();

            StepVerifier.create(channel.sendWithAck(message))
                .expectError(IllegalArgumentException.class)
                .verify(VERIFY_TIMEOUT);
        }
    }

    /**
     * Verifies error when sending with message id is set.
     */
    @Test
    void sendMessageIdSet() {
        try (MockChannel innerChannel = new MockChannel(null, null)) {
            final Message message = mock(Message.class);
            when(message.getMessageId()).thenReturn(10L);
            innerChannel.arrange();
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel();

            // Act & Assert
            innerChannel.setActive();

            StepVerifier.create(channel.sendWithAck(message))
                .expectError(IllegalArgumentException.class)
                .verify(VERIFY_TIMEOUT);
        }
    }

    /**
     * Verifies a message is received in transaction mode.
     */
    @Test
    void sendMessageWithTransaction() {
        try (MockChannel.Request request = new MockChannel.Request();
            MockChannel.Response response = new MockChannel.Response();
            MockChannel innerChannel = new MockChannel(request, response)) {
            innerChannel.arrange();
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel();

            // Act
            innerChannel.setActive();

            final Message message = request.getMessage();
            final UnsignedLong messageId = request.getMessageId();
            final TransactionalState transactionalState = request.getTransactionalState();
            StepVerifier.create(channel.sendWithAck(message, transactionalState))
                .then(innerChannel::emitResponseMessage)
                .assertNext(received -> assertEquals(messageId, received.getCorrelationId()))
                .expectComplete()
                .verify(VERIFY_TIMEOUT);

            // Assert
            verify(message).setMessageId(argThat(e -> e instanceof UnsignedLong && messageId.equals(e)));
            verify(message).setReplyTo(argThat(path -> path != null && path.startsWith(ENTITY_PATH)));

            final Delivery delivery = request.getDelivery();
            final Receiver receiveLink = innerChannel.getResponseLink();
            final Sender senderLink = innerChannel.getRequestLink();
            verify(delivery).disposition(same(transactionalState));
            verify(receiveLink).flow(1);
            verify(senderLink).delivery(any());
            verify(senderLink).send(any(), eq(0), eq(request.getEncodedSize()));
            verify(senderLink).advance();
        }
    }

    /**
     * Verifies a message is received.
     */
    @Test
    void sendMessage() {
        try (MockChannel.Request request = new MockChannel.Request();
            MockChannel.Response response = new MockChannel.Response();
            MockChannel innerChannel = new MockChannel(request, response)) {
            innerChannel.arrange();
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel();

            // Act
            innerChannel.setActive();

            final Message message = request.getMessage();
            final UnsignedLong messageId = request.getMessageId();
            StepVerifier.create(channel.sendWithAck(message))
                .then(innerChannel::emitResponseMessage)
                .assertNext(received -> assertEquals(messageId, received.getCorrelationId()))
                .expectComplete()
                .verify(VERIFY_TIMEOUT);

            // Assert
            verify(message).setMessageId(argThat(e -> e instanceof UnsignedLong && messageId.equals(e)));
            verify(message).setReplyTo(argThat(path -> path != null && path.startsWith(ENTITY_PATH)));

            final Receiver receiveLink = innerChannel.getResponseLink();
            final Sender senderLink = innerChannel.getRequestLink();
            verify(receiveLink).flow(1);
            verify(senderLink).delivery(any());
            verify(senderLink).send(any(), eq(0), eq(request.getEncodedSize()));
            verify(senderLink).advance();
        }
    }

    /**
     * Verifies a message send duration is recorded.
     */
    @Test
    void sendMessageWithMetrics() {
        try (MockChannel.Request request = new MockChannel.Request();
            MockChannel.Response response = new MockChannel.Response();
            MockChannel innerChannel = new MockChannel(request, response)) {
            final TestMeter meter = new TestMeter();
            final AmqpMetricsProvider metricsProvider = new AmqpMetricsProvider(meter, NAMESPACE, ENTITY_PATH);
            innerChannel.arrange();
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel(metricsProvider);

            // Act
            innerChannel.setActive();

            final long start = Instant.now().toEpochMilli();
            final Message message = request.getMessage();
            final UnsignedLong messageId = request.getMessageId();
            StepVerifier.create(channel.sendWithAck(message))
                .then(innerChannel::emitResponseMessage)
                .assertNext(received -> assertEquals(messageId, received.getCorrelationId()))
                .expectComplete()
                .verify(VERIFY_TIMEOUT);

            // Assert
            List<TestMeasurement<Double>> durations
                = meter.getHistograms().get("messaging.az.amqp.management.request.duration").getMeasurements();
            assertEquals(1, durations.size());
            final TestMeasurement<Double> duration = durations.get(0);
            assertTrue(Instant.now().toEpochMilli() - start >= duration.getValue());
            assertTrue(duration.getValue() >= 0, "Expected positive or null, got - " + duration);
            assertEquals("accepted", duration.getAttributes().get(AmqpMetricsProvider.STATUS_CODE_KEY));
            assertEquals(NAMESPACE, duration.getAttributes().get(ClientConstants.HOSTNAME_KEY));
            assertEquals(ENTITY_NAME, duration.getAttributes().get(ClientConstants.ENTITY_NAME_KEY));
            assertEquals(ENTITY_PATH, duration.getAttributes().get(ClientConstants.ENTITY_PATH_KEY));
        }
    }

    /**
     * Verifies a message send duration is recorded when exception is thrown during send
     */
    @Test
    void sendMessageSendErrorWithMetrics() {
        try (MockChannel.Request request = new MockChannel.Request();
            MockChannel innerChannel = new MockChannel(request, null)) {
            final TestMeter meter = new TestMeter();
            final AmqpMetricsProvider metricsProvider = new AmqpMetricsProvider(meter, NAMESPACE, ENTITY_PATH);
            innerChannel.arrange(new RejectedExecutionException("send-error"));
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel(metricsProvider);

            // Act
            innerChannel.setActive();

            long start = Instant.now().toEpochMilli();
            final Message message = request.getMessage();
            StepVerifier.create(channel.sendWithAck(message)).expectErrorMessage("send-error").verify(VERIFY_TIMEOUT);

            // Assert
            List<TestMeasurement<Double>> durations
                = meter.getHistograms().get("messaging.az.amqp.management.request.duration").getMeasurements();
            assertEquals(1, durations.size());
            final TestMeasurement<Double> duration = durations.get(0);
            assertTrue(Instant.now().toEpochMilli() - start >= duration.getValue());
            assertTrue(duration.getValue() >= 0, "Expected positive or null, got - " + duration);
            assertEquals("error", duration.getAttributes().get(AmqpMetricsProvider.STATUS_CODE_KEY));
            assertEquals(NAMESPACE, duration.getAttributes().get(ClientConstants.HOSTNAME_KEY));
            assertEquals(ENTITY_NAME, duration.getAttributes().get(ClientConstants.ENTITY_NAME_KEY));
            assertEquals(ENTITY_PATH, duration.getAttributes().get(ClientConstants.ENTITY_PATH_KEY));
        }
    }

    /**
     * Verifies a message send duration is recorded.
     */
    @Test
    void sendMessageEndpointErrorWithMetrics() {
        final String operationName = AmqpConstants.VENDOR + ":renew-lock";
        final ApplicationProperties props
            = new ApplicationProperties(Collections.singletonMap("operation", operationName));
        try (MockChannel.Request request = new MockChannel.Request(props);
            MockChannel innerChannel = new MockChannel(request, null)) {
            final TestMeter meter = new TestMeter();
            final AmqpMetricsProvider metricsProvider = new AmqpMetricsProvider(meter, NAMESPACE, ENTITY_PATH);
            final AmqpException error = new AmqpException(true, "Message", new AmqpErrorContext("some-context"));
            innerChannel.arrange();
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel(metricsProvider);

            // Act
            innerChannel.setActive();

            final long start = Instant.now().toEpochMilli();
            final Message message = request.getMessage();
            StepVerifier.create(channel.sendWithAck(message))
                .then(() -> innerChannel.setRequestLinkError(error))
                .expectErrorSatisfies(e -> assertEquals(error, e))
                .verify(VERIFY_TIMEOUT);

            // Assert
            List<TestMeasurement<Double>> durations
                = meter.getHistograms().get("messaging.az.amqp.management.request.duration").getMeasurements();
            assertEquals(1, durations.size());
            final TestMeasurement<Double> duration = durations.get(0);
            assertTrue(Instant.now().toEpochMilli() - start >= duration.getValue());
            assertTrue(duration.getValue() >= 0, "Expected positive or null, got - " + duration);
            assertEquals("error", duration.getAttributes().get(AmqpMetricsProvider.STATUS_CODE_KEY));
            assertEquals(NAMESPACE, duration.getAttributes().get(ClientConstants.HOSTNAME_KEY));
            assertEquals(ENTITY_NAME, duration.getAttributes().get(ClientConstants.ENTITY_NAME_KEY));
            assertEquals(ENTITY_PATH, duration.getAttributes().get(ClientConstants.ENTITY_PATH_KEY));
            assertEquals(operationName, duration.getAttributes().get(ClientConstants.OPERATION_NAME_KEY));
        }
    }

    @Test
    void propagatesSendLinkError() {
        try (MockChannel.Request request = new MockChannel.Request();
            MockChannel innerChannel = new MockChannel(request, null)) {
            final AmqpException error = new AmqpException(true, "Message", new AmqpErrorContext("some-context"));
            innerChannel.arrange();
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel();

            // Act
            innerChannel.setActive();

            final Message message = request.getMessage();
            StepVerifier.create(channel.sendWithAck(message))
                .then(() -> innerChannel.setRequestLinkError(error))
                .expectErrorSatisfies(e -> assertEquals(error, e))
                .verify(VERIFY_TIMEOUT);

            // Assert
            assertTrue(channel.isDisposed());
        }
    }

    /**
     * Verifies that when an exception occurs in the parent, the connection is also closed.
     */
    @Test
    void connectionShutdownDisposesChannel() {
        try (MockChannel innerChannel = new MockChannel(null, null)) {
            final AmqpShutdownSignal shutdownSignal
                = new AmqpShutdownSignal(false, false, "connection-shutdown-signal");
            final TestPublisher<AmqpShutdownSignal> shutdownSignals = TestPublisher.create();
            innerChannel.arrange();
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel(shutdownSignals);

            // Act
            shutdownSignals.next(shutdownSignal);

            // We are in the process of disposing.
            assertTrue(channel.isDisposed());

            // This turns it into a synchronous operation so we know that it is disposed completely.
            channel.closeAsync().block();

            // Assert
            final Receiver receiveLink = innerChannel.getResponseLink();
            final Sender senderLink = innerChannel.getRequestLink();
            verify(receiveLink).close();
            verify(senderLink).close();
            innerChannel.assertNoEndpointSubscribers();
            shutdownSignals.assertNoSubscribers();
        }
    }

    /**
     * Verifies that closing times out and does not wait indefinitely.
     */
    @Test
    public void closeAsyncTimeout() {
        try (MockChannel innerChannel = new MockChannel(null, null)) {
            final AmqpRetryOptions retry = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(1)).setMaxRetries(0);
            innerChannel.arrange(false);
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel(retry);

            // Act & Assert
            StepVerifier.create(channel.closeAsync())
                .thenAwait(retry.getTryTimeout())
                .expectComplete()
                .verify(Duration.ofSeconds(30));

            // Calling closeAsync() returns the same completed status.
            StepVerifier.create(channel.closeAsync()).expectComplete().verify(VERIFY_TIMEOUT);

            // The last state would be uninitialised because we did not emit any state.
            StepVerifier.create(channel.getEndpointStates()).expectComplete().verify(VERIFY_TIMEOUT);

            assertTrue(channel.isDisposed());
        }
    }

    /**
     * Verifies that closing does not wait indefinitely.
     */
    @Test
    public void closeAsync() {
        try (MockChannel innerChannel = new MockChannel(null, null)) {
            final AmqpRetryOptions retry = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(1)).setMaxRetries(0);
            innerChannel.arrange();
            final RequestResponseChannel channel = innerChannel.wrapInRRChannel(retry);

            // Act & Assert
            innerChannel.setActive();
            StepVerifier.create(channel.closeAsync()).expectComplete().verify(VERIFY_TIMEOUT);

            // Calling closeAsync() returns the same completed status.
            StepVerifier.create(channel.closeAsync()).expectComplete().verify(VERIFY_TIMEOUT);

            // The last endpoint we saw was active.
            StepVerifier.create(channel.getEndpointStates())
                .expectNext(AmqpEndpointState.ACTIVE)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);

            assertTrue(channel.isDisposed());
        }
    }

    // A mocked inner request-response channel that is aware of sending of a request object and receiving of a response object specified in the Ctr.
    private static final class MockChannel implements AutoCloseable {
        private static final AmqpRetryOptions RETRY_OPTIONS = new AmqpRetryOptions();
        private final Request request;
        private final Response response;
        private final MessageSerializer serializer;
        private final ReactorDispatcher reactorDispatcher;
        private final ReactorProvider reactorProvider;
        private final ReactorHandlerProvider handlerProvider;
        private final SendLinkHandler requestLinkHandler;
        private final ReceiveLinkHandler2 responseLinkHandler;
        private final Sender requestLink;
        private final Receiver responseLink;
        private final Record record;
        private final TestPublisher<EndpointState> requestEndpointStates = TestPublisher.createCold();
        private final Event responseLinkOpenEvent;

        MockChannel(Request request, Response response) {
            this.request = request;
            this.response = response;
            this.serializer = mock(MessageSerializer.class);
            this.reactorDispatcher = mock(ReactorDispatcher.class);
            this.reactorProvider = mock(ReactorProvider.class);
            this.handlerProvider = mock(ReactorHandlerProvider.class);
            this.requestLinkHandler = mock(SendLinkHandler.class);
            this.requestLink = mock(Sender.class);
            this.responseLinkHandler = createReceiveHandler();
            this.responseLink = mock(Receiver.class);
            this.record = mock(Record.class);
            this.responseLinkOpenEvent = mock(Event.class);
        }

        void arrange() {
            arrangeIntern(null, true);
        }

        void arrange(Throwable requestError) {
            arrangeIntern(requestError, true);
        }

        void arrange(boolean completeEndpointsOnLinkClose) {
            arrangeIntern(null, completeEndpointsOnLinkClose);
        }

        private void arrangeIntern(Throwable requestError, boolean completeEndpointsOnLinkClose) {
            if (request != null) {
                request.arrange(serializer);
            }
            if (response != null) {
                response.arrange(responseLink);
            }
            try {
                doAnswer(invocation -> {
                    final Runnable runnable = invocation.getArgument(0);
                    runnable.run();
                    return null;
                }).when(reactorDispatcher).invoke(any(Runnable.class));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);
            when(requestLinkHandler.getEndpointStates()).thenReturn(requestEndpointStates.flux());
            when(handlerProvider.createSendLinkHandler(CONNECTION_ID, NAMESPACE, LINK_NAME, ENTITY_PATH))
                .thenReturn(requestLinkHandler);
            when(handlerProvider.createReceiveLinkHandler(eq(CONNECTION_ID), eq(NAMESPACE), eq(LINK_NAME),
                eq(ENTITY_PATH), eq(DeliverySettleMode.ACCEPT_AND_SETTLE_ON_DELIVERY), eq(false), eq(reactorDispatcher),
                any(AmqpRetryOptions.class))).thenReturn(responseLinkHandler);
            when(responseLink.getName()).thenReturn(LINK_NAME + ":receiver");
            when(responseLink.getRemoteSource()).thenReturn(new Source());
            when(responseLinkOpenEvent.getLink()).thenReturn(responseLink);
            //
            when(requestLink.getName()).thenReturn(LINK_NAME + ":sender");
            when(requestLink.attachments()).thenReturn(record);
            if (requestError != null) {
                when(requestLink.delivery(any(byte[].class))).thenThrow(requestError);
            } else {
                if (request != null) {
                    when(requestLink.delivery(any(byte[].class))).thenReturn(request.getDelivery());
                }
            }
            doAnswer(invocation -> {
                if (completeEndpointsOnLinkClose) {
                    requestEndpointStates.complete();
                }
                return null;
            }).when(requestLink).close();
            //
            when(responseLink.attachments()).thenReturn(record);
            if (response != null) {
                final byte[] messageBytes = response.getMessageBytes();
                when(responseLink.recv(any(), eq(0), eq(messageBytes.length))).thenAnswer(invocation -> {
                    final byte[] buffer = invocation.getArgument(0);
                    System.arraycopy(messageBytes, 0, buffer, 0, messageBytes.length);
                    return messageBytes.length;
                });
            }
            doAnswer(invocation -> {
                if (completeEndpointsOnLinkClose) {
                    responseLinkHandler.onLinkFinal(null);
                }
                return null;
            }).when(responseLink).close();
        }

        RequestResponseChannel wrapInRRChannel() {
            return wrapInRRChannel(mockConnection(null), AmqpMetricsProvider.noop(), RETRY_OPTIONS);
        }

        RequestResponseChannel wrapInRRChannel(AmqpRetryOptions retryOptions) {
            return wrapInRRChannel(mockConnection(null), AmqpMetricsProvider.noop(), retryOptions);
        }

        RequestResponseChannel wrapInRRChannel(AmqpMetricsProvider metricsProvider) {
            return wrapInRRChannel(mockConnection(null), metricsProvider, RETRY_OPTIONS);
        }

        RequestResponseChannel wrapInRRChannel(TestPublisher<AmqpShutdownSignal> connectionShutdownSignals) {
            return wrapInRRChannel(mockConnection(connectionShutdownSignals), AmqpMetricsProvider.noop(),
                RETRY_OPTIONS);
        }

        private RequestResponseChannel wrapInRRChannel(AmqpConnection amqpConnection,
            AmqpMetricsProvider metricsProvider, AmqpRetryOptions retryOptions) {
            return new RequestResponseChannel(amqpConnection, CONNECTION_ID, NAMESPACE, ENTITY_PATH,
                new ProtonSession.ProtonChannel(LINK_NAME, requestLink, responseLink), retryOptions, handlerProvider,
                reactorProvider, serializer, SenderSettleMode.SETTLED, ReceiverSettleMode.SECOND, metricsProvider,
                true);
        }

        private AmqpConnection mockConnection(TestPublisher<AmqpShutdownSignal> shutdownSignals) {
            final AmqpConnection connection = mock(AmqpConnection.class);
            if (shutdownSignals == null) {
                when(connection.getShutdownSignals()).thenReturn(Flux.never());
            } else {
                when(connection.getShutdownSignals()).thenReturn(shutdownSignals.flux());
            }
            return connection;
        }

        Sender getRequestLink() {
            return requestLink;
        }

        Receiver getResponseLink() {
            return responseLink;
        }

        void setActive() {
            requestEndpointStates.next(EndpointState.ACTIVE);            // set request link active
            responseLinkHandler.onLinkRemoteOpen(responseLinkOpenEvent); // set response link active
        }

        void setRequestLinkError(Throwable e) {
            requestEndpointStates.error(e);
        }

        void emitResponseMessage() {
            responseLinkHandler.onDelivery(response.getQPidDeliveryEvent());
        }

        void assertNoEndpointSubscribers() {
            requestEndpointStates.assertNoSubscribers();
            // receiveEndpoint subscription cannot be verified because it is managed by the handler.
        }

        private ReceiveLinkHandler2 createReceiveHandler() {
            return new ReceiveLinkHandler2(CONNECTION_ID, NAMESPACE, LINK_NAME, ENTITY_PATH,
                DeliverySettleMode.ACCEPT_AND_SETTLE_ON_DELIVERY, reactorDispatcher, RETRY_OPTIONS, false,
                AmqpMetricsProvider.noop());
        }

        @Override
        public void close() {
            Mockito.framework().clearInlineMock(this);
        }

        static final class Request implements AutoCloseable {
            private final ApplicationProperties properties;
            private final UnsignedLong messageId = UnsignedLong.valueOf(1);
            private final TransactionalState transactionalState;
            private final int encodedSize = 143;
            private final Message message;
            // the 'Delivery' object to return upon attempt to encode 'message' bytes via requestLink.delivery(bytes[]).
            private final Delivery delivery;

            Request() {
                this.transactionalState = new TransactionalState();
                this.transactionalState.setTxnId(new Binary("1".getBytes()));
                this.message = mock(Message.class);
                this.delivery = mock(Delivery.class);
                this.properties = null;
            }

            Request(ApplicationProperties properties) {
                this.transactionalState = new TransactionalState();
                this.transactionalState.setTxnId(new Binary("1".getBytes()));
                this.message = mock(Message.class);
                this.delivery = mock(Delivery.class);
                this.properties = properties;
            }

            void arrange(MessageSerializer serializer) {
                when(serializer.getSize(message)).thenReturn(150);
                when(message.encode(any(), eq(0), anyInt())).thenReturn(encodedSize);
                when(message.getCorrelationId()).thenReturn(messageId);
                if (properties != null) {
                    when(message.getApplicationProperties()).thenReturn(properties);
                }
                doNothing().when(delivery).setMessageFormat(anyInt());
                doNothing().when(delivery).disposition(any(TransactionalState.class));
            }

            TransactionalState getTransactionalState() {
                return transactionalState;
            }

            UnsignedLong getMessageId() {
                return messageId;
            }

            int getEncodedSize() {
                return encodedSize;
            }

            Message getMessage() {
                return message;
            }

            Delivery getDelivery() {
                return delivery;
            }

            @Override
            public void close() {
                Mockito.framework().clearInlineMock(this);
            }
        }

        static final class Response implements AutoCloseable {
            private final byte[] messageBytes = new byte[] {
                0,
                83,
                115,
                -64,
                15,
                13,
                64,
                64,
                64,
                64,
                64,
                83,
                1,
                64,
                64,
                64,
                64,
                64,
                64,
                64,
                0,
                83,
                116,
                -63,
                49,
                4,
                -95,
                11,
                115,
                116,
                97,
                116,
                117,
                115,
                45,
                99,
                111,
                100,
                101,
                113,
                0,
                0,
                0,
                -54,
                -95,
                18,
                115,
                116,
                97,
                116,
                117,
                115,
                45,
                100,
                101,
                115,
                99,
                114,
                105,
                112,
                116,
                105,
                111,
                110,
                -95,
                8,
                65,
                99,
                99,
                101,
                112,
                116,
                101,
                100 };
            private final Delivery delivery;
            private final Event deliveryEvent;

            Response() {
                this.delivery = mock(Delivery.class);
                this.deliveryEvent = mock(Event.class);
            }

            void arrange(Receiver responseLink) {
                when(delivery.pending()).thenReturn(messageBytes.length);
                when(delivery.getLink()).thenReturn(responseLink);
                when(deliveryEvent.getDelivery()).thenReturn(delivery);
            }

            byte[] getMessageBytes() {
                return messageBytes;
            }

            Event getQPidDeliveryEvent() {
                return deliveryEvent;
            }

            @Override
            public void close() {
                Mockito.framework().clearInlineMock(this);
            }
        }
    }
}
