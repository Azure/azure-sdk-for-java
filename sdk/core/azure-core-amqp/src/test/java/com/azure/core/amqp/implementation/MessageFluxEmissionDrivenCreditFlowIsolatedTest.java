// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for credit flow using {@link EmissionDrivenCreditAccountingStrategy} strategy.
 * <p>
 * See <a href=
 * "https://github.com/Azure/azure-sdk-for-java/wiki/Unit-Testing#stepverifierwithvirtualtime">stepverifierwithvirtualtime</a>
 * for why this test class needs to run in Isolated mode.
 * </p>
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class MessageFluxEmissionDrivenCreditFlowIsolatedTest {
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
    public void initialFlowShouldBePrefetch() {
        final int prefetch = 100;
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux
            = new MessageFlux(upstream.flux(), prefetch, CreditFlowMode.EmissionDriven, retryPolicy);

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

        Assertions.assertEquals(prefetch, initialFlow.get());
        verify(receiver).closeAsync();
        upstream.assertCancelled();
    }

    private static Flux<AmqpEndpointState> activeThenNeverTerminate() {
        return Flux.just(AmqpEndpointState.ACTIVE).concatWith(Flux.never());
    }
}
