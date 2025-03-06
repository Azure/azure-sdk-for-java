// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.implementation.ReactorConnectionCache;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorAmqpConnection;
import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static com.azure.messaging.servicebus.ReceiverOptions.createNonSessionOptions;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests auto lock renewal feature of {@link AutoDispositionLockRenew} operator.
 */
public class FluxAutoLockRenewTest {
    private static final String NAMESPACE = "contoso.servicebus.windows.net";
    private static final String ENTITY_PATH = "queue0";
    private static final ServiceBusReceiverInstrumentation INSTRUMENTATION_NOOP
        = new ServiceBusReceiverInstrumentation(null, null, NAMESPACE, ENTITY_PATH, null, ReceiverKind.ASYNC_RECEIVER);
    private static final ServiceBusMessageSerializer MESSAGE_SERIALIZER = new ServiceBusMessageSerializer();
    private static final Runnable ON_CLIENT_CLOSE_NOOP = () -> {
    };
    private static final String CLIENT_IDENTIFIER = "client-id";
    private static final Duration LOCK_CLEANUP_INTERVAL = Duration.ofDays(5);
    private static final UUID LOCK_TOKEN_UUID = UUID.randomUUID();
    private static final String LOCK_TOKEN_STRING = LOCK_TOKEN_UUID.toString();
    private static final Duration MAX_AUTO_LOCK_RENEW_DURATION = Duration.ofSeconds(6);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private final Semaphore completionLock = new Semaphore(1);
    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void teardown() throws Exception {
        mocksCloseable.close();
        Mockito.framework().clearInlineMock(this);
    }

    @Test
    void doesNotContinueOnCancellation() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> publisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message3 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message4 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);
        when(client.beginLockRenewal(any(ServiceBusReceivedMessage.class))).thenReturn(Disposables.single());
        final AutoDispositionLockRenew autoComplete = autoLockRenewOperator(publisher.flux(), client);

        // Act
        StepVerifier.create(autoComplete)
            .then(() -> publisher.next(message1, message2, message3, message4))
            .thenConsumeWhile(m -> m != message2)
            .thenCancel()
            .verify(DEFAULT_TIMEOUT);

        // Assert
        publisher.assertWasCancelled();
    }

    /**
     * Test that the function to renew lock is invoked. It will verify 1. the renewal happened multiple
     * times. 2. The updated new lockedUntil is reflected on ServiceBusReceivedMessage instance.
     */
    @Test
    public void lockRenewedMultipleTimes() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> publisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage(BinaryData.fromString("message"));
        final Duration initialLockedDuration = Duration.ofSeconds(2);
        final Duration renewedLockDuration = Duration.ofSeconds(1);
        final int expectedRenewalCalls = 3;
        final long processingTime
            = computeProcessingTime(initialLockedDuration, renewedLockDuration, expectedRenewalCalls);
        message.setLockToken(LOCK_TOKEN_UUID);
        message.setLockedUntil(OffsetDateTime.now().plus(initialLockedDuration));
        final LockRenewalHandler lockRenewalHandler = new LockRenewalHandler(renewedLockDuration);
        final ServiceBusReceiverAsyncClient client = createClient(INSTRUMENTATION_NOOP, lockRenewalHandler);
        final AutoDispositionLockRenew autoLockRenewal = autoLockRenewOperator(publisher.flux(), client);

        StepVerifier.create(autoLockRenewal).then(() -> publisher.emit(message)).assertNext(m -> {
            final OffsetDateTime lockedUntilBefore = m.getLockedUntil();
            sleep(processingTime);
            final OffsetDateTime lockedUntilAfter = m.getLockedUntil();
            Assertions.assertTrue(lockedUntilAfter.isAfter(lockedUntilBefore));
            Assertions.assertEquals(LOCK_TOKEN_STRING, m.getLockToken());
        }).expectComplete().verify(DEFAULT_TIMEOUT);

        final int actualRenewalCalls = lockRenewalHandler.getRenewCount();
        assertTrue(actualRenewalCalls >= expectedRenewalCalls,
            "Lock should be renewed for at least least " + expectedRenewalCalls + ". but was " + actualRenewalCalls);
    }

    @Test
    public void lockRenewedMultipleTimeWithTracing() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> publisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage(BinaryData.fromString("message"));
        final Duration initialLockedDuration = Duration.ofSeconds(2);
        final Duration renewedLockDuration = Duration.ofSeconds(1);
        final int expectedRenewalCalls = 3;
        final long processingTime
            = computeProcessingTime(initialLockedDuration, renewedLockDuration, expectedRenewalCalls);
        message.setLockToken(LOCK_TOKEN_UUID);
        message.setLockedUntil(OffsetDateTime.now().plus(initialLockedDuration));
        final LockRenewalHandler lockRenewalHandler = new LockRenewalHandler(renewedLockDuration);

        final Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        when(tracer.start(eq("ServiceBus.renewMessageLock"), any(StartSpanOptions.class), any())).thenAnswer(
            invocation -> invocation.getArgument(2, Context.class).addData(PARENT_TRACE_CONTEXT_KEY, "span"));

        final ServiceBusReceiverAsyncClient client = createClient(createInstrumentation(tracer), lockRenewalHandler);
        final AutoDispositionLockRenew autoLockRenewal = autoLockRenewOperator(publisher.flux(), client);

        StepVerifier.create(autoLockRenewal).then(() -> publisher.emit(message)).assertNext(m -> {
            final OffsetDateTime lockedUntilBefore = m.getLockedUntil();
            sleep(processingTime);
            final OffsetDateTime lockedUntilAfter = m.getLockedUntil();
            Assertions.assertTrue(lockedUntilAfter.isAfter(lockedUntilBefore));
            Assertions.assertEquals(LOCK_TOKEN_STRING, m.getLockToken());
        }).expectComplete().verify(DEFAULT_TIMEOUT);

        final int actualRenewalCalls = lockRenewalHandler.getRenewCount();
        assertTrue(actualRenewalCalls >= expectedRenewalCalls,
            "Lock should be renewed for at least least " + expectedRenewalCalls + ". but was " + actualRenewalCalls);

        // we might not have got all the spans yet.
        verify(tracer, atLeast(actualRenewalCalls - 1)).extractContext(any());
        verify(tracer, atLeast(actualRenewalCalls - 1)).start(eq("ServiceBus.renewMessageLock"),
            any(StartSpanOptions.class), any(Context.class));
        verify(tracer, atLeast(actualRenewalCalls - 1)).end(isNull(), isNull(), any(Context.class));
    }

    @Test
    void renewFailsWithTracing() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> publisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage(BinaryData.fromString("message"));
        final Duration initialLockedDuration = Duration.ofSeconds(2);
        message.setLockToken(LOCK_TOKEN_UUID);
        message.setLockedUntil(OffsetDateTime.now().plus(initialLockedDuration));

        final CountDownLatch latch = new CountDownLatch(1);
        final RuntimeException renewalError = new RuntimeException("lock-lost");
        final Mono<OffsetDateTime> renewMono
            = Mono.error(renewalError).map(i -> OffsetDateTime.now()).doFinally(st -> latch.countDown());
        final LockRenewalHandler lockRenewalHandler = new LockRenewalHandler(renewMono);

        final Tracer tracer = mock(Tracer.class);
        when(tracer.isEnabled()).thenReturn(true);
        when(tracer.start(eq("ServiceBus.renewMessageLock"), any(StartSpanOptions.class), any())).thenAnswer(
            invocation -> invocation.getArgument(2, Context.class).addData(PARENT_TRACE_CONTEXT_KEY, "span"));

        final ServiceBusReceiverAsyncClient client = createClient(createInstrumentation(tracer), lockRenewalHandler);
        final AutoDispositionLockRenew autoLockRenewal = autoLockRenewOperator(publisher.flux(), client);

        // Act
        StepVerifier.create(autoLockRenewal).then(() -> publisher.emit(message)).assertNext(m -> {
            try {
                assertTrue(latch.await(20, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                fail(e);
            }
        }).expectComplete().verify(DEFAULT_TIMEOUT);

        // Assert
        verify(tracer, times(1)).extractContext(any());
        verify(tracer, times(1)).start(eq("ServiceBus.renewMessageLock"), any(StartSpanOptions.class),
            any(Context.class));
        verify(tracer, times(1)).end(isNull(), same(renewalError), any(Context.class));
    }

    @Test
    void autoCompleteDisabledLockRenewNotDisposed() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> publisher = TestPublisher.createCold();
        final Disposable lockRenewalDisposable = Disposables.single();
        final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage(BinaryData.fromString("message"));
        message.setLockToken(LOCK_TOKEN_UUID);
        message.setLockedUntil(OffsetDateTime.now().plus(Duration.ofSeconds(2)));
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);
        when(client.beginLockRenewal(any(ServiceBusReceivedMessage.class))).thenReturn(lockRenewalDisposable);
        final AutoDispositionLockRenew autoLockRenewal = autoLockRenewOperator(publisher.flux(), client);
        final FluxAutoCompleteTest.TestCoreSubscriber subscriber = new FluxAutoCompleteTest.TestCoreSubscriber(0);

        // Act
        autoLockRenewal.subscribe(subscriber);
        publisher.emit(message);

        // Assert
        assertFalse(lockRenewalDisposable.isDisposed());
    }

    @Test
    void autoCompleteEnabledLockRenewDisposed() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> publisher = TestPublisher.createCold();
        final Disposable lockRenewalDisposable = Disposables.single();
        final ServiceBusReceivedMessage message = new ServiceBusReceivedMessage(BinaryData.fromString("message"));
        message.setLockToken(LOCK_TOKEN_UUID);
        message.setLockedUntil(OffsetDateTime.now().plus(Duration.ofSeconds(2)));
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);
        when(client.beginLockRenewal(any(ServiceBusReceivedMessage.class))).thenReturn(lockRenewalDisposable);
        final AutoDispositionLockRenew autoLockRenewal
            = new AutoDispositionLockRenew(publisher.flux(), client, true, true, completionLock);
        final FluxAutoCompleteTest.TestCoreSubscriber subscriber = new FluxAutoCompleteTest.TestCoreSubscriber(1);

        // Act
        autoLockRenewal.subscribe(subscriber);
        publisher.emit(message);

        // Assert
        assertFalse(subscriber.onErrorInvocations.isEmpty(), "Error should have received.");
        assertTrue(lockRenewalDisposable.isDisposed());
    }

    @Test
    void propagatesContext() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> publisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);
        final AutoDispositionLockRenew operator
            = new AutoDispositionLockRenew(publisher.flux(), client, false, false, completionLock);

        StepVerifier.create(operator.contextWrite((context) -> context.put("A", "B")))
            .thenRequest(1)
            .expectAccessibleContext()
            .contains("A", "B")
            .hasSize(1)
            .then()
            .then(() -> publisher.next(message))
            .expectNext(message)
            .thenCancel()
            .verify(DEFAULT_TIMEOUT);
    }

    @SuppressWarnings("unchecked")
    private ServiceBusReceiverAsyncClient createClient(ServiceBusReceiverInstrumentation instrumentation,
        LockRenewalHandler lockRenewalHandler) {
        final ServiceBusReactorAmqpConnection connection = mock(ServiceBusReactorAmqpConnection.class);
        final ServiceBusManagementNode managementNode = mock(ServiceBusManagementNode.class);
        when(managementNode.renewMessageLock(any(String.class), any())).thenAnswer(lockRenewalHandler);
        when(connection.getManagementNode(ENTITY_PATH, MessagingEntityType.QUEUE))
            .thenReturn(Mono.just(managementNode));
        final ReactorConnectionCache<ServiceBusReactorAmqpConnection> connectionCache
            = mock(ReactorConnectionCache.class);
        when(connectionCache.get()).thenReturn(Mono.just(connection));
        final ReceiverOptions options
            = createNonSessionOptions(ServiceBusReceiveMode.RECEIVE_AND_DELETE, 1, MAX_AUTO_LOCK_RENEW_DURATION, false);
        return new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE, options,
            connectionCache, LOCK_CLEANUP_INTERVAL, instrumentation, MESSAGE_SERIALIZER, ON_CLIENT_CLOSE_NOOP,
            CLIENT_IDENTIFIER);
    }

    private AutoDispositionLockRenew autoLockRenewOperator(Flux<ServiceBusReceivedMessage> messageFlux,
        ServiceBusReceiverAsyncClient client) {
        return new AutoDispositionLockRenew(messageFlux, client, false, true, completionLock);
    }

    private static ServiceBusReceiverInstrumentation createInstrumentation(Tracer tracer) {
        return new ServiceBusReceiverInstrumentation(tracer, null, NAMESPACE, ENTITY_PATH, null,
            ReceiverKind.ASYNC_RECEIVER);
    }

    private static long computeProcessingTime(Duration initialLockedDuration, Duration renewedLockDuration,
        int expectedRenewalCalls) {
        final long millis
            = initialLockedDuration.plus(renewedLockDuration.multipliedBy(expectedRenewalCalls + 1)).toMillis();
        return TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS);
    }

    private static void sleep(long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            fail("sleep simulating processing interrupted.", e);
        }
    }

    private static final class LockRenewalHandler implements Answer<Mono<OffsetDateTime>> {
        private final AtomicInteger renewCount = new AtomicInteger(0);
        private final Mono<OffsetDateTime> renewalMono;
        private final Duration lockedDuration;

        LockRenewalHandler(Duration lockedDuration) {
            this.lockedDuration = lockedDuration;
            this.renewalMono = null;
        }

        LockRenewalHandler(Mono<OffsetDateTime> renewalMono) {
            this.renewalMono = renewalMono;
            this.lockedDuration = null;
        }

        @Override
        public Mono<OffsetDateTime> answer(InvocationOnMock invocation) {
            renewCount.incrementAndGet();
            if (renewalMono != null) {
                return renewalMono;
            }
            assert lockedDuration != null;
            return Mono.just(OffsetDateTime.now().plus(lockedDuration));
        }

        public int getRenewCount() {
            return renewCount.get();
        }
    }
}
