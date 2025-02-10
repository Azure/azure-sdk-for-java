// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for credit flow using {@link RequestDrivenCreditAccountingStrategy} strategy.
 * <p>
 * See <a href=
 * "https://github.com/Azure/azure-sdk-for-java/wiki/Unit-Testing#stepverifierwithvirtualtime">stepverifierwithvirtualtime</a>
 * for why this test class needs to run in Isolated mode.
 * </p>
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class MessageFluxRequestDrivenCreditFlowIsolatedTest {
    private static final int MAX_RETRY = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(3);
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setMaxRetries(MAX_RETRY).setDelay(RETRY_DELAY);
    private final AmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(retryOptions);
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
    public void initialFlowShouldBeSumOfRequestAndPrefetch() {
        final int prefetch = 100;
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux
            = new MessageFlux(upstream.flux(), prefetch, CreditFlowMode.RequestDriven, retryPolicy);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        when(receiver.receive()).thenReturn(Flux.never());
        when(receiver.getEndpointStates()).thenReturn(activeThenNeverTerminate());
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        final AtomicLong initialFlow = new AtomicLong();
        doAnswer(invocation -> {
            final Supplier<Long> creditSupplier = invocation.getArgument(0);
            Assertions.assertNotNull(creditSupplier);
            initialFlow.addAndGet(creditSupplier.get());
            return null;
        }).when(receiver).addCredit(any());

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .thenRequest(10)
                .then(() -> upstream.next(receiver))
                .then(() -> upstream.complete())
                .verifyComplete();
        }

        Assertions.assertEquals(prefetch + 10, initialFlow.get());
        verify(receiver).closeAsync();
        upstream.assertCancelled();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldSendFlowWhenRequestAccumulatedEqualsPrefetch() {
        final int prefetch = 100;
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux
            = new MessageFlux(upstream.flux(), prefetch, CreditFlowMode.RequestDriven, retryPolicy);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        when(receiver.receive()).thenReturn(Flux.never());
        when(receiver.getEndpointStates()).thenReturn(activeThenNeverTerminate());
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        final AtomicInteger flowCalls = new AtomicInteger();
        final AtomicLong firstFlow = new AtomicLong();
        final AtomicLong secondFlow = new AtomicLong();
        doAnswer(invocation -> {
            final Supplier<Long> creditSupplier = invocation.getArgument(0);
            Assertions.assertNotNull(creditSupplier);

            final int calls = flowCalls.incrementAndGet();
            if (calls == 1) {
                firstFlow.set(creditSupplier.get());
            } else if (calls == 2) {
                secondFlow.set(creditSupplier.get());
            }
            return null;
        }).when(receiver).addCredit(any());

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(() -> upstream.next(receiver))
                .thenRequest(10)    // 10 - 0 + 100 =
                // 110
                // [accumulatedRequest_110
                // >= 100]
                .thenAwait()
                .thenRequest(20)    // 30 - 110 + 100 = 20 [accumulatedRequest_20 < 100]
                .thenRequest(20)    // 50 - 130 + 100 = 20 [accumulatedRequest_40 < 100]
                .thenRequest(20)    // 70 - 150 + 100 = 20 [accumulatedRequest_60 < 100]
                .thenRequest(20)    // 90 - 170 + 100 = 20 [accumulatedRequest_80 < 100]
                .thenRequest(20)    // 110 - 190 + 100 = 20 [accumulatedRequest_100 >= 100]
                .then(() -> upstream.complete())
                .verifyComplete();
        }

        Assertions.assertEquals(2, flowCalls.get());
        Assertions.assertEquals(prefetch + 10, firstFlow.get());
        Assertions.assertEquals(prefetch, secondFlow.get());
        verify(receiver).closeAsync();
        upstream.assertCancelled();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldSendFlowWhenRequestAccumulatedGreaterThanPrefetch() {
        final int prefetch = 100;
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux
            = new MessageFlux(upstream.flux(), prefetch, CreditFlowMode.RequestDriven, retryPolicy);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        when(receiver.receive()).thenReturn(Flux.never());
        when(receiver.getEndpointStates()).thenReturn(activeThenNeverTerminate());
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        final AtomicInteger flowCalls = new AtomicInteger();
        final AtomicLong firstFlow = new AtomicLong();
        final AtomicLong secondFlow = new AtomicLong();
        doAnswer(invocation -> {
            final Supplier<Long> creditSupplier = invocation.getArgument(0);
            Assertions.assertNotNull(creditSupplier);

            final int calls = flowCalls.incrementAndGet();
            if (calls == 1) {
                firstFlow.set(creditSupplier.get());
            } else if (calls == 2) {
                secondFlow.set(creditSupplier.get());
            }
            return null;
        }).when(receiver).addCredit(any());

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(() -> upstream.next(receiver))
                .thenRequest(10)    // 10 - 0 + 100 =
                // 110
                // [accumulatedRequest_110
                // >= 100]
                .thenAwait()
                .thenRequest(20)    // 30 - 110 + 100 = 20 [accumulatedRequest_20 < 100]
                .thenRequest(20)    // 50 - 130 + 100 = 20 [accumulatedRequest_40 < 100]
                .thenRequest(20)    // 70 - 150 + 100 = 20 [accumulatedRequest_60 < 100]
                .thenRequest(20)    // 90 - 170 + 100 = 20 [accumulatedRequest_80 < 100]
                .thenRequest(30)    // 120 - 190 + 100 = 30 [accumulatedRequest_110 >= 100]
                .then(() -> upstream.complete())
                .verifyComplete();
        }

        Assertions.assertEquals(2, flowCalls.get());
        Assertions.assertEquals(prefetch + 10, firstFlow.get());
        Assertions.assertEquals(prefetch + 10, secondFlow.get());
        verify(receiver).closeAsync();
        upstream.assertCancelled();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldSendFlowOnRequestWhenNoPrefetch() {
        final int prefetch = 0;
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux
            = new MessageFlux(upstream.flux(), prefetch, CreditFlowMode.RequestDriven, retryPolicy);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        when(receiver.receive()).thenReturn(Flux.never());
        when(receiver.getEndpointStates()).thenReturn(activeThenNeverTerminate());
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        final AtomicInteger flowCalls = new AtomicInteger();
        final AtomicLong firstFlow = new AtomicLong();
        final AtomicLong secondFlow = new AtomicLong();
        final AtomicLong thirdFlow = new AtomicLong();
        final AtomicLong fourthFlow = new AtomicLong();
        doAnswer(invocation -> {
            final Supplier<Long> creditSupplier = invocation.getArgument(0);
            Assertions.assertNotNull(creditSupplier);

            final int calls = flowCalls.incrementAndGet();
            if (calls == 1) {
                firstFlow.set(creditSupplier.get());
            } else if (calls == 2) {
                secondFlow.set(creditSupplier.get());
            } else if (calls == 3) {
                thirdFlow.set(creditSupplier.get());
            } else if (calls == 4) {
                fourthFlow.set(creditSupplier.get());
            }
            return null;
        }).when(receiver).addCredit(any());

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(() -> upstream.next(receiver))
                .thenRequest(10)   // 10 - 0 + 0 = 10
                // [accumulatedRequest_10
                // >= 0]
                .thenAwait()
                .thenRequest(20)   // 30 - 10 + 0 = 20 [accumulatedRequest_20 >= 0]
                .thenRequest(30)   // 60 - 30 + 0 = 30 [accumulatedRequest_30 >= 0]
                .thenRequest(40)   // 100 - 60 + 0 = 40 [accumulatedRequest_40 >= 0]
                .then(() -> upstream.complete())
                .verifyComplete();
        }

        Assertions.assertEquals(4, flowCalls.get());
        Assertions.assertEquals(10, firstFlow.get());
        Assertions.assertEquals(20, secondFlow.get());
        Assertions.assertEquals(30, thirdFlow.get());
        Assertions.assertEquals(40, fourthFlow.get());
        verify(receiver).closeAsync();
        upstream.assertCancelled();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldBoundUnboundedRequest() {
        final int prefetch = 0;
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux
            = new MessageFlux(upstream.flux(), prefetch, CreditFlowMode.RequestDriven, retryPolicy);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        final Message message = mock(Message.class);
        final int messagesCount = 4;
        when(receiver.receive()).thenReturn(Flux.range(0, messagesCount - 1).map(__ -> message));
        when(receiver.getEndpointStates()).thenReturn(activeThenNeverTerminate());
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        final ConcurrentLinkedQueue<Long> flows = new ConcurrentLinkedQueue<>();
        doAnswer(invocation -> {
            final Supplier<Long> creditSupplier = invocation.getArgument(0);
            Assertions.assertNotNull(creditSupplier);
            flows.add(creditSupplier.get());
            return null;
        }).when(receiver).addCredit(any());

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(() -> upstream.next(receiver))
                // Unbounded-Request: (Combined with no Prefetch) Switches to a mode where initially one message is
                // requested,
                // the arrival then the emission of resulting message trigger request for next message and so on...
                .thenRequest(Long.MAX_VALUE)
                .thenAwait()
                // Any requests post Unbounded-Request are ignored.
                .thenRequest(10)
                .thenRequest(20)
                .then(() -> upstream.complete())
                .thenConsumeWhile(__ -> true)
                .verifyComplete();
        }

        // With Prefetch disabled, there is going to be 'messagesCount' flow calls, each with credit 1
        Assertions.assertEquals(messagesCount, flows.size());
        flows.forEach(c -> Assertions.assertEquals(1, c));
        verify(receiver).closeAsync();
        upstream.assertCancelled();
    }

    private static Flux<AmqpEndpointState> activeThenNeverTerminate() {
        return Flux.just(AmqpEndpointState.ACTIVE).concatWith(Flux.never());
    }
}
