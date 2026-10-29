// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import reactor.core.Disposable;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class ServiceBusSessionAcquirerIsolatedTest {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusSessionAcquirerIsolatedTest.class);
    private static final String IDENTIFIER = "identifier";
    private static final String ENTITY_PATH = "q0";
    private static final MessagingEntityType ENTITY_TYPE = MessagingEntityType.QUEUE;
    private static final ServiceBusReceiveMode RECEIVE_MODE = ServiceBusReceiveMode.PEEK_LOCK;
    private static final Duration TRY_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration RETRY_BACKOFF = Duration.ofSeconds(2);
    private static final Duration AWAIT_DURATION = TRY_TIMEOUT.plusSeconds(5);
    private static final AmqpException BROKER_TIMEOUT_ERROR = new AmqpException(true, AmqpErrorCondition.TIMEOUT_ERROR,
        "com.microsoft:timeout", new AmqpErrorContext(ENTITY_PATH));
    private static final TimeoutException CLIENT_TIMEOUT_ERROR = new TimeoutException("client-side-timeout");

    private AutoCloseable mocksCloseable;

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
    public void shouldMapThenPropagateBrokerTimeoutErrorIfRetryDisabled() {
        final Deque<Mono<ServiceBusReceiveLink>> sessionLinks = new ArrayDeque<>(2);
        sessionLinks.add(Mono.error(BROKER_TIMEOUT_ERROR));
        sessionLinks.add(Mono.error(BROKER_TIMEOUT_ERROR));
        final OnCreateSessionLink onCreateSessionLink = new OnCreateSessionLink(sessionLinks);
        final ConnectionCacheWrapper cacheWrapper = createMockConnectionWrapper(onCreateSessionLink);
        final ServiceBusSessionAcquirer sessionAcquirer = createSessionAcquirer(cacheWrapper, true);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(sessionAcquirer::acquire).thenAwait(AWAIT_DURATION).verifyErrorSatisfies(e -> {
                Assertions.assertInstanceOf(TimeoutException.class, e);
                Assertions.assertNotNull(e.getCause());
                final Throwable cause = e.getCause();
                Assertions.assertEquals(BROKER_TIMEOUT_ERROR, cause);
            });
        }
        Assertions.assertEquals(1, onCreateSessionLink.pending()); // Assert that first error itself is propagated.
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldPropagateAnyErrorIfRetryDisabled() {
        final Deque<Mono<ServiceBusReceiveLink>> sessionLinks = new ArrayDeque<>(1);
        final RuntimeException error = new RuntimeException();
        sessionLinks.add(Mono.error(error));
        final OnCreateSessionLink onCreateSessionLink = new OnCreateSessionLink(sessionLinks);
        final ConnectionCacheWrapper cacheWrapper = createMockConnectionWrapper(onCreateSessionLink);
        final ServiceBusSessionAcquirer sessionAcquirer = createSessionAcquirer(cacheWrapper, true);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(sessionAcquirer::acquire).thenAwait(AWAIT_DURATION).verifyErrorSatisfies(e -> {
                Assertions.assertEquals(error, e);
            });
        }
        Assertions.assertEquals(0, onCreateSessionLink.pending());
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldClientSideTimeoutAndCloseLinkWhenAcquireHangsIfRetryDisabled() {
        // A receive link whose getSessionProperties() never emits simulates a hung acquire where the
        // broker accepts the link but never sends its detach. Without a client-side guard this would
        // block the caller for the full operation timeout (issue #49093).
        final ServiceBusReceiveLink hungLink = mock(ServiceBusReceiveLink.class);
        when(hungLink.getSessionProperties()).thenReturn(Mono.never());
        final PublisherProbe<Void> close = PublisherProbe.empty();
        when(hungLink.closeAsync()).thenReturn(close.mono());
        final Deque<Mono<ServiceBusReceiveLink>> sessionLinks = new ArrayDeque<>(1);
        sessionLinks.add(Mono.just(hungLink));
        final OnCreateSessionLink onCreateSessionLink = new OnCreateSessionLink(sessionLinks);
        final ConnectionCacheWrapper cacheWrapper = createMockConnectionWrapper(onCreateSessionLink);
        final ServiceBusSessionAcquirer sessionAcquirer = createSessionAcquirer(cacheWrapper, true);

        // The client-side guard is 2 * tryTimeout; await slightly longer so it fires.
        final Duration awaitPastGuard = TRY_TIMEOUT.multipliedBy(2).plusSeconds(1);
        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(sessionAcquirer::acquire).thenAwait(awaitPastGuard).verifyErrorSatisfies(e -> {
                Assertions.assertInstanceOf(TimeoutException.class, e);
            });
        }
        Assertions.assertEquals(0, onCreateSessionLink.pending());
        close.assertWasSubscribed();
        Mockito.verify(hungLink, Mockito.never()).dispose();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void shouldCloseLinkWithoutBlockingOnTimeout(boolean timeoutRetryDisabled) throws Exception {
        final ServiceBusReceiveLink link = createHungReceiveLink();
        final CountDownLatch closed = new CountDownLatch(1);
        final AtomicBoolean nonBlockingThread = new AtomicBoolean();
        when(link.closeAsync()).thenReturn(Mono.defer(() -> {
            nonBlockingThread.set(Schedulers.isInNonBlockingThread());
            closed.countDown();
            return Mono.never();
        }));
        final RuntimeException terminalError = new RuntimeException("stop retries");
        final Deque<Mono<ServiceBusReceiveLink>> links = new ArrayDeque<>();
        links.add(Mono.just(link));
        links.add(Mono.error(terminalError));
        final ConnectionCacheWrapper wrapper = createMockConnectionWrapper(new OnCreateSessionLink(links));
        when(wrapper.getRetryOptions()).thenReturn(new AmqpRetryOptions().setDelay(Duration.ofMillis(10)));
        final ServiceBusSessionAcquirer acquirer = new ServiceBusSessionAcquirer(LOGGER, IDENTIFIER, ENTITY_PATH,
            ENTITY_TYPE, RECEIVE_MODE, Duration.ofMillis(100), timeoutRetryDisabled, wrapper);
        final Queue<Throwable> droppedErrors = new ConcurrentLinkedQueue<>();
        Hooks.onErrorDropped(droppedErrors::add);
        try {
            StepVerifier.create(acquirer.acquire())
                .expectErrorMatches(
                    error -> timeoutRetryDisabled ? error instanceof TimeoutException : error == terminalError)
                .verify(Duration.ofSeconds(5));
            Assertions.assertTrue(closed.await(5, TimeUnit.SECONDS));
            Assertions.assertTrue(nonBlockingThread.get());
            Assertions.assertTrue(droppedErrors.isEmpty(), droppedErrors.toString());
            Mockito.verify(link).closeAsync();
            Mockito.verify(link, Mockito.never()).dispose();
        } finally {
            Hooks.resetOnErrorDropped();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void shouldCloseLinkOnCallerCancellation(boolean timeoutRetryDisabled) throws Exception {
        final ServiceBusReceiveLink link = createHungReceiveLink();
        final PublisherProbe<Void> close = PublisherProbe.of(Mono.never());
        when(link.closeAsync()).thenReturn(close.mono());
        final Deque<Mono<ServiceBusReceiveLink>> links = new ArrayDeque<>();
        links.add(Mono.just(link));
        final ServiceBusSessionAcquirer acquirer
            = createSessionAcquirer(createMockConnectionWrapper(new OnCreateSessionLink(links)), timeoutRetryDisabled);

        final Disposable acquisition = acquirer.acquire("session-id").subscribe();
        final CountDownLatch cancelled = new CountDownLatch(1);
        Schedulers.parallel().schedule(() -> {
            acquisition.dispose();
            cancelled.countDown();
        });
        Assertions.assertTrue(cancelled.await(5, TimeUnit.SECONDS));

        close.assertWasSubscribed();
        close.assertWasNotCancelled();
        Mockito.verify(link).closeAsync();
        Mockito.verify(link, Mockito.never()).dispose();
    }

    @Test
    public void shouldHandleCloseErrorWithoutDroppingItOnCancellation() {
        final ServiceBusReceiveLink link = mock(ServiceBusReceiveLink.class);
        when(link.getSessionProperties()).thenReturn(Mono.never());
        final PublisherProbe<Void> close = PublisherProbe.of(Mono.error(new IOException("close failed")));
        when(link.closeAsync()).thenReturn(close.mono());
        final Deque<Mono<ServiceBusReceiveLink>> links = new ArrayDeque<>();
        links.add(Mono.just(link));
        final ServiceBusSessionAcquirer acquirer
            = createSessionAcquirer(createMockConnectionWrapper(new OnCreateSessionLink(links)), true);
        final Queue<Throwable> droppedErrors = new ConcurrentLinkedQueue<>();
        Hooks.onErrorDropped(droppedErrors::add);
        try {
            acquirer.acquire().subscribe().dispose();
            close.assertWasSubscribed();
            Assertions.assertTrue(droppedErrors.isEmpty(), droppedErrors.toString());
        } finally {
            Hooks.resetOnErrorDropped();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void shouldTransferLinkOwnershipOnSuccess(boolean timeoutRetryDisabled) {
        final ServiceBusReceiveLink link = mock(ServiceBusReceiveLink.class);
        final ServiceBusReceiveLink.SessionProperties properties = mock(ServiceBusReceiveLink.SessionProperties.class);
        when(link.getSessionProperties()).thenReturn(Mono.just(properties));
        final Deque<Mono<ServiceBusReceiveLink>> links = new ArrayDeque<>();
        links.add(Mono.just(link));
        final ServiceBusSessionAcquirer acquirer
            = createSessionAcquirer(createMockConnectionWrapper(new OnCreateSessionLink(links)), timeoutRetryDisabled);

        StepVerifier.create(acquirer.acquire())
            .assertNext(session -> Assertions.assertSame(link, session.getLink()))
            .verifyComplete();

        Mockito.verify(link, Mockito.never()).closeAsync();
        Mockito.verify(link, Mockito.never()).dispose();
    }

    private ServiceBusReceiveLink createHungReceiveLink() {
        final ServiceBusReceiveLink link = mock(ServiceBusReceiveLink.class);
        when(link.getSessionProperties()).thenReturn(Mono.never());
        // Match ReactorReceiver.dispose(): synchronous close blocks on the asynchronous close operation.
        Mockito.doAnswer(invocation -> {
            link.closeAsync().block(Duration.ofMillis(100));
            return null;
        }).when(link).dispose();
        return link;
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldBackOffBetweenRetriesIfRetryEnabled() {
        // First attempt fails with a (retriable) broker timeout, then a terminal error. The retry must
        // wait for the backoff before the second acquire attempt, i.e. it must not spin (issue #49093).
        final Deque<Mono<ServiceBusReceiveLink>> sessionLinks = new ArrayDeque<>(2);
        sessionLinks.add(Mono.error(BROKER_TIMEOUT_ERROR));
        final RuntimeException error = new RuntimeException();
        sessionLinks.add(Mono.error(error));
        final OnCreateSessionLink onCreateSessionLink = new OnCreateSessionLink(sessionLinks);
        final ConnectionCacheWrapper cacheWrapper = createMockConnectionWrapper(onCreateSessionLink);
        final ServiceBusSessionAcquirer sessionAcquirer = createSessionAcquirer(cacheWrapper, false);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(sessionAcquirer::acquire)
                .thenAwait(RETRY_BACKOFF.dividedBy(2))
                // Before the backoff elapses the second attempt must not have been made yet.
                .then(() -> Assertions.assertEquals(1, onCreateSessionLink.pending()))
                .thenAwait(RETRY_BACKOFF)
                .verifyErrorSatisfies(e -> Assertions.assertEquals(error, e));
        }
        Assertions.assertEquals(0, onCreateSessionLink.pending());
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRetryOnBrokerTimeoutErrorIfRetryEnabled() {
        final Deque<Mono<ServiceBusReceiveLink>> sessionLinks = new ArrayDeque<>(2);
        sessionLinks.add(Mono.error(BROKER_TIMEOUT_ERROR));
        sessionLinks.add(Mono.error(BROKER_TIMEOUT_ERROR));
        final RuntimeException error = new RuntimeException();
        sessionLinks.add(Mono.error(error));
        final OnCreateSessionLink onCreateSessionLink = new OnCreateSessionLink(sessionLinks);
        final ConnectionCacheWrapper cacheWrapper = createMockConnectionWrapper(onCreateSessionLink);
        final ServiceBusSessionAcquirer sessionAcquirer = createSessionAcquirer(cacheWrapper, false);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(sessionAcquirer::acquire).thenAwait(AWAIT_DURATION).verifyErrorSatisfies(e -> {
                Assertions.assertEquals(error, e);
            });
        }
        Assertions.assertEquals(0, onCreateSessionLink.pending());
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRetryOnTimeoutErrorIfRetryEnabled() {
        final Deque<Mono<ServiceBusReceiveLink>> sessionLinks = new ArrayDeque<>(2);
        sessionLinks.add(Mono.error(BROKER_TIMEOUT_ERROR));
        sessionLinks.add(Mono.error(CLIENT_TIMEOUT_ERROR));
        final RuntimeException error = new RuntimeException();
        sessionLinks.add(Mono.error(error));
        final OnCreateSessionLink onCreateSessionLink = new OnCreateSessionLink(sessionLinks);
        final ConnectionCacheWrapper cacheWrapper = createMockConnectionWrapper(onCreateSessionLink);
        final ServiceBusSessionAcquirer sessionAcquirer = createSessionAcquirer(cacheWrapper, false);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(sessionAcquirer::acquire).thenAwait(AWAIT_DURATION).verifyErrorSatisfies(e -> {
                Assertions.assertEquals(error, e);
            });
        }
        Assertions.assertEquals(0, onCreateSessionLink.pending());
    }

    private ConnectionCacheWrapper createMockConnectionWrapper(OnCreateSessionLink onCreateSessionLink) {
        final ServiceBusAmqpConnection connection = mock(ServiceBusAmqpConnection.class);
        when(connection.createReceiveLink(anyString(), anyString(), any(ServiceBusReceiveMode.class), any(),
            any(MessagingEntityType.class), anyString(), any())).thenAnswer(onCreateSessionLink);

        final ConnectionCacheWrapper cacheWrapper = Mockito.mock(ConnectionCacheWrapper.class);
        when(cacheWrapper.isV2()).thenReturn(true);
        when(cacheWrapper.getConnection()).thenReturn(Mono.just(connection));
        when(cacheWrapper.getRetryOptions()).thenReturn(new AmqpRetryOptions().setDelay(RETRY_BACKOFF));
        return cacheWrapper;
    }

    private ServiceBusSessionAcquirer createSessionAcquirer(ConnectionCacheWrapper cacheWrapper,
        boolean isTimeoutRetryDisabled) {
        return new ServiceBusSessionAcquirer(LOGGER, IDENTIFIER, ENTITY_PATH, ENTITY_TYPE, RECEIVE_MODE, TRY_TIMEOUT,
            isTimeoutRetryDisabled, cacheWrapper);
    }

    private static final class OnCreateSessionLink implements Answer<Mono<ServiceBusReceiveLink>> {
        final Deque<Mono<ServiceBusReceiveLink>> sessionLinks = new ArrayDeque<>();

        OnCreateSessionLink(Deque<Mono<ServiceBusReceiveLink>> sessionLinks) {
            this.sessionLinks.addAll(sessionLinks);
        }

        int pending() {
            return sessionLinks.size();
        }

        @Override
        public Mono<ServiceBusReceiveLink> answer(InvocationOnMock invocation) {
            final Mono<ServiceBusReceiveLink> link = sessionLinks.poll();
            if (link == null) {
                throw new IllegalStateException("unexpected request when there are no more session links.");
            }
            return link;
        }
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
