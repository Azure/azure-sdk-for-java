// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.implementation.MessageSerializer;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Modified;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.ServiceBusSessionAcquirer.Session;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class SessionsMessagePumpIsolatedTest {
    private final AmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(new AmqpRetryOptions());
    private final Duration idleTimeoutDisabled = null;
    private AutoCloseable mocksCloseable;
    @Captor
    private ArgumentCaptor<String> lockTokenCaptor;
    @Captor
    private ArgumentCaptor<DeliveryState> deliveryStateCaptor;

    @BeforeEach
    public void setup() throws IOException {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }


    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldCloseBackingSessionReceiverOnCancel() {
        final String session1Id = "1";

        final HashMap<Message, ServiceBusReceivedMessage> session1Messages = new HashMap<>(0);
        final TestPublisher<AmqpEndpointState> session1EpStates = TestPublisher.createCold();
        session1EpStates.next(AmqpEndpointState.ACTIVE);
        final Session session1 = createMockSession(session1Id, session1Messages, session1EpStates);
        final MessageSerializer serializer = mock(MessageSerializer.class);
        final ServiceBusSessionAcquirer sessionAcquirer = createMockSessionAcquirer(session1);
        final Runnable onTerminate = createMockOnTerminate();

        final int maxSessions = 1;
        final int concurrency = 1;
        final boolean autoDispositionDisabled = true;
        final Consumer<ServiceBusReceivedMessageContext> processMessage = context -> { };
        final Consumer<ServiceBusErrorContext> processError = e -> { };
        final SessionsMessagePump pump = createSessionsMessagePump(sessionAcquirer, idleTimeoutDisabled, maxSessions, concurrency,
            autoDispositionDisabled, serializer, processMessage, processError, onTerminate);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.begin())
                .thenAwait()
                .thenCancel()
                .verify();
        }
        verify(session1.link, times(1)).closeAsync();
        verify(onTerminate, times(1)).run();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldPumpFromSingleSession() {
        final String session1Id = "1";
        final int session1MessagesCount = 5;

        final HashMap<Message, ServiceBusReceivedMessage> session1Messages = createMockMessages(session1Id, session1MessagesCount);
        final TestPublisher<AmqpEndpointState> session1EpStates = TestPublisher.createCold();
        session1EpStates.next(AmqpEndpointState.ACTIVE); // endpoint state does not end (hence session).
        final Session session1 = createMockSession(session1Id, session1Messages, session1EpStates);
        final MessageSerializer serializer = createMockmessageSerializer(session1Messages);
        final ServiceBusSessionAcquirer sessionAcquirer = createMockSessionAcquirer(session1);
        final Runnable onTerminate = createMockOnTerminate();

        final int maxSessions = 1;
        final int concurrency = 1;
        final boolean autoDispositionDisabled = true;
        final Set<ServiceBusReceivedMessage> unseenMessages = values(session1Messages);
        final Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
            unseenMessages.remove(context.getMessage());
        };
        final Consumer<ServiceBusErrorContext> processError = e -> { };
        final SessionsMessagePump pump = createSessionsMessagePump(sessionAcquirer, idleTimeoutDisabled, maxSessions, concurrency,
            autoDispositionDisabled, serializer, processMessage, processError, onTerminate);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.begin())
                .thenAwait()
                .thenCancel()
                .verify();
        }
        Assertions.assertTrue(unseenMessages.isEmpty());
        // closeAsync() invocation upon RollingSessionReceiver.MessageFlux cancellation.
        verify(session1.link, times(1)).closeAsync();
        verify(onTerminate, times(1)).run();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldPumpFromMultiSession() {
        final String session1Id = "1";
        final String session2Id = "2";
        final int sessionMessagesCount = 5;

        final HashMap<Message, ServiceBusReceivedMessage> session1Messages = createMockMessages(session1Id, sessionMessagesCount);
        final HashMap<Message, ServiceBusReceivedMessage> session2Messages = createMockMessages(session2Id, sessionMessagesCount);
        final TestPublisher<AmqpEndpointState> session1EpStates = TestPublisher.createCold();
        final TestPublisher<AmqpEndpointState> session2EpStates = TestPublisher.createCold();
        session1EpStates.next(AmqpEndpointState.ACTIVE);
        session2EpStates.next(AmqpEndpointState.ACTIVE);
        final Session session1 = createMockSession(session1Id, session1Messages, session1EpStates);
        final Session session2 = createMockSession(session2Id, session2Messages, session2EpStates);
        final MessageSerializer serializer = createMockmessageSerializer(session1Messages, session2Messages);
        final ServiceBusSessionAcquirer sessionAcquirer = createMockSessionAcquirer(session1, session2);
        final Runnable onTerminate = createMockOnTerminate();

        final int maxSessions = 2;
        final int concurrency = 1;
        final boolean autoDispositionDisabled = true;
        final Set<ServiceBusReceivedMessage> unseenMessages = values(session1Messages, session2Messages);
        final Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
            unseenMessages.remove(context.getMessage());
        };
        final Consumer<ServiceBusErrorContext> processError = e -> { };
        final SessionsMessagePump pump = createSessionsMessagePump(sessionAcquirer, idleTimeoutDisabled, maxSessions, concurrency,
            autoDispositionDisabled, serializer, processMessage, processError, onTerminate);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.begin())
                .thenAwait()
                .thenCancel()
                .verify();
        }
        Assertions.assertTrue(unseenMessages.isEmpty());
        verify(session1.link, times(1)).closeAsync();
        verify(session2.link, times(1)).closeAsync();
        verify(onTerminate, times(1)).run();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldTerminateOnAcquireError() {
        final int maxSessions = 1;
        final int concurrency = 1;
        final boolean autoDispositionDisabled = true;
        final ServiceBusSessionAcquirer sessionAcquirer = mock(ServiceBusSessionAcquirer.class);
        when(sessionAcquirer.acquire()).thenReturn(Mono.<Session>fromCallable(() -> {
            throw new RuntimeException("non-transient-error");
        }));
        final Runnable onTerminate = createMockOnTerminate();
        final Consumer<ServiceBusReceivedMessageContext> processMessage = context -> { };
        final Consumer<ServiceBusErrorContext> processError = e -> { };
        final SessionsMessagePump pump = createSessionsMessagePump(sessionAcquirer, idleTimeoutDisabled, maxSessions, concurrency,
            autoDispositionDisabled, mock(ServiceBusMessageSerializer.class), processMessage, processError, onTerminate);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.begin())
                .thenAwait()
                .verifyErrorSatisfies(e -> {
                    Assertions.assertTrue(e instanceof TerminatedException);
                    Assertions.assertNotNull(e.getCause());
                    Assertions.assertTrue(e.getCause() instanceof RuntimeException);
                    Assertions.assertEquals("non-transient-error", e.getCause().getMessage());
                });
        }
        verify(onTerminate, times(1)).run();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldTerminateOnConnectionTermination() {
        final Duration connectionStatePollInterval = Duration.ofSeconds(20);
        final String session1Id = "1";

        final HashMap<Message, ServiceBusReceivedMessage> session1Messages = new HashMap<>(0);
        final TestPublisher<AmqpEndpointState> session1EpStates = TestPublisher.createCold();
        session1EpStates.next(AmqpEndpointState.ACTIVE);
        final Session session1 = createMockSession(session1Id, session1Messages, session1EpStates);
        final MessageSerializer serializer = mock(MessageSerializer.class);
        final ServiceBusSessionAcquirer sessionAcquirer = createMockSessionAcquirer(session1);
        when(sessionAcquirer.isConnectionClosed()).thenReturn(false, true);
        final Runnable onTerminate = createMockOnTerminate();

        final int maxSessions = 1;
        final int concurrency = 1;
        final boolean autoDispositionDisabled = true;
        final Consumer<ServiceBusReceivedMessageContext> processMessage = context -> { };
        final Consumer<ServiceBusErrorContext> processError = e -> { };
        final SessionsMessagePump pump = createSessionsMessagePump(sessionAcquirer, idleTimeoutDisabled, maxSessions, concurrency,
            autoDispositionDisabled, serializer, processMessage, processError, onTerminate);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.begin())
                .thenAwait(connectionStatePollInterval.multipliedBy(3))
                .verifyErrorSatisfies(e -> {
                    Assertions.assertTrue(e instanceof TerminatedException);
                    Assertions.assertEquals("session#connection-state-poll", e.getMessage());
                });
        }
        verify(session1.link, times(1)).closeAsync();
        verify(onTerminate, times(1)).run();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRollToNextSessionUponErrorOnActiveSession() {
        final String session1Id = "1";
        final String session2Id = "2";
        final String session3Id = "3";
        final String session4Id = "4";
        final int sessionMessagesCount = 5;

        final HashMap<Message, ServiceBusReceivedMessage> session1Messages = createMockMessages(session1Id, sessionMessagesCount);
        final HashMap<Message, ServiceBusReceivedMessage> session2Messages = createMockMessages(session2Id, sessionMessagesCount);
        final HashMap<Message, ServiceBusReceivedMessage> session3Messages = createMockMessages(session3Id, sessionMessagesCount);
        final TestPublisher<AmqpEndpointState> session1EpStates = TestPublisher.createCold();
        final TestPublisher<AmqpEndpointState> session2EpStates = TestPublisher.createCold();
        final TestPublisher<AmqpEndpointState> session3EpStates = TestPublisher.createCold();
        session1EpStates.next(AmqpEndpointState.ACTIVE);
        session2EpStates.next(AmqpEndpointState.ACTIVE);
        session3EpStates.next(AmqpEndpointState.ACTIVE);
        final Session session1 = createMockSession(session1Id, session1Messages, session1EpStates);
        final Session session2 = createMockSession(session2Id, session2Messages, session2EpStates);
        final Session session3 = createMockSession(session3Id, session3Messages, session3EpStates);
        final MessageSerializer serializer = createMockmessageSerializer(session1Messages, session2Messages, session3Messages);
        final ServiceBusSessionAcquirer sessionAcquirer = createMockSessionAcquirer(session1, session2, session3);
        final Runnable onTerminate = createMockOnTerminate();

        final int maxSessions = 2; // Initially pump only from Session1 and Session2.
        final int concurrency = 1;
        final boolean autoDispositionDisabled = true;
        final Set<ServiceBusReceivedMessage> unseenMessages = values(session1Messages, session2Messages, session3Messages);
        final Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
            unseenMessages.remove(context.getMessage());
        };
        final Consumer<ServiceBusErrorContext> processError = e -> { };
        final SessionsMessagePump pump = createSessionsMessagePump(sessionAcquirer, idleTimeoutDisabled, maxSessions, concurrency,
            autoDispositionDisabled, serializer, processMessage, processError, onTerminate);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.begin())
                .thenAwait()
                .then(() -> session2EpStates.error(new RuntimeException("session2-detached"))) // Session2 terminated, now pump from Session3.
                .thenAwait(Duration.ofSeconds(5)) // Advance by 5-Sec, a time greater than MessageFlux backoff before roll to Session3.
                .thenCancel()
                .verify();
        }
        Assertions.assertTrue(unseenMessages.isEmpty());
        verify(session1.link, times(1)).closeAsync();
        verify(session2.link, times(1)).closeAsync();
        verify(session3.link, times(1)).closeAsync();
        verify(onTerminate, times(1)).run();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRollToNextSessionUponCompletionOfActiveSession() {
        final String session1Id = "1";
        final String session2Id = "2";
        final String session3Id = "3";
        final int sessionMessagesCount = 5;

        final HashMap<Message, ServiceBusReceivedMessage> session1Messages = createMockMessages(session1Id, sessionMessagesCount);
        final HashMap<Message, ServiceBusReceivedMessage> session2Messages = createMockMessages(session2Id, sessionMessagesCount);
        final HashMap<Message, ServiceBusReceivedMessage> session3Messages = createMockMessages(session3Id, sessionMessagesCount);
        final TestPublisher<AmqpEndpointState> session1EpStates = TestPublisher.createCold();
        final TestPublisher<AmqpEndpointState> session2EpStates = TestPublisher.createCold();
        final TestPublisher<AmqpEndpointState> session3EpStates = TestPublisher.createCold();
        session1EpStates.next(AmqpEndpointState.ACTIVE);
        session2EpStates.next(AmqpEndpointState.ACTIVE);
        session3EpStates.next(AmqpEndpointState.ACTIVE);
        final Session session1 = createMockSession(session1Id, session1Messages, session1EpStates);
        final Session session2 = createMockSession(session2Id, session2Messages, session2EpStates);
        final Session session3 = createMockSession(session3Id, session3Messages, session3EpStates);
        final MessageSerializer serializer = createMockmessageSerializer(session1Messages, session2Messages, session3Messages);
        final ServiceBusSessionAcquirer sessionAcquirer = createMockSessionAcquirer(session1, session2, session3);
        final Runnable onTerminate = createMockOnTerminate();

        final int maxSessions = 2; // Initially pump only from Session1 and Session2.
        final int concurrency = 1;
        final boolean autoDispositionDisabled = true;
        final Set<ServiceBusReceivedMessage> unseenMessages = values(session1Messages, session2Messages, session3Messages);
        final Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
            unseenMessages.remove(context.getMessage());
        };
        final Consumer<ServiceBusErrorContext> processError = e -> { };
        final SessionsMessagePump pump = createSessionsMessagePump(sessionAcquirer, idleTimeoutDisabled, maxSessions, concurrency,
            autoDispositionDisabled, serializer, processMessage, processError, onTerminate);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.begin())
                .thenAwait()
                .then(() -> session2EpStates.complete()) // Session2 terminated, now pump from Session3.
                .thenAwait(Duration.ofSeconds(5)) // Advance by 5-Sec, a time greater than MessageFlux backoff before roll to Session3.
                .thenCancel()
                .verify();
        }
        Assertions.assertTrue(unseenMessages.isEmpty());
        verify(session1.link, times(1)).closeAsync();
        verify(session2.link, times(1)).closeAsync();
        verify(session3.link, times(1)).closeAsync();
        verify(onTerminate, times(1)).run();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldCompleteMessageOnSuccessfulProcessing() {
        final String session1Id = "1";
        final int session1MessagesCount = 1;

        final HashMap<Message, ServiceBusReceivedMessage> session1Messages = createMockMessages(session1Id, session1MessagesCount);
        final TestPublisher<AmqpEndpointState> session1EpStates = TestPublisher.createCold();
        session1EpStates.next(AmqpEndpointState.ACTIVE);
        final Session session1 = createMockSession(session1Id, session1Messages, session1EpStates);
        when(session1.link.updateDisposition(any(), any())).thenReturn(Mono.empty());
        final MessageSerializer serializer = createMockmessageSerializer(session1Messages);
        final ServiceBusSessionAcquirer sessionAcquirer = createMockSessionAcquirer(session1);
        final Runnable onTerminate = createMockOnTerminate();

        final int maxSessions = 1;
        final int concurrency = 1;
        final boolean autoDispositionDisabled = false;
        final Set<ServiceBusReceivedMessage> unseenMessages = values(session1Messages);
        Assertions.assertEquals(1, unseenMessages.size());
        final ServiceBusReceivedMessage processedMessage = unseenMessages.iterator().next();
        final Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
            unseenMessages.remove(context.getMessage());
        };
        final Consumer<ServiceBusErrorContext> processError = e -> { };
        final SessionsMessagePump pump = createSessionsMessagePump(sessionAcquirer, idleTimeoutDisabled, maxSessions, concurrency,
            autoDispositionDisabled, serializer, processMessage, processError, onTerminate);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.begin())
                .thenAwait()
                .thenCancel()
                .verify();
        }

        Assertions.assertTrue(unseenMessages.isEmpty());
        verify(session1.link).updateDisposition(lockTokenCaptor.capture(), deliveryStateCaptor.capture());
        final String lockToken = lockTokenCaptor.getValue();
        final DeliveryState deliveryState = deliveryStateCaptor.getValue();
        Assertions.assertEquals(processedMessage.getLockToken(), lockToken);
        Assertions.assertEquals(Accepted.getInstance(), deliveryState);
        verify(session1.link, times(1)).closeAsync();
        verify(onTerminate, times(1)).run();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldAbandonMessageOnErroredProcessing() {
        final String session1Id = "1";
        final int session1MessagesCount = 1;

        final HashMap<Message, ServiceBusReceivedMessage> session1Messages = createMockMessages(session1Id, session1MessagesCount);
        final TestPublisher<AmqpEndpointState> session1EpStates = TestPublisher.createCold();
        session1EpStates.next(AmqpEndpointState.ACTIVE);
        final Session session1 = createMockSession(session1Id, session1Messages, session1EpStates);
        when(session1.link.updateDisposition(any(), any())).thenReturn(Mono.empty());
        final MessageSerializer serializer = createMockmessageSerializer(session1Messages);
        final ServiceBusSessionAcquirer sessionAcquirer = createMockSessionAcquirer(session1);
        final Runnable onTerminate = createMockOnTerminate();

        final int maxSessions = 1;
        final int concurrency = 1;
        final boolean autoDispositionDisabled = false;
        final Set<ServiceBusReceivedMessage> unseenMessages = values(session1Messages);
        Assertions.assertEquals(1, unseenMessages.size());
        final ServiceBusReceivedMessage processedMessage = unseenMessages.iterator().next();
        final Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
            unseenMessages.remove(context.getMessage());
            throw new RuntimeException("business-error");
        };
        final Consumer<ServiceBusErrorContext> processError = e -> { };
        final SessionsMessagePump pump = createSessionsMessagePump(sessionAcquirer, idleTimeoutDisabled, maxSessions, concurrency,
            autoDispositionDisabled, serializer, processMessage, processError, onTerminate);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.begin())
                .thenAwait()
                .thenCancel()
                .verify();
        }


        Assertions.assertTrue(unseenMessages.isEmpty());
        verify(session1.link).updateDisposition(lockTokenCaptor.capture(), deliveryStateCaptor.capture());
        final String lockToken = lockTokenCaptor.getValue();
        final DeliveryState deliveryState = deliveryStateCaptor.getValue();
        Assertions.assertEquals(processedMessage.getLockToken(), lockToken);
        Assertions.assertTrue(deliveryState instanceof Modified);
        verify(session1.link, times(1)).closeAsync();
        verify(onTerminate, times(1)).run();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldEmitErrorIfBeginInvokedMoreThanOnce() {
        final String session1Id = "1";

        final HashMap<Message, ServiceBusReceivedMessage> session1Messages = new HashMap<>(0);
        final TestPublisher<AmqpEndpointState> session1EpStates = TestPublisher.createCold();
        session1EpStates.next(AmqpEndpointState.ACTIVE);
        final Session session1 = createMockSession(session1Id, session1Messages, session1EpStates);
        final MessageSerializer serializer = mock(MessageSerializer.class);
        final ServiceBusSessionAcquirer sessionAcquirer = createMockSessionAcquirer(session1);
        final Runnable onTerminate = createMockOnTerminate();

        final int maxSessions = 1;
        final int concurrency = 1;
        final boolean autoDispositionDisabled = true;
        final Consumer<ServiceBusReceivedMessageContext> processMessage = context -> { };
        final Consumer<ServiceBusErrorContext> processError = e -> { };
        final SessionsMessagePump pump = createSessionsMessagePump(sessionAcquirer, idleTimeoutDisabled, maxSessions, concurrency,
            autoDispositionDisabled, serializer, processMessage, processError, onTerminate);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.begin())
                .thenAwait()
                .thenCancel()
                .verify();
        }

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.begin())
                .verifyErrorSatisfies(e -> {
                    Assertions.assertTrue(e instanceof TerminatedException);
                    Assertions.assertNotNull(e.getCause());
                    Assertions.assertTrue(e.getCause() instanceof IllegalStateException);
                });
        }
        verify(onTerminate, times(1)).run();
    }

    private static HashMap<Message, ServiceBusReceivedMessage> createMockMessages(String sessionId, int count) {
        // LinkedHashMap to keep the insertion order.
        final HashMap<Message, ServiceBusReceivedMessage> messages = new LinkedHashMap<>(count);
        for (int messageId = 0; messageId < count; messageId++) {
            final Message qpidMessage = mock(Message.class);
            final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
            when(message.getSessionId()).thenReturn(sessionId);
            when(message.getMessageId()).thenReturn(Integer.toString(messageId));
            when(message.getLockToken()).thenReturn(sessionId + "_" + messageId);
            messages.put(qpidMessage, message);
        }
        return messages;
    }

    @SafeVarargs
    private static MessageSerializer createMockmessageSerializer(HashMap<Message, ServiceBusReceivedMessage>... messagesMapArray) {
        final MessageSerializer serializer = mock(MessageSerializer.class);
        when(serializer.deserialize(any(Message.class), any())).thenAnswer(invocation -> {
            final Message qpidMessage = invocation.getArgument(0);
            for (int i = 0; i < messagesMapArray.length; i++) {
                final HashMap<Message, ServiceBusReceivedMessage> messagesMap = messagesMapArray[i];
                final ServiceBusReceivedMessage message = messagesMap.get(qpidMessage);
                if (message != null) {
                    return message;
                }
            }
            // message == null
            throw new IllegalArgumentException("Unexpected QPid message for lookup.");
        });
        return serializer;
    }

    private static Session createMockSession(String sessionId,
        HashMap<Message, ServiceBusReceivedMessage> messagesMap, TestPublisher<AmqpEndpointState> endpointStates) {
        final ArrayList<Message> messages = new ArrayList<>(messagesMap.keySet());

        final ServiceBusReceiveLink sessionLink = mock(ServiceBusReceiveLink.class);
        when(sessionLink.getSessionProperties()).thenReturn(Mono.just(new ServiceBusReceiveLink.SessionProperties(sessionId, null)));
        final AtomicBoolean isClosed = new AtomicBoolean(false);
        when(sessionLink.closeAsync()).thenAnswer(__ -> {
            isClosed.set(true);
            endpointStates.complete();
            return Mono.empty();
        });
        when(sessionLink.isDisposed()).then(__ -> {
            return isClosed.get();
        });
        final TestPublisher<Message> messagesPublisher = TestPublisher.createCold();
        final int size = messages.size();
        if (size == 1) {
            messagesPublisher.next(messages.get(0));
        } else if (size > 1) {
            messagesPublisher.next(messages.get(0), messages.subList(1, size).toArray(new Message[0]));
        }
        when(sessionLink.receive()).thenReturn(messagesPublisher.flux());
        when(sessionLink.getEndpointStates()).thenReturn(endpointStates.flux());

        return new Session(sessionLink, new ServiceBusReceiveLink.SessionProperties(sessionId, null));
    }

    private static ServiceBusSessionAcquirer createMockSessionAcquirer(Session... sessionsArray) {
        final Deque<Session> sessions = new ArrayDeque<>(sessionsArray.length);
        for (int i = 0; i < sessionsArray.length; i++) {
            sessions.addLast(sessionsArray[i]);
        }
        final ServiceBusSessionAcquirer sessionAcquirer = mock(ServiceBusSessionAcquirer.class);
        when(sessionAcquirer.acquire()).thenReturn(Mono.<Session>fromCallable(() -> {
            final Session session = sessions.pollFirst();
            if (session == null) {
                throw new IllegalArgumentException("Unexpected acquire() call when there are no more sessions.");
            }
            return session;
        }));
        return sessionAcquirer;
    }

    private static Runnable createMockOnTerminate() {
        return mock(Runnable.class);
    }

    @SafeVarargs
    private static Set<ServiceBusReceivedMessage> values(HashMap<Message, ServiceBusReceivedMessage>... sessionMessagesArray) {
        final Set<ServiceBusReceivedMessage> values = new HashSet<>();
        int totalSize = 0;
        for (int i = 0; i < sessionMessagesArray.length; i++) {
            values.addAll(sessionMessagesArray[i].values());
            totalSize += sessionMessagesArray[i].values().size();
        }
        Assertions.assertEquals(totalSize, values.size());
        return values;
    }

    private SessionsMessagePump createSessionsMessagePump(ServiceBusSessionAcquirer sessionAcquirer, Duration sessionIdleTimeout,
        int maxConcurrentSessions, int concurrencyPerSession, boolean autoDispositionDisabled, MessageSerializer serializer,
        Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError, Runnable onTerminate) {
        final ServiceBusReceiverInstrumentation instrumentation = mock(ServiceBusReceiverInstrumentation.class);
        when(instrumentation.getTracer()).thenReturn(mock(ServiceBusTracer.class));
        doNothing().when(onTerminate).run();
        final boolean enableAutoDisposition = !autoDispositionDisabled;
        return new SessionsMessagePump("identifier-1", "FQDN", "Orders", ServiceBusReceiveMode.PEEK_LOCK,
            instrumentation, sessionAcquirer, Duration.ZERO, sessionIdleTimeout, maxConcurrentSessions, concurrencyPerSession,
            0, enableAutoDisposition, Mono.empty(), serializer, retryPolicy, processMessage, processError, onTerminate);
    }

    private static final class VirtualTimeStepVerifier implements AutoCloseable {
        private final VirtualTimeScheduler scheduler;

        VirtualTimeStepVerifier() {
            scheduler = VirtualTimeScheduler.create();
        }

        <T> StepVerifier.Step<T> create(Supplier<Mono<T>> scenarioSupplier) {
            return StepVerifier.withVirtualTime(scenarioSupplier, () -> scheduler, Integer.MAX_VALUE);
        }

        @Override
        public void close() {
            scheduler.dispose();
        }
    }
}
