// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpLinkProvider;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorConnectionCache;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.amqp.implementation.ReactorExecutor;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpLinkProvider;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorAmqpConnection;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusSenderInstrumentation;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.core.amqp.implementation.RetryUtil.getRetryPolicy;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.OPERATION_TIMEOUT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class ServiceBusSenderAsyncClientRecoveryIsolatedTest {
    private static final BinaryData TEST_CONTENTS = BinaryData.fromString("My message for service bus queue!");
    private static final String FQDN = "contoso-shopping.servicebus.windows.net";
    private static final String QUEUE_NAME = "orders";
    private static final String CLIENT_IDENTIFIER = "client-identifier";
    private static final ServiceBusSenderInstrumentation DEFAULT_INSTRUMENTATION = new ServiceBusSenderInstrumentation(
        null, null, FQDN, QUEUE_NAME);
    private static final Duration VIRTUAL_TIME_SHIFT = OPERATION_TIMEOUT.plusSeconds(30);
    private static final AmqpException RETRIABLE_LINK_ERROR = new AmqpException(true, AmqpErrorCondition.LINK_DETACH_FORCED,
        "detach-link-error", new AmqpErrorContext(FQDN));
    private static final AmqpException RETRIABLE_SESSION_ERROR = new AmqpException(true, "session-error",
        new AmqpErrorContext(FQDN));
    private static final AmqpException RETRIABLE_CONNECTION_ERROR = new AmqpException(true, AmqpErrorCondition.CONNECTION_FORCED,
        "connection-forced-error", new AmqpErrorContext(FQDN));
    private static final AmqpException NON_RETRIABLE_ERROR_1 = new AmqpException(false, AmqpErrorCondition.NOT_ALLOWED,
        "not-allowed-error-1", new AmqpErrorContext(FQDN));
    private static final AmqpException NON_RETRIABLE_ERROR_2 = new AmqpException(false, AmqpErrorCondition.NOT_ALLOWED,
        "not-allowed-error-2", new AmqpErrorContext(FQDN));
    private static final AmqpRetryOptions RETRY_OPTIONS = new AmqpRetryOptions()
        .setMode(AmqpRetryMode.FIXED)
        .setMaxRetries(10)
        .setMaxDelay(Duration.ofSeconds(5))
        .setDelay(Duration.ofSeconds(1))
        .setTryTimeout(OPERATION_TIMEOUT);
    private final MessageSerializer messageSerializer = new ServiceBusMessageSerializer();
    @Mock
    private Runnable onClientClosed;
    @Captor
    private ArgumentCaptor<List<Message>> sendMessagesCaptor0;
    @Captor
    private ArgumentCaptor<List<Message>> sendMessagesCaptor1;

    private AutoCloseable mocksCloseable;

    @BeforeEach
    void setup() {
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
    @Execution(ExecutionMode.SAME_THREAD)
    void shouldRecoverFromRetriableSendLinkError() {
        // A Connection with one Session and two AmqpSendLink in that Session.
        final int sessionsCnt = 1;
        final int[] linksPerSession = new int[] { 2 };
        try (MockEndpoint endpoint = createMockEndpoint(sessionsCnt, linksPerSession)) {
            // The ServiceBusReactorAmqpConnection connection supplier for ReactorConnectionCache.
            final Supplier<ServiceBusReactorAmqpConnection> connectionSupplier = singleConnectionSupplier(endpoint);
            // Answers the invocation of 'send' API on two AmqpSendLink in the Session.
            final Answer<Mono<Void>> sendAnswer = new Answer<Mono<Void>>() {
                private int invocationCount = -1;

                @Override
                public Mono<Void> answer(InvocationOnMock invocation) {
                    invocationCount++;
                    switch (invocationCount) {
                        case 0:
                            // Replicate the arrival of transient-error in the first (current) AmqpSendLink on which 'send' API is invoked.
                            endpoint.emitCurrentSendLinkError(RETRIABLE_LINK_ERROR);
                            return Mono.error(RETRIABLE_LINK_ERROR);
                        case 1:
                            // Replicate the successful processing of 'send' API invocation on the second (current) AmqpSendLink.
                            return Mono.empty();
                        default:
                            throw new RuntimeException("More than two invocations of send-answer is not expected.");
                    }
                }
            };

            // Associate Answer to the two AmqpSendLink in the Session.
            when(endpoint.getAmqpSendLink(0, 0).send(anyList())).thenAnswer(sendAnswer);
            when(endpoint.getAmqpSendLink(0, 1).send(anyList())).thenAnswer(sendAnswer);

            final int messagesCount = 4;
            final List<ServiceBusMessage> messagesToSend = createMessagesToSend(messagesCount);

            final ReactorConnectionCache<ServiceBusReactorAmqpConnection> connectionCache = createConnectionCache(connectionSupplier);
            final ServiceBusSenderAsyncClient sender = createSenderAsyncClient(connectionCache, false);
            try {
                // The Producer.send that internally attempt to use the first AmqpSendLink, but upon transient-error
                // on the first link, the retry obtains and uses second AmqpSendLink.
                try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                    verifier.create(() -> sender.sendMessages(messagesToSend))
                        .thenAwait(VIRTUAL_TIME_SHIFT)
                        .verifyComplete();
                }

                // Verify that both the AmqpSendLinks were notified with the expected messages corresponding
                // to List<ServiceBusMessage>.
                verify(endpoint.getAmqpSendLink(0, 0)).send(sendMessagesCaptor0.capture());
                final List<Message> messagesSendInSession0Link0 = sendMessagesCaptor0.getValue();
                Assertions.assertEquals(messagesCount, messagesSendInSession0Link0.size());

                verify(endpoint.getAmqpSendLink(0, 1)).send(sendMessagesCaptor1.capture());
                final List<Message> messagesSendInSession0Link1 = sendMessagesCaptor1.getValue();
                Assertions.assertEquals(messagesCount, messagesSendInSession0Link1.size());
            } finally {
                sender.close();
                connectionCache.dispose();
            }
        }
    }


    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void shouldBubbleUpNonRetriableSendLinkError() {
        // A Connection with one Session and two AmqpSendLink in that Session.
        final int sessionsCnt = 1;
        final int[] linksPerSession = new int[] { 2 };
        try (MockEndpoint endpoint = createMockEndpoint(sessionsCnt, linksPerSession)) {
            // The ServiceBusReactorAmqpConnection connection supplier for ReactorConnectionCache.
            final Supplier<ServiceBusReactorAmqpConnection> connectionSupplier = singleConnectionSupplier(endpoint);
            // Answers the invocation of 'send' API on two AmqpSendLink in the Session.
            final Answer<Mono<Void>> sendAnswer = new Answer<Mono<Void>>() {
                private int invocationCount = -1;

                @Override
                public Mono<Void> answer(InvocationOnMock invocation) {
                    invocationCount++;
                    switch (invocationCount) {
                        case 0:
                            // Replicate the arrival of transient-error in the first (current) AmqpSendLink
                            // on which 'send' API is invoked.
                            endpoint.emitCurrentSendLinkError(RETRIABLE_LINK_ERROR);
                            return Mono.error(RETRIABLE_LINK_ERROR);
                        case 1:
                            // Replicate the arrival of non-transient-error in the second (current) AmqpSendLink
                            // on which 'send' API is invoked.
                            endpoint.emitCurrentSendLinkError(NON_RETRIABLE_ERROR_1);
                            return Mono.error(NON_RETRIABLE_ERROR_1);
                        default:
                            throw new RuntimeException("More than two invocations of send-answer is not expected.");
                    }
                }
            };

            // Associate Answer to the two AmqpSendLink in the Session.
            when(endpoint.getAmqpSendLink(0, 0).send(anyList())).thenAnswer(sendAnswer);
            when(endpoint.getAmqpSendLink(0, 1).send(anyList())).thenAnswer(sendAnswer);

            final int messagesCount = 4;
            final List<ServiceBusMessage> messagesToSend = createMessagesToSend(messagesCount);

            final ReactorConnectionCache<ServiceBusReactorAmqpConnection> connectionCache = createConnectionCache(connectionSupplier);
            final ServiceBusSenderAsyncClient sender = createSenderAsyncClient(connectionCache, false);
            try {
                // The Producer.send that internally attempt to use the first AmqpSendLink, but upon transient-error
                // on the first link, the retry obtains second AmqpSendLink, which has non-transient-error.
                try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                    verifier.create(() -> sender.sendMessages(messagesToSend))
                        .thenAwait(VIRTUAL_TIME_SHIFT)
                        .verifyErrorSatisfies(e -> {
                            Assertions.assertTrue(e instanceof ServiceBusException);
                            final ServiceBusException se = (ServiceBusException) e;
                            Assertions.assertSame(NON_RETRIABLE_ERROR_1, se.getCause());
                        });
                }

                // Verify that both the AmqpSendLinks were notified with the expected messages corresponding
                // to List<ServiceBusMessage>.
                verify(endpoint.getAmqpSendLink(0, 0)).send(sendMessagesCaptor0.capture());
                final List<Message> messagesSendInSession0Link0 = sendMessagesCaptor0.getValue();
                Assertions.assertEquals(messagesCount, messagesSendInSession0Link0.size());

                verify(endpoint.getAmqpSendLink(0, 1)).send(sendMessagesCaptor1.capture());
                final List<Message> messagesSendInSession0Link1 = sendMessagesCaptor1.getValue();
                Assertions.assertEquals(messagesCount, messagesSendInSession0Link1.size());

                verifyNoInteractions(onClientClosed);
            } finally {
                sender.close();
                connectionCache.dispose();
            }
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void shouldRecoverFromRetriableSessionError() {
        // A Connection with two Session and one AmqpSendLink per Session.
        final int sessionsCnt = 2;
        final int[] linksPerSession = new int[] { 1, 1 };
        try (MockEndpoint endpoint = createMockEndpoint(sessionsCnt, linksPerSession)) {
            // The ServiceBusReactorAmqpConnection connection supplier for ReactorConnectionCache.
            final Supplier<ServiceBusReactorAmqpConnection> connectionSupplier = singleConnectionSupplier(endpoint);
            // Answers the invocation of 'send' API on each AmqpSendLink in the two Session.
            final Answer<Mono<Void>> sendAnswer = new Answer<Mono<Void>>() {
                private int invocationCount = -1;

                @Override
                public Mono<Void> answer(InvocationOnMock invocation) {
                    invocationCount++;
                    switch (invocationCount) {
                        case 0:
                            // Replicate the arrival of transient-error in the first (current) Session hosting
                            // the AmqpSendLink on which 'send' API is invoked.
                            endpoint.emitCurrentSessionError(RETRIABLE_SESSION_ERROR);
                            return Mono.error(RETRIABLE_SESSION_ERROR);
                        case 1:
                            // Replicate the successful processing of 'send' API invocation on the AmqpSendLink
                            // in the second (current) Session.
                            return Mono.empty();
                        default:
                            throw new RuntimeException("More than two invocations of send-answer is not expected.");
                    }
                }
            };

            // Associate Answer to each AmqpSendLink in the two Session.
            when(endpoint.getAmqpSendLink(0, 0).send(anyList())).thenAnswer(sendAnswer);
            when(endpoint.getAmqpSendLink(1, 0).send(anyList())).thenAnswer(sendAnswer);

            final int messagesCount = 4;
            final List<ServiceBusMessage> messagesToSend = createMessagesToSend(messagesCount);

            final ReactorConnectionCache<ServiceBusReactorAmqpConnection> connectionCache = createConnectionCache(connectionSupplier);
            final ServiceBusSenderAsyncClient sender = createSenderAsyncClient(connectionCache, false);
            try {
                // The Producer.send that internally attempt to use AmqpSendLink in the first Session, but upon
                // transient-error on the first session, the retry obtains second Session and uses AmqpSendLink in it.
                try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                    verifier.create(() -> sender.sendMessages(messagesToSend))
                        .thenAwait(VIRTUAL_TIME_SHIFT)
                        .verifyComplete();
                }

                // Verify that both the AmqpSendLinks were notified with the expected messages corresponding
                // to List<ServiceBusMessage>.
                verify(endpoint.getAmqpSendLink(0, 0)).send(sendMessagesCaptor0.capture());
                final List<Message> messagesSendInSession0Link0 = sendMessagesCaptor0.getValue();
                Assertions.assertEquals(messagesCount, messagesSendInSession0Link0.size());

                verify(endpoint.getAmqpSendLink(1, 0)).send(sendMessagesCaptor1.capture());
                final List<Message> messagesSendInSession1Link0 = sendMessagesCaptor1.getValue();
                Assertions.assertEquals(messagesCount, messagesSendInSession1Link0.size());
            } finally {
                sender.close();
                connectionCache.dispose();
            }
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void shouldBubbleUpNonRetriableSessionError() {
        // A Connection with two Session and one AmqpSendLink per Session.
        final int sessionsCnt = 2;
        final int[] linksPerSession = new int[] { 1, 1 };
        try (MockEndpoint endpoint = createMockEndpoint(sessionsCnt, linksPerSession)) {
            // The EventHubReactorAmqpConnection connection supplier for ReactorConnectionCache.
            final Supplier<ServiceBusReactorAmqpConnection> connectionSupplier = singleConnectionSupplier(endpoint);
            // Answers the invocation of 'send' API on each AmqpSendLink in the two Session.
            final Answer<Mono<Void>> sendAnswer = new Answer<Mono<Void>>() {
                private int invocationCount = -1;

                @Override
                public Mono<Void> answer(InvocationOnMock invocation) {
                    invocationCount++;
                    switch (invocationCount) {
                        case 0:
                            // Replicate the arrival of transient-error in the first (current) Session hosting
                            // the AmqpSendLink on which 'send' API is invoked.
                            endpoint.emitCurrentSessionError(RETRIABLE_SESSION_ERROR);
                            return Mono.error(RETRIABLE_SESSION_ERROR);
                        case 1:
                            // Replicate the arrival of non-transient-error in the second (current) Session hosting
                            // the AmqpSendLink on which 'send' API is invoked.
                            endpoint.emitCurrentSessionError(NON_RETRIABLE_ERROR_1);
                            return Mono.error(NON_RETRIABLE_ERROR_1);
                        default:
                            throw new RuntimeException("More than two invocations of send-answer is not expected.");
                    }
                }
            };

            // Associate Answer to each AmqpSendLink in the two Session.
            when(endpoint.getAmqpSendLink(0, 0).send(anyList())).thenAnswer(sendAnswer);
            when(endpoint.getAmqpSendLink(1, 0).send(anyList())).thenAnswer(sendAnswer);

            final int messagesCount = 4;
            final List<ServiceBusMessage> messagesToSend = createMessagesToSend(messagesCount);

            final ReactorConnectionCache<ServiceBusReactorAmqpConnection> connectionCache = createConnectionCache(connectionSupplier);
            final ServiceBusSenderAsyncClient sender = createSenderAsyncClient(connectionCache, false);
            try {
                // The Producer.send that internally attempt to use AmqpSendLink in the first Session, but upon
                // transient-error on the first session, the retry obtains second Session which has non-transient error.
                try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                    verifier.create(() -> sender.sendMessages(messagesToSend))
                        .thenAwait(VIRTUAL_TIME_SHIFT)
                        .verifyErrorSatisfies(e -> {
                            Assertions.assertTrue(e instanceof ServiceBusException);
                            final ServiceBusException se = (ServiceBusException) e;
                            Assertions.assertSame(NON_RETRIABLE_ERROR_1, se.getCause());
                        });
                }

                // Verify that both the AmqpSendLinks were notified with the expected messages corresponding
                // to List<ServiceBusMessage>.
                verify(endpoint.getAmqpSendLink(0, 0)).send(sendMessagesCaptor0.capture());
                final List<Message> messagesSendInSession0Link0 = sendMessagesCaptor0.getValue();
                Assertions.assertEquals(messagesCount, messagesSendInSession0Link0.size());

                verify(endpoint.getAmqpSendLink(1, 0)).send(sendMessagesCaptor1.capture());
                final List<Message> messagesSendInSession1Link0 = sendMessagesCaptor1.getValue();
                Assertions.assertEquals(messagesCount, messagesSendInSession1Link0.size());

                verifyNoInteractions(onClientClosed);
            } finally {
                sender.close();
                connectionCache.dispose();
            }
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void shouldSenderReusableAfterNonRetriableLinkAndSessionError() {
        // A Connection with three Session and one AmqpSendLink per Session.
        final int sessionsCnt = 3;
        final int[] linksPerSession = new int[] { 1, 1, 1 };
        try (MockEndpoint endpoint = createMockEndpoint(sessionsCnt, linksPerSession)) {
            // The ServiceBusReactorAmqpConnection connection supplier for ReactorConnectionCache.
            final Supplier<ServiceBusReactorAmqpConnection> connectionSupplier = new Supplier<ServiceBusReactorAmqpConnection>() {
                private int invocationCount = -1;

                @Override
                public ServiceBusReactorAmqpConnection get() {
                    invocationCount++;
                    switch (invocationCount) {
                        case 0:
                            final ServiceBusReactorAmqpConnection c = endpoint.arrange();
                            return c;
                        default:
                            throw new RuntimeException("More than one invocation of connection-supplier is not expected.");
                    }
                }
            };

            // Answers the invocation of 'send' API on each AmqpSendLink in the two Session.
            final Answer<Mono<Void>> sendAnswer = new Answer<Mono<Void>>() {
                private int invocationCount = -1;

                @Override
                public Mono<Void> answer(InvocationOnMock invocation) {
                    invocationCount++;
                    switch (invocationCount) {
                        case 0:
                            // Replicate the arrival of non-transient-error in the current AmqpSendLink
                            // in the first session on which 'send' API is invoked.
                            endpoint.emitCurrentSendLinkError(NON_RETRIABLE_ERROR_1);
                            return Mono.error(NON_RETRIABLE_ERROR_1);
                        case 1:
                            // Replicate the arrival of non-transient-error in the second Session hosting
                            // the current AmqpSendLink on which 'send' API is invoked.
                            endpoint.emitCurrentSessionError(NON_RETRIABLE_ERROR_2);
                            return Mono.error(NON_RETRIABLE_ERROR_2);
                        case 2:
                            // Replicate the successful processing of 'send' API invocation on the current
                            // AmqpSendLink in the third Session.
                            return Mono.empty();
                        default:
                            throw new RuntimeException("More than three invocations of send-answer is not expected.");
                    }
                }
            };

            // Associate Answer to each AmqpSendLink in the three Session.
            // 1. The AmqpSendLink in the first Session on which 'send' API invoked with Flux<EventData>.
            when(endpoint.getAmqpSendLink(0, 0).send(anyList())).thenAnswer(sendAnswer);
            // 2. The AmqpSendLink in the second Session on which 'send' API invoked with Flux<EventData>.
            when(endpoint.getAmqpSendLink(1, 0).send(anyList())).thenAnswer(sendAnswer);
            // 2. The AmqpSendLink in the third Session on which 'send' API invoked with EventData.
            when(endpoint.getAmqpSendLink(2, 0).send(any(Message.class))).thenAnswer(sendAnswer);

            final int messagesCount = 4;
            final List<ServiceBusMessage> messagesToSend = createMessagesToSend(messagesCount);
            final ServiceBusMessage messageToSend = createMessageToSend();

            final ReactorConnectionCache<ServiceBusReactorAmqpConnection> connectionCache = createConnectionCache(connectionSupplier);
            final ServiceBusSenderAsyncClient sender = createSenderAsyncClient(connectionCache, false);
            try {
                // The Producer.send that internally attempt to use AmqpSendLink in the first Session, but fails
                // with a non-transient-error on the AmqpSendLink.
                try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                    verifier.create(() -> sender.sendMessages(messagesToSend))
                        .thenAwait(VIRTUAL_TIME_SHIFT)
                        .verifyErrorSatisfies(e -> {
                            Assertions.assertTrue(e instanceof ServiceBusException);
                            final ServiceBusException se = (ServiceBusException) e;
                            Assertions.assertSame(NON_RETRIABLE_ERROR_1, se.getCause());
                        });
                }

                // The Producer.send that internally attempt to use AmqpSendLink in the second Session, but fails
                // with a non-transient-error on the Session.
                try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                    verifier.create(() -> sender.sendMessages(messagesToSend))
                        .thenAwait(VIRTUAL_TIME_SHIFT)
                        .verifyErrorSatisfies(e -> {
                            Assertions.assertTrue(e instanceof ServiceBusException);
                            final ServiceBusException se = (ServiceBusException) e;
                            Assertions.assertSame(NON_RETRIABLE_ERROR_2, se.getCause());
                        });
                }

                // The Sender.send that internally attempt to use AmqpSendLink in the third Session, that should be
                // successful, indicating the sender instance can be used irrespective of errors from earlier
                // 'send' API calls.
                try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                    verifier.create(() -> sender.sendMessage(messageToSend))
                        .thenAwait(VIRTUAL_TIME_SHIFT)
                        .verifyComplete();
                }

                // Verify that all three AmqpSendLinks were notified with the expected messages/message
                // corresponding to List<ServiceBusMessage>/ServiceBusMessage.
                verify(endpoint.getAmqpSendLink(0, 0)).send(sendMessagesCaptor0.capture());
                final List<Message> messagesSendInSession0Link0 = sendMessagesCaptor0.getValue();
                Assertions.assertEquals(messagesCount, messagesSendInSession0Link0.size());

                verify(endpoint.getAmqpSendLink(1, 0)).send(sendMessagesCaptor1.capture());
                final List<Message> messagesSendInSession1Link0 = sendMessagesCaptor1.getValue();
                Assertions.assertEquals(messagesCount, messagesSendInSession1Link0.size());

                verify(endpoint.getAmqpSendLink(2, 0), times(1)).send(any(Message.class));
            } finally {
                sender.close();
                connectionCache.dispose();
            }
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void shouldRecoverFromRetriableConnectionError() {
        final int endpointsCount = 4;
        final List<SessionLinkCount> sessionLinkCountList = new ArrayList<>(endpointsCount);
        sessionLinkCountList.add(new SessionLinkCount(1, new int[] { 1 }));
        sessionLinkCountList.add(new SessionLinkCount(1, new int[] { 1 }));
        sessionLinkCountList.add(new SessionLinkCount(1, new int[] { 1 }));
        sessionLinkCountList.add(new SessionLinkCount(1, new int[] { 1 }));

        try (MockEndpoints endpoints = createMockEndpoints(sessionLinkCountList)) {
            final AtomicReference<MockEndpoint> currentEndpoint = new AtomicReference<>();
            // The ServiceBusReactorAmqpConnection connection supplier for ReactorConnectionCache.
            final Supplier<ServiceBusReactorAmqpConnection> connectionSupplier = new Supplier<ServiceBusReactorAmqpConnection>() {
                private int endpointIndex = -1;

                @Override
                public ServiceBusReactorAmqpConnection get() {
                    endpointIndex++;
                    if (endpointIndex >= endpointsCount) {
                        throw new RuntimeException("More than " + endpointsCount + " invocation of connection-supplier is not expected.");
                    }
                    final MockEndpoint e = endpoints.get(endpointIndex);
                    currentEndpoint.set(e);
                    final ServiceBusReactorAmqpConnection c = e.arrange();
                    return c;
                }
            };

            // Answers the invocation of 'send' API on each Session's AmqpSendLink in each Connection.
            final Answer<Mono<Void>> sendAnswer = new Answer<Mono<Void>>() {
                private int invocationCount = -1;

                @Override
                public Mono<Void> answer(InvocationOnMock invocation) {
                    invocationCount++;
                    switch (invocationCount) {
                        case 0:
                            Assertions.assertEquals(endpoints.get(0), currentEndpoint.get());
                            // Replicate the arrival of transient-error in the current AmqpSendLink (in the first
                            // connection) on which 'send' API is invoked.
                            endpoints.get(0).emitCurrentSendLinkError(RETRIABLE_LINK_ERROR);
                            return Mono.error(RETRIABLE_LINK_ERROR);
                        case 1:
                            Assertions.assertEquals(endpoints.get(1), currentEndpoint.get());
                            // Replicate the arrival of transient-error in the current Session (in the second
                            // connection) hosting the current AmqpSendLink on which 'send' API is invoked.
                            endpoints.get(1).emitCurrentSessionError(RETRIABLE_SESSION_ERROR);
                            return Mono.error(RETRIABLE_SESSION_ERROR);
                        case 2:
                            Assertions.assertEquals(endpoints.get(2), currentEndpoint.get());
                            // Replicate the arrival of transient-error in the third connection owning the Session
                            // hosting the current AmqpSendLink on which 'send' API is invoked.
                            endpoints.get(2).emitConnectionError(RETRIABLE_CONNECTION_ERROR);
                            return Mono.error(RETRIABLE_CONNECTION_ERROR);
                        case 3:
                            Assertions.assertEquals(endpoints.get(3), currentEndpoint.get());
                            // Replicate the successful processing of 'send' API invocation on the current
                            // AmqpSendLink in the Session owned by fourth Session.
                            return Mono.empty();
                        default:
                            throw new RuntimeException("More than three invocations of send-answer is not expected.");
                    }
                }
            };

            // Associate Answer to each AmqpSendLink in three connections.
            when(endpoints.get(0).getAmqpSendLink(0, 0).send(anyList())).thenAnswer(sendAnswer);
            when(endpoints.get(1).getAmqpSendLink(0, 0).send(anyList())).thenAnswer(sendAnswer);
            when(endpoints.get(2).getAmqpSendLink(0, 0).send(anyList())).thenAnswer(sendAnswer);
            when(endpoints.get(3).getAmqpSendLink(0, 0).send(anyList())).thenAnswer(sendAnswer);

            final int messagesCount = 4;
            final List<ServiceBusMessage> messagesToSend = createMessagesToSend(messagesCount);

            final ReactorConnectionCache<ServiceBusReactorAmqpConnection> connectionCache = createConnectionCache(connectionSupplier);
            final ServiceBusSenderAsyncClient sender = createSenderAsyncClient(connectionCache, false);
            try {
                try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                    verifier.create(() -> sender.sendMessages(messagesToSend))
                        .thenAwait(VIRTUAL_TIME_SHIFT)
                        .verifyComplete();
                }

                verify(endpoints.get(0).getAmqpSendLink(0, 0), times(1)).send(anyList());
                verify(endpoints.get(1).getAmqpSendLink(0, 0), times(1)).send(anyList());
                verify(endpoints.get(2).getAmqpSendLink(0, 0), times(1)).send(anyList());
                verify(endpoints.get(3).getAmqpSendLink(0, 0), times(1)).send(anyList());
            } finally {
                sender.close();
                connectionCache.dispose();
            }
        }
    }

    private MockEndpoint createMockEndpoint(int sessionsCnt, int[] linksPerSession) {
        final String connectionId = "1";
        return MockEndpoint.create(connectionId, QUEUE_NAME, RETRY_OPTIONS, sessionsCnt, linksPerSession);
    }

    private MockEndpoints createMockEndpoints(List<SessionLinkCount> sessionLinkCountList) {
        return MockEndpoints.create(QUEUE_NAME, RETRY_OPTIONS, sessionLinkCountList);
    }

    private Supplier<ServiceBusReactorAmqpConnection> singleConnectionSupplier(MockEndpoint endpoint) {
        final Supplier<ServiceBusReactorAmqpConnection> connectionSupplier = new Supplier<ServiceBusReactorAmqpConnection>() {
            private int invocationCount = -1;
            @Override
            public ServiceBusReactorAmqpConnection get() {
                invocationCount++;
                switch (invocationCount) {
                    case 0:
                        final ServiceBusReactorAmqpConnection c = endpoint.arrange();
                        return c;
                    default:
                        throw new RuntimeException("More than one invocation of connection-supplier is not expected.");
                }
            }
        };
        return connectionSupplier;
    }

    private ReactorConnectionCache<ServiceBusReactorAmqpConnection> createConnectionCache(
        Supplier<ServiceBusReactorAmqpConnection> connectionSupplier) {
        return new ReactorConnectionCache<>(connectionSupplier,
            FQDN, QUEUE_NAME, getRetryPolicy(RETRY_OPTIONS), new HashMap<>());
    }

    private ServiceBusSenderAsyncClient createSenderAsyncClient(
        ReactorConnectionCache<ServiceBusReactorAmqpConnection> connectionCache, boolean isSharedConnection) {
        final ConnectionCacheWrapper connectionSupport = new ConnectionCacheWrapper(connectionCache);
        return new ServiceBusSenderAsyncClient(QUEUE_NAME, MessagingEntityType.QUEUE, connectionSupport, RETRY_OPTIONS,
            DEFAULT_INSTRUMENTATION, messageSerializer, onClientClosed, "", CLIENT_IDENTIFIER);
    }

    private static List<ServiceBusMessage> createMessagesToSend(int messagesCount) {
        return IntStream.range(0, messagesCount)
            .mapToObj(__ -> new ServiceBusMessage(TEST_CONTENTS))
            .collect(Collectors.toList());
    }

    private static ServiceBusMessage createMessageToSend() {
        final ServiceBusMessage messageToSend = new ServiceBusMessage(TEST_CONTENTS);
        return messageToSend;
    }

    private static final class VirtualTimeStepVerifier implements AutoCloseable {
        private final VirtualTimeScheduler scheduler;

        VirtualTimeStepVerifier() {
            scheduler = VirtualTimeScheduler.create();
        }

        <T> StepVerifier.Step<T> create(Supplier<Mono<T>> scenarioSupplier) {
            return StepVerifier.withVirtualTime(scenarioSupplier, () -> scheduler, 0);
        }

        @Override
        public void close() {
            scheduler.dispose();
        }
    }

    private static class SessionLinkCount {
        private final int sessionsCnt;
        private final int[] linksPerSession;

        SessionLinkCount(int sessionsCnt, int[] linksPerSession) {
            this.sessionsCnt = sessionsCnt;
            this.linksPerSession = linksPerSession;
        }
    }

    private static final class MockEndpoints implements Closeable {
        private final List<MockEndpoint> mockEndpoints;
        private final int mockEndpointsCnt;

        private MockEndpoints(List<MockEndpoint> mockEndpoints) {
            this.mockEndpoints = mockEndpoints;
            this.mockEndpointsCnt = this.mockEndpoints.size();
        }

        static MockEndpoints create(String queueName, AmqpRetryOptions retryOptions, List<SessionLinkCount> sessionLinkCounts) {
            final List<MockEndpoint> mockEndpoints = new ArrayList<>(sessionLinkCounts.size());
            int conId = 1;
            for (SessionLinkCount slc : sessionLinkCounts) {
                mockEndpoints.add(MockEndpoint.create(String.valueOf(conId), queueName, retryOptions, slc.sessionsCnt, slc.linksPerSession));
                conId++;
            }
            return new MockEndpoints(mockEndpoints);
        }

        MockEndpoint get(int index) {
            if (index >= mockEndpointsCnt) {
                throw new IndexOutOfBoundsException("index:" + index + " maxIndex: " + (mockEndpointsCnt - 1));
            }
            return mockEndpoints.get(index);
        }

        @Override
        public void close() {
            for (MockEndpoint mockEndpoint : mockEndpoints) {
                mockEndpoint.close();
            }
        }
    }

    private static final class MockEndpoint implements Closeable {
        private final String connectionId;
        private final String queueName;
        private final AmqpRetryOptions retryOptions;
        private final MockSendSessions mockSendSessions;
        private final ConnectionOptions connectionOptions;
        private final Connection connection;
        private final Reactor reactor;
        private final ReactorDispatcher reactorDispatcher;
        private final ReactorExecutor reactorExecutor;
        private final ReactorProvider reactorProvider;
        private final ConnectionHandler connectionHandler;
        private final Sinks.Many<EndpointState> connectionStateSink;
        private final ReactorHandlerProvider handlerProvider;
        private final ServiceBusAmqpLinkProvider linkProvider;
        private final TokenManagerProvider tokenManagerProvider;
        private final TokenManager tokenManager;
        private final MessageSerializer messageSerializer;
        private final AtomicBoolean arranged = new AtomicBoolean(false);

        private MockEndpoint(String connectionId, String queueName, AmqpRetryOptions retryOptions, MockSendSessions mockSendSessions,
                             ConnectionOptions connectionOptions, Connection connection, Reactor reactor, ReactorDispatcher reactorDispatcher,
                             ReactorExecutor reactorExecutor, ReactorProvider reactorProvider, ConnectionHandler connectionHandler,
                             Sinks.Many<EndpointState> connectionStateSink, ReactorHandlerProvider handlerProvider, ServiceBusAmqpLinkProvider linkProvider,
                             TokenManagerProvider tokenManagerProvider, TokenManager tokenManager, MessageSerializer messageSerializer) {
            this.connectionId = connectionId;
            this.queueName = queueName;
            this.retryOptions = retryOptions;
            this.mockSendSessions = mockSendSessions;
            this.connectionOptions = connectionOptions;
            this.connection = connection;
            this.reactor = reactor;
            this.reactorDispatcher = reactorDispatcher;
            this.reactorExecutor = reactorExecutor;
            this.reactorProvider = reactorProvider;
            this.connectionHandler = connectionHandler;
            this.connectionStateSink = connectionStateSink;
            this.handlerProvider = handlerProvider;
            this.linkProvider = linkProvider;
            this.tokenManagerProvider = tokenManagerProvider;
            this.tokenManager = tokenManager;
            this.messageSerializer = messageSerializer;
        }

        static MockEndpoint create(String connectionId, String queueName, AmqpRetryOptions retryOptions, int sessionsCnt, int[] linksPerSession) {
            Assertions.assertNotNull(retryOptions);
            Assertions.assertTrue(sessionsCnt > 0, "sessionsCnt must be > 0.");
            Assertions.assertEquals(sessionsCnt, linksPerSession.length);
            for (int linksCnt = 0; linksCnt < linksPerSession.length; linksCnt++) {
                Assertions.assertTrue(linksCnt >= 0, "links-count in linksPerSession must be >= 0.");
            }

            final MockSendSessions mockSendSessions = MockSendSessions.create(connectionId, sessionsCnt, linksPerSession);

            final ConnectionOptions connectionOptions = mock(ConnectionOptions.class);
            final Connection connection = mock(Connection.class);
            final Sinks.Many<EndpointState> connectionStateSink = Sinks.many().replay()
                .latestOrDefault(EndpointState.UNINITIALIZED);
            final ConnectionHandler connectionHandler = mock(ConnectionHandler.class);

            final Reactor reactor = mock(Reactor.class);
            final ReactorDispatcher reactorDispatcher = mock(ReactorDispatcher.class);
            final ReactorExecutor reactorExecutor = mock(ReactorExecutor.class);

            final ReactorProvider reactorProvider = mock(ReactorProvider.class);
            final ReactorHandlerProvider handlerProvider = mock(ReactorHandlerProvider.class);
            final ServiceBusAmqpLinkProvider linkProvider = mock(ServiceBusAmqpLinkProvider.class);
            final TokenManager tokenManager = mock(TokenManager.class);
            final TokenManagerProvider tokenManagerProvider = mock(TokenManagerProvider.class);
            final MessageSerializer messageSerializer = mock(MessageSerializer.class);

            return new MockEndpoint(connectionId, queueName, retryOptions, mockSendSessions, connectionOptions, connection,
                reactor, reactorDispatcher, reactorExecutor, reactorProvider, connectionHandler, connectionStateSink,
                handlerProvider, linkProvider, tokenManagerProvider, tokenManager, messageSerializer);
        }

        ServiceBusReactorAmqpConnection arrange() {
            if (arranged.getAndSet(true)) {
                throw new RuntimeException("Only one connection can be obtained from a MockEndpoint instance.");
            }

            mockSendSessions.arrange(handlerProvider, linkProvider, connection, connectionStateSink);

            when(connectionOptions.getRetry()).thenReturn(retryOptions);
            doNothing().when(connection).close();
            connectionStateSink.emitNext(EndpointState.ACTIVE, Sinks.EmitFailureHandler.FAIL_FAST);
            when(connectionHandler.getEndpointStates()).thenReturn(connectionStateSink.asFlux().distinctUntilChanged());
            doNothing().when(connectionHandler).close();

            when(reactor.connectionToHost(any(), anyInt(), any())).thenReturn(connection);
            try {
                doAnswer(invocation -> {
                    final Runnable work = invocation.getArgument(0);
                    work.run();
                    return null;
                }).when(reactorDispatcher).invoke(any(Runnable.class));
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
            when(reactorDispatcher.getShutdownSignal()).thenReturn(Mono.empty());
            doNothing().when(reactorExecutor).start();
            when(reactorExecutor.closeAsync()).thenReturn(Mono.empty());
            try {
                when(reactorProvider.createReactor(anyString(), anyInt())).thenReturn(reactor);
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }

            when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);
            when(reactorProvider.createExecutor(any(), anyString(), any(), any(), any())).thenReturn(reactorExecutor);
            when(handlerProvider.createConnectionHandler(anyString(), any())).thenReturn(connectionHandler);
            when(tokenManager.authorize()).thenReturn(Mono.just(Duration.ofHours(1).toMillis()));
            when(tokenManagerProvider.getTokenManager(any(), anyString())).thenReturn(tokenManager);

            // New tests only for ReactorConnectionCache introduced in v2.
            final boolean isV2 = true;
            return new ServiceBusReactorAmqpConnection(connectionId, connectionOptions,
                reactorProvider, handlerProvider, linkProvider, tokenManagerProvider, messageSerializer,  false, isV2);
        }

        AmqpSendLink getAmqpSendLink(int sessionIdx, int linkIdx) {
            return mockSendSessions.getAmqpSendLink(sessionIdx, linkIdx);
        }

        void emitConnectionError(Throwable throwable) {
            connectionStateSink.emitError(throwable, Sinks.EmitFailureHandler.FAIL_FAST);
        }

        void emitConnectionCompletion() {
            connectionStateSink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        }

        void emitCurrentSessionState(EndpointState state) {
            final MockSendSession session = mockSendSessions.getCurrentSendSession();
            session.emitSessionState(state);
        }

        void emitCurrentSessionError(Throwable throwable) {
            final MockSendSession session = mockSendSessions.getCurrentSendSession();
            session.emitSessionError(throwable);
        }

        void emitCurrentSessionCompletion() {
            final MockSendSession session = mockSendSessions.getCurrentSendSession();
            session.emitSessionCompletion();
        }

        void emitCurrentSendLinkState(EndpointState state) {
            final MockSendLink sendLink = mockSendSessions.getCurrentSendLink();
            sendLink.emitSendLinkState(state);
        }

        void emitCurrentSendLinkError(Throwable throwable) {
            final MockSendLink sendLink = mockSendSessions.getCurrentSendLink();
            sendLink.emitSendLinkError(throwable);
        }

        void emitCurrentSendLinkCompletion() {
            final MockSendLink sendLink = mockSendSessions.getCurrentSendLink();
            sendLink.emitSendLinkCompletion();
        }

        @Override
        public void close() {
            Mockito.framework().clearInlineMock(connectionOptions);
            Mockito.framework().clearInlineMock(connection);
            Mockito.framework().clearInlineMock(connectionHandler);
            Mockito.framework().clearInlineMock(reactor);
            Mockito.framework().clearInlineMock(reactorDispatcher);
            Mockito.framework().clearInlineMock(reactorExecutor);
            Mockito.framework().clearInlineMock(reactorProvider);
            Mockito.framework().clearInlineMock(handlerProvider);
            Mockito.framework().clearInlineMock(linkProvider);
            Mockito.framework().clearInlineMock(tokenManager);
            Mockito.framework().clearInlineMock(tokenManagerProvider);
            Mockito.framework().clearInlineMock(messageSerializer);

            mockSendSessions.close();
        }
    }

    private static final class MockSendSessions implements Closeable {
        private final Object lock = new Object();
        private final List<MockSendSession> mockSendSessions;
        // If the last Session in this MockSendSessions emits a retriable error, then the corresponding retry will be
        // served with the below terminal Session. Suppose the MockSendSessions ends up serving this terminal Session;
        // in that case, it will result in the closing of Connection (see 'moveToNextSession(..)'), this way, the next
        // retry will see Connection as closed and attempt to obtain the next Connection.
        private final MockSendSession terminalMockSendSession;
        private final int sessionsCnt;
        private int sessionIdx;
        private MockSendSession currentMockSendSession;

        private MockSendSessions(List<MockSendSession> mockSendSessions, MockSendSession terminalMockSendSession) {
            this.mockSendSessions = mockSendSessions;
            this.terminalMockSendSession = terminalMockSendSession;
            this.sessionsCnt = this.mockSendSessions.size();
            this.sessionIdx = 0;
        }

        static MockSendSessions create(String connectionId, int sessionsCnt, int[] linksPerSession) {
            final List<MockSendSession> mockSendSessions = new ArrayList<>(sessionsCnt);
            for (int i = 0; i < sessionsCnt; i++) {
                mockSendSessions.add(MockSendSession.create(connectionId, linksPerSession[i]));
            }
            final MockSendSession terminalMockSendSession = MockSendSession.create(connectionId, 0);

            return new MockSendSessions(Collections.unmodifiableList(mockSendSessions), terminalMockSendSession);
        }

        void arrange(ReactorHandlerProvider handlerProvider, AmqpLinkProvider linkProvider, Connection connection,
                     Sinks.Many<EndpointState> connectionStateSink) {
            for (MockSendSession mockSession : mockSendSessions) {
                mockSession.arrange();
                mockSession.emitSessionState(EndpointState.ACTIVE);
            }
            terminalMockSendSession.arrange();
            terminalMockSendSession.emitSessionCompletion();

            // Arrange the stub to provide SessionHandlers hosting AmqpSendLinks.
            when(handlerProvider.createSessionHandler(anyString(), any(), anyString(), any()))
                .thenAnswer(invocation -> {
                    final MockSendSession session = moveToNextSendSession(connectionStateSink);
                    return session.getSessionHandler();
                });

            // Arrange the stub to provide SendLinkHandlers.
            when(handlerProvider.createSendLinkHandler(anyString(), any(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    final MockSendLink sendLink = moveToNextSendLinkInCurrentSession();
                    return sendLink.getSendLinkHandler();
                });

            // Arrange the stub to provide AmqpSendLinks.
            when(linkProvider.createSendLink(any(ServiceBusReactorAmqpConnection.class), anyString(), any(Sender.class),
                any(SendLinkHandler.class), any(ReactorProvider.class), any(TokenManager.class), any(MessageSerializer.class),
                any(AmqpRetryOptions.class), any(Scheduler.class), any()))
                .thenAnswer(invocation -> {
                    final SendLinkHandler sendLinkHandler = invocation.getArgument(3);
                    final AmqpSendLink amqpSendLink = lookupAmqpSendLinkFor(sendLinkHandler);
                    return amqpSendLink;
                });

            // Arrange the stub to provide the QPID sessions.
            final ArrayList<Session> qpidSessions = new ArrayList<>(sessionsCnt + 1);
            for (MockSendSession mockSendSession : mockSendSessions) {
                qpidSessions.add(mockSendSession.getQpidSession());
            }
            qpidSessions.add(terminalMockSendSession.getQpidSession());
            when(connection.session())
                .thenReturn(qpidSessions.get(0), qpidSessions.subList(1, sessionsCnt + 1).toArray(new Session[0]));
        }

        AmqpSendLink getAmqpSendLink(int sessionIdx, int linkIdx) {
            Assertions.assertTrue(sessionIdx >= 0 && sessionIdx < sessionsCnt, "sessionIdx is not in range.");
            final MockSendSession session = mockSendSessions.get(sessionIdx);
            return session.getAmqpSendLink(linkIdx);
        }

        MockSendSession getCurrentSendSession() {
            final MockSendSession session;
            synchronized (lock) {
                session = Objects.requireNonNull(currentMockSendSession, "Current Session is null");
            }
            return session;
        }

        MockSendLink getCurrentSendLink() {
            final MockSendLink sendLink;
            synchronized (lock) {
                final MockSendSession session = getCurrentSendSession();
                sendLink = session.getCurrentSendLink();
            }
            return sendLink;
        }

        private MockSendSession moveToNextSendSession(Sinks.Many<EndpointState> connectionStateSink) {
            final MockSendSession nextSession;
            synchronized (lock) {
                if (sessionIdx >= sessionsCnt) {
                    nextSession = terminalMockSendSession;
                } else {
                    nextSession = mockSendSessions.get(sessionIdx);
                    sessionIdx++;
                }
                this.currentMockSendSession = nextSession;
            }
            if (isTerminalSession(nextSession)) {
                connectionStateSink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
            }
            return nextSession;
        }

        private MockSendLink moveToNextSendLinkInCurrentSession() {
            final MockSendSession session;
            final MockSendLink nextSendLink;
            synchronized (lock) {
                session = Objects.requireNonNull(currentMockSendSession, "Current Session is null");
                nextSendLink = session.moveToNextSendLink();
            }
            if (session.isTerminalSendLink(nextSendLink)) {
                session.emitSessionCompletion();
            }
            return nextSendLink;
        }

        private AmqpSendLink lookupAmqpSendLinkFor(SendLinkHandler sendLinkHandler) {
            for (MockSendSession mockSendSession : mockSendSessions) {
                final AmqpSendLink amqpSendLink = mockSendSession.lookupAmqpSendLinkFor(sendLinkHandler);
                if (amqpSendLink != null) {
                    return amqpSendLink;
                }
            }
            final AmqpSendLink amqpSendLink = terminalMockSendSession.lookupAmqpSendLinkFor(sendLinkHandler);
            if (amqpSendLink != null) {
                return amqpSendLink;
            }
            throw new NullPointerException("Lookup for AmqpSendLink failed.");
        }

        private boolean isTerminalSession(MockSendSession session) {
            return session == terminalMockSendSession;
        }

        @Override
        public void close() {
            for (MockSendSession mockSendSession : mockSendSessions) {
                mockSendSession.close();
            }
            terminalMockSendSession.close();
        }
    }

    private static final class MockSendSession implements Closeable {
        private final String connectionId;
        private final Session session;
        private final Record sessionAttachments;
        private final SessionHandler sessionHandler;
        private final Sinks.Many<EndpointState> sessionStateSink;
        private final List<MockSendLink> mockSendLinks;
        // If the last AmqpSendLink in this MockSendSession emits a retriable error, then the corresponding retry will be
        // served with the below terminal AmqpSendLink. Suppose the Session ends up serving this terminal link, in that
        // case, it will result in closing of Session (see 'moveToNextSendLinkInCurrentSession()), and the invocation
        // of send API on the terminal link emits a retriable error, this way the next retry will see Session as closed,
        // and attempt to obtain the next Session.
        private final MockSendLink terminalMockSendLink;
        private final int sendLinkCnt;
        private int sendLinkIdx;
        // The read-write to currentMockSendLink is synchronized by MockSessions lock.
        private MockSendLink currentMockSendLink;

        private MockSendSession(String connectionId, Session session, Record sessionAttachments, SessionHandler sessionHandler,
                                Sinks.Many<EndpointState> sessionStateSink, List<MockSendLink> mockSendLinks, MockSendLink terminalMockSendLink) {
            this.connectionId = connectionId;
            this.session = session;
            this.sessionAttachments = sessionAttachments;
            this.sessionHandler = sessionHandler;
            this.sessionStateSink = sessionStateSink;
            this.mockSendLinks = mockSendLinks;
            this.terminalMockSendLink = terminalMockSendLink;
            this.sendLinkCnt = this.mockSendLinks.size();
            this.sendLinkIdx = 0;
        }

        static MockSendSession create(String connectionId, int sendLinkCnt) {
            final List<MockSendLink> mockSendLinks = new ArrayList<>(sendLinkCnt);
            for (int i = 0; i < sendLinkCnt; i++) {
                mockSendLinks.add(MockSendLink.create());
            }
            final MockSendLink terminalMockSendLink = MockSendLink.create();

            final Record sessionAttachments = mock(Record.class);
            final Session session = mock(Session.class);
            final SessionHandler sessionHandler = mock(SessionHandler.class);
            final Sinks.Many<EndpointState> sessionStateSink = Sinks.many().replay()
                .latestOrDefault(EndpointState.UNINITIALIZED);
            return new MockSendSession(connectionId, session, sessionAttachments, sessionHandler, sessionStateSink,
                Collections.unmodifiableList(mockSendLinks), terminalMockSendLink);
        }

        void arrange() {
            for (MockSendLink mockSendLink : mockSendLinks) {
                mockSendLink.arrange();
                mockSendLink.emitSendLinkState(EndpointState.ACTIVE);
            }
            terminalMockSendLink.arrange();
            terminalMockSendLink.emitSendLinkCompletion();
            final Answer<Mono<Void>> terminalSendAnswer = new Answer<Mono<Void>>() {
                @Override
                public Mono<Void> answer(InvocationOnMock invocation) {
                    return Mono.error(new AmqpException(true, "terminal-send-link-result", null));
                }
            };
            when(terminalMockSendLink.getAmqpSendLink().send(anyList())).then(terminalSendAnswer);
            when(terminalMockSendLink.getAmqpSendLink().send(any(Message.class))).then(terminalSendAnswer);
            when(terminalMockSendLink.getAmqpSendLink().send(any(Message.class), any(DeliveryState.class))).then(terminalSendAnswer);
            when(terminalMockSendLink.getAmqpSendLink().send(anyList(), any(DeliveryState.class))).then(terminalSendAnswer);
            when(terminalMockSendLink.getAmqpSendLink().send(any(), anyInt(), anyInt(), any(DeliveryState.class))).then(terminalSendAnswer);

            doNothing().when(sessionAttachments).set(any(), any(), anyString());
            when(session.attachments()).thenReturn(sessionAttachments);
            doNothing().when(session).open();
            doNothing().when(session).setCondition(any());
            when(sessionHandler.getConnectionId()).thenReturn(connectionId);
            when(sessionHandler.getEndpointStates()).thenReturn(sessionStateSink.asFlux().distinctUntilChanged());
            doNothing().when(sessionHandler).close();

            // Arrange the stub to provide the QPID senders.
            final ArrayList<Sender> qpidSenders = new ArrayList<>(sendLinkCnt + 1);
            for (MockSendLink mockSendLink : mockSendLinks) {
                qpidSenders.add(mockSendLink.getQpidSender());
            }
            qpidSenders.add(terminalMockSendLink.getQpidSender());
            when(session.sender(any()))
                .thenReturn(qpidSenders.get(0), qpidSenders.subList(1, sendLinkCnt + 1).toArray(new Sender[0]));
        }

        Session getQpidSession() {
            return session;
        }

        SessionHandler getSessionHandler() {
            return sessionHandler;
        }

        AmqpSendLink getAmqpSendLink(int linkIdx) {
            Assertions.assertTrue(linkIdx >= 0 && linkIdx < sendLinkCnt, "linkIdx is not in range.");
            return mockSendLinks.get(linkIdx).getAmqpSendLink();
        }

        MockSendLink moveToNextSendLink() {
            final MockSendLink nextSendLink;
            if (sendLinkIdx >= sendLinkCnt) {
                nextSendLink = terminalMockSendLink;
            } else {
                nextSendLink = mockSendLinks.get(sendLinkIdx);
                sendLinkIdx++;
            }
            // The read-write to currentMockSendLink is synchronized by MockSessions lock.
            currentMockSendLink = nextSendLink;
            return nextSendLink;
        }

        boolean isTerminalSendLink(MockSendLink link) {
            return this.terminalMockSendLink == link;
        }

        MockSendLink getCurrentSendLink() {
            // The read-write to currentMockSendLink is synchronized by MockSessions lock.
            return Objects.requireNonNull(currentMockSendLink, "Current Link is null");
        }

        AmqpSendLink lookupAmqpSendLinkFor(SendLinkHandler sendLinkHandler) {
            for (MockSendLink sendLink : mockSendLinks) {
                if (sendLink.getSendLinkHandler() == sendLinkHandler) {
                    return sendLink.getAmqpSendLink();
                }
            }
            if (terminalMockSendLink.getSendLinkHandler() == sendLinkHandler) {
                return terminalMockSendLink.getAmqpSendLink();
            }
            return null;
        }

        void emitSessionState(EndpointState state) {
            this.sessionStateSink.emitNext(state, Sinks.EmitFailureHandler.FAIL_FAST);
        }

        void emitSessionError(Throwable error) {
            this.sessionStateSink.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST);
        }

        void emitSessionCompletion() {
            this.sessionStateSink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        }

        @Override
        public void close() {
            Mockito.framework().clearInlineMock(session);
            Mockito.framework().clearInlineMock(sessionAttachments);
            Mockito.framework().clearInlineMock(sessionHandler);

            for (MockSendLink sendLink : mockSendLinks) {
                sendLink.close();
            }
            terminalMockSendLink.close();
        }
    }

    private static final class MockSendLink implements Closeable {
        private final Sender sender;
        private final Record senderAttachments;
        private final AmqpSendLink amqpSendLink;
        private final SendLinkHandler sendLinkHandler;
        private final Sinks.Many<EndpointState> sendLinkStateSink;

        private MockSendLink(Sender sender, Record senderAttachments, AmqpSendLink amqpSendLink, SendLinkHandler sendLinkHandler,
                             Sinks.Many<EndpointState> sendLinkStateSink) {
            this.sender = sender;
            this.senderAttachments = senderAttachments;
            this.amqpSendLink = amqpSendLink;
            this.sendLinkHandler = sendLinkHandler;
            this.sendLinkStateSink = sendLinkStateSink;
        }

        static MockSendLink create() {
            final Record senderAttachments = mock(Record.class);
            final Sender sender = mock(Sender.class);
            final AmqpSendLink amqpSendLink = mock(AmqpSendLink.class);
            final SendLinkHandler sendLinkHandler = mock(SendLinkHandler.class);
            final Sinks.Many<EndpointState> sendLinkStateSink = Sinks.many().replay()
                .latestOrDefault(EndpointState.UNINITIALIZED);
            return new MockSendLink(sender, senderAttachments, amqpSendLink, sendLinkHandler, sendLinkStateSink);
        }

        void arrange() {
            doNothing().when(senderAttachments).set(any(), any(), anyString());
            when(sender.attachments()).thenReturn(senderAttachments);
            doNothing().when(sender).setTarget(any());
            doNothing().when(sender).setSenderSettleMode(any());
            doNothing().when(sender).setProperties(any());
            doNothing().when(sender).setSource(any());
            doNothing().when(sender).open();
            when(amqpSendLink.getLinkSize()).thenReturn(Mono.just(ServiceBusSenderAsyncClient.MAX_MESSAGE_LENGTH_BYTES));
            when(amqpSendLink.getEndpointStates())
                .thenReturn(sendLinkStateSink.asFlux().distinctUntilChanged().map(state -> toAmqpEndpointState(state)));
            when(sendLinkHandler.getEndpointStates()).thenReturn(sendLinkStateSink.asFlux().distinctUntilChanged());
            doNothing().when(sendLinkHandler).close();
        }

        Sender getQpidSender() {
            return sender;
        }

        AmqpSendLink getAmqpSendLink() {
            return amqpSendLink;
        }

        SendLinkHandler getSendLinkHandler() {
            return sendLinkHandler;
        }

        void emitSendLinkState(EndpointState state) {
            this.sendLinkStateSink.emitNext(state, Sinks.EmitFailureHandler.FAIL_FAST);
        }

        void emitSendLinkError(Throwable error) {
            this.sendLinkStateSink.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST);
        }

        void emitSendLinkCompletion() {
            this.sendLinkStateSink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        }

        private static AmqpEndpointState toAmqpEndpointState(EndpointState state) {
            switch (state) {
                case ACTIVE:
                    return AmqpEndpointState.ACTIVE;
                case UNINITIALIZED:
                    return AmqpEndpointState.UNINITIALIZED;
                case CLOSED:
                    return AmqpEndpointState.CLOSED;
                default:
                    throw new IllegalArgumentException("This endpoint state is not supported. State:" + state);
            }
        }

        @Override
        public void close() {
            Mockito.framework().clearInlineMock(sender);
            Mockito.framework().clearInlineMock(senderAttachments);
            Mockito.framework().clearInlineMock(amqpSendLink);
            Mockito.framework().clearInlineMock(sendLinkHandler);
        }
    }
}
