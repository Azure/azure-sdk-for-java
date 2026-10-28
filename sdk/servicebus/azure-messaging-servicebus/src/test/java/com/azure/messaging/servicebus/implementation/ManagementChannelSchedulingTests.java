// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.ChannelCacheWrapper;
import com.azure.core.amqp.implementation.RequestResponseChannel;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import com.azure.messaging.servicebus.TestUtils;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Isolated
@Execution(ExecutionMode.SAME_THREAD)
class ManagementChannelSchedulingTests {
    private static final String LINK_NAME = "schedule-link";
    private static final OffsetDateTime DUE_AT = OffsetDateTime.parse("2030-01-01T00:00:00Z");
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setMaxRetries(3)
        .setMode(AmqpRetryMode.FIXED)
        .setDelay(Duration.ofMillis(10))
        .setMaxDelay(Duration.ofSeconds(1))
        .setTryTimeout(TIMEOUT);
    private final List<Message> requests = new ArrayList<>();
    private RequestResponseChannel channel;
    private ManagementChannel management;

    @BeforeEach
    void setup() {
        channel = mock(RequestResponseChannel.class);
        when(channel.getErrorContext()).thenReturn(new SessionErrorContext("namespace", "queue"));
        TokenManager tokenManager = mock(TokenManager.class);
        when(tokenManager.getAuthorizationResults()).thenReturn(Flux.just(AmqpResponseCode.OK));
        management = new ManagementChannel(new ChannelCacheWrapper(Mono.just(channel)), "namespace", "queue",
            tokenManager, TestUtils.createMessageSerializer(), retryOptions);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2 })
    void retriesBusyResponseWithFreshRequestAndUnchangedMessages(int messageCount) {
        List<ServiceBusMessage> messages = messages(messageCount);
        long[] sequenceNumbers = messageCount == 1 ? new long[] { 42L } : new long[] { 42L, 43L };
        when(channel.sendWithAck(any(Message.class), isNull())).thenAnswer(invocation -> {
            Message request = invocation.getArgument(0);
            assertNull(request.getMessageId());
            assertNull(request.getReplyTo());
            request.setMessageId(UnsignedLong.valueOf(requests.size() + 1));
            request.setReplyTo("management-replies");
            requests.add(request);
            return Mono.just(requests.size() == 1 ? busyResponse() : success(sequenceNumbers));
        });

        StepVerifier.withVirtualTime(() -> management.schedule(messages, DUE_AT, 256 * 1024, LINK_NAME, null))
            .thenAwait(Duration.ofSeconds(1))
            .expectNextSequence(messageCount == 1 ? Collections.singletonList(42L) : Arrays.asList(42L, 43L))
            .expectComplete()
            .verify(TIMEOUT);

        assertEquals(2, requests.size());
        assertNotSame(requests.get(0), requests.get(1));
        assertEquals(((AmqpValue) requests.get(0).getBody()).getValue(),
            ((AmqpValue) requests.get(1).getBody()).getValue());
        assertEquals(LINK_NAME,
            requests.get(1).getApplicationProperties().getValue().get(ManagementConstants.ASSOCIATED_LINK_NAME_KEY));
        for (int i = 0; i < messageCount; i++) {
            assertEquals("business-" + i, messages.get(i).getMessageId());
            assertEquals(DUE_AT, messages.get(i).getScheduledEnqueueTime());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 3 })
    void respectsRetryLimitAndPreservesBusyError(int maxRetries) {
        retryOptions.setMaxRetries(maxRetries);
        when(channel.sendWithAck(any(Message.class), isNull())).thenReturn(Mono.just(busyResponse()));

        StepVerifier.withVirtualTime(() -> schedule(null))
            .thenAwait(Duration.ofSeconds(maxRetries))
            .expectErrorSatisfies(ManagementChannelSchedulingTests::assertBusy)
            .verify(TIMEOUT);

        verify(channel, times(maxRetries + 1)).sendWithAck(any(Message.class), isNull());
    }

    @Test
    void retryBudgetIsPerSubscription() {
        AtomicInteger sends = new AtomicInteger();
        when(channel.sendWithAck(any(Message.class), isNull()))
            .thenAnswer(invocation -> Mono.just(sends.incrementAndGet() % 4 == 0 ? success(42L) : busyResponse()));
        Flux<Long> operation = schedule(null);

        StepVerifier.withVirtualTime(() -> operation.concatWith(operation))
            .thenAwait(Duration.ofSeconds(6))
            .expectNext(42L, 42L)
            .verifyComplete();

        assertEquals(8, sends.get());
    }

    @Test
    void cancellationStopsDelayedRetry() {
        when(channel.sendWithAck(any(Message.class), isNull())).thenReturn(Mono.just(busyResponse()));
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.getOrSet();
        List<Long> results = new ArrayList<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        Disposable subscription = schedule(null).subscribe(results::add, error::set);
        try {
            verify(channel).sendWithAck(any(Message.class), isNull());
            subscription.dispose();
            scheduler.advanceTimeBy(Duration.ofSeconds(10));
            verify(channel).sendWithAck(any(Message.class), isNull());
            assertTrue(results.isEmpty());
            assertNull(error.get());
        } finally {
            subscription.dispose();
            VirtualTimeScheduler.reset();
        }
    }

    @ParameterizedTest
    @EnumSource(AmqpRetryMode.class)
    void observesConfiguredRetryDelay(AmqpRetryMode mode) {
        retryOptions.setMode(mode);
        when(channel.sendWithAck(any(Message.class), isNull())).thenReturn(Mono.just(busyResponse()))
            .thenReturn(Mono.just(success(42L)));

        StepVerifier.withVirtualTime(() -> schedule(null))
            .expectSubscription()
            .expectNoEvent(Duration.ofMillis(999))
            .then(() -> verify(channel).sendWithAck(any(Message.class), isNull()))
            .thenAwait(Duration.ofMillis(1))
            .expectNext(42L)
            .verifyComplete();

        verify(channel, times(2)).sendWithAck(any(Message.class), isNull());
    }

    @Test
    void doesNotRetryTransactionalBusyResponse() {
        ByteBuffer transactionId = ByteBuffer.wrap(new byte[] { 1, 2, 3 });
        ServiceBusTransactionContext context = mock(ServiceBusTransactionContext.class);
        when(context.getTransactionId()).thenReturn(transactionId);
        when(channel.sendWithAck(any(Message.class), any(DeliveryState.class))).thenAnswer(invocation -> {
            TransactionalState state = invocation.getArgument(1);
            assertEquals(Binary.create(transactionId), state.getTxnId());
            return Mono.just(busyResponse());
        });

        StepVerifier.create(schedule(context))
            .expectErrorSatisfies(ManagementChannelSchedulingTests::assertBusy)
            .verify(TIMEOUT);

        verify(channel).sendWithAck(any(Message.class), any(DeliveryState.class));
    }

    @Test
    void doesNotRetryNonTransientResponse() {
        when(channel.sendWithAck(any(Message.class), isNull()))
            .thenReturn(Mono.just(response(AmqpResponseCode.UNAUTHORIZED, AmqpErrorCondition.UNAUTHORIZED_ACCESS)));

        StepVerifier.create(schedule(null)).expectErrorSatisfies(error -> {
            ServiceBusException exception = (ServiceBusException) error;
            assertEquals(ServiceBusFailureReason.UNAUTHORIZED, exception.getReason());
            assertFalse(exception.isTransient());
        }).verify(TIMEOUT);

        verify(channel).sendWithAck(any(Message.class), isNull());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void doesNotRetryTransportFailureOrMissingResponse(boolean busyTransportError) {
        Throwable failure = busyTransportError
            ? new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "transport busy", channel.getErrorContext())
            : new TimeoutException("no response");
        when(channel.sendWithAck(any(Message.class), isNull())).thenReturn(Mono.error(failure));

        StepVerifier.create(schedule(null)).expectErrorSatisfies(error -> {
            if (busyTransportError) {
                assertTrue(error instanceof ServiceBusException);
                assertSame(failure, error.getCause());
            } else {
                assertSame(failure, error);
            }
        }).verify(TIMEOUT);

        verify(channel).sendWithAck(any(Message.class), isNull());
    }

    @Test
    void doesNotRetryDifferentTransientManagementResponse() {
        when(channel.sendWithAck(any(Message.class), isNull()))
            .thenReturn(Mono.just(response(AmqpResponseCode.INTERNAL_SERVER_ERROR, AmqpErrorCondition.INTERNAL_ERROR)));

        StepVerifier.create(schedule(null)).expectError(ServiceBusException.class).verify(TIMEOUT);

        verify(channel).sendWithAck(any(Message.class), isNull());
    }

    @Test
    void successfulResponseDoesNotRetry() {
        when(channel.sendWithAck(any(Message.class), isNull())).thenReturn(Mono.just(success(42L)));

        StepVerifier.create(schedule(null)).expectNext(42L).verifyComplete();

        verify(channel).sendWithAck(any(Message.class), isNull());
    }

    @Test
    void preservesEncodedMessageIdentityAndScheduledTime() {
        ServiceBusMessage original = messages(1).get(0);
        List<byte[]> encodedMessages = new ArrayList<>();
        when(channel.sendWithAck(any(Message.class), isNull())).thenAnswer(invocation -> {
            Message request = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) ((AmqpValue) request.getBody()).getValue();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> entries = (List<Map<String, Object>>) body.get(ManagementConstants.MESSAGES);
            Binary binary = (Binary) entries.get(0).get(ManagementConstants.MESSAGE);
            encodedMessages.add(Arrays.copyOfRange(binary.getArray(), binary.getArrayOffset(),
                binary.getArrayOffset() + binary.getLength()));
            return Mono.just(encodedMessages.size() == 1 ? busyResponse() : success(42L));
        });

        StepVerifier
            .withVirtualTime(
                () -> management.schedule(Collections.singletonList(original), DUE_AT, 256 * 1024, LINK_NAME, null))
            .thenAwait(Duration.ofSeconds(1))
            .expectNext(42L)
            .verifyComplete();

        assertEquals(2, encodedMessages.size());
        assertArrayEquals(encodedMessages.get(0), encodedMessages.get(1));
        Message decoded = Proton.message();
        decoded.decode(encodedMessages.get(1), 0, encodedMessages.get(1).length);
        assertEquals(original.getMessageId(), decoded.getMessageId());
        assertEquals(original.getSessionId(), decoded.getGroupId());
        assertEquals(Date.from(DUE_AT.toInstant()),
            decoded.getMessageAnnotations().getValue().get(Symbol.valueOf("x-opt-scheduled-enqueue-time")));
    }

    private Flux<Long> schedule(ServiceBusTransactionContext context) {
        return management.schedule(messages(1), DUE_AT, 256 * 1024, LINK_NAME, context);
    }

    private static List<ServiceBusMessage> messages(int count) {
        List<ServiceBusMessage> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            messages.add(new ServiceBusMessage("scheduled body").setMessageId("business-" + i).setSessionId("session"));
        }
        return messages;
    }

    private static Message busyResponse() {
        return response(AmqpResponseCode.SERVICE_UNAVAILABLE, AmqpErrorCondition.SERVER_BUSY_ERROR);
    }

    private static Message success(long... sequenceNumbers) {
        Message response = response(AmqpResponseCode.OK, null);
        response
            .setBody(new AmqpValue(Collections.singletonMap(ManagementConstants.SEQUENCE_NUMBERS, sequenceNumbers)));
        return response;
    }

    private static Message response(AmqpResponseCode status, AmqpErrorCondition condition) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("status-code", status.getValue());
        properties.put("status-description", "management response");
        if (condition != null) {
            properties.put("error-condition", condition.getErrorCondition());
        }
        Message response = Proton.message();
        response.setApplicationProperties(new ApplicationProperties(properties));
        return response;
    }

    private static void assertBusy(Throwable error) {
        assertTrue(error instanceof ServiceBusException);
        ServiceBusException exception = (ServiceBusException) error;
        assertEquals(ServiceBusFailureReason.SERVICE_BUSY, exception.getReason());
        assertTrue(exception.isTransient());
    }
}
