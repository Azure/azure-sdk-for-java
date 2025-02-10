// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.RequestResponseChannelCacheTest.MockEndpoint;
import com.azure.core.amqp.implementation.RequestResponseChannelCacheTest.MockEndpoint.ChannelState;
import org.apache.qpid.proton.engine.EndpointState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;

/**
 * Tests for {@link RequestResponseChannelCache}.
 * <p>
 * See <a href=
 * "https://github.com/Azure/azure-sdk-for-java/wiki/Unit-Testing#stepverifierwithvirtualtime">stepverifierwithvirtualtime</a>
 * for why this test class needs to run in Isolated mode.
 * </p>
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class RequestResponseChannelCacheIsolatedTest {
    private static final String CON_ID = "MF_0f4c2e_1680070221023";
    private static final String CH_ENTITY_PATH = "orders";
    private static final String CH_SESSION_NAME = "cbs-session";
    private static final String CH_LINK_NAME = "cbs";
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration VIRTUAL_TIME_SHIFT = OPERATION_TIMEOUT.plusSeconds(30);
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(OPERATION_TIMEOUT);
    private final FixedAmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(retryOptions);
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
    public void shouldRetryOnChannelActiveTimeout() {
        final int channelsCount = 2;
        final Deque<ChannelState> channelStates = new ArrayDeque<>(channelsCount);
        // The state for the first channel that never emit, so the wait for the channel to 'active' timeout.
        channelStates.add(ChannelState.never());
        // The state for the second channel that will be active.
        channelStates.add(ChannelState.as(EndpointState.ACTIVE));

        final MockEndpoint endpoint = createEndpoint(CON_ID, channelStates);
        endpoint.arrange();
        final RequestResponseChannelCache channelCache = createCache(endpoint);
        try {
            final Mono<RequestResponseChannel> channelMono = channelCache.get();
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> channelMono)
                    .thenRequest(1)
                    .thenAwait(VIRTUAL_TIME_SHIFT)
                    .expectNextMatches(ch -> {
                        Assertions.assertTrue(endpoint.isCurrentChannel(ch));
                        Assertions.assertFalse(ch.isDisposed());
                        return true;
                    })
                    .verifyComplete();
                // Two channel should be supplied (initial channel and second channel on retry when initial channel
                // timeout).
                endpoint.assertChannelCreateCount(channelsCount);
            }
        } finally {
            endpoint.close();
            channelCache.dispose();
        }
        endpoint.assertCurrentChannelClosed();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRetryIfChannelClosesWithoutBeingActive() {
        final int channelsCount = 2;
        final Deque<ChannelState> channelStates = new ArrayDeque<>(channelsCount);
        // The state for the first channel that directly completes without being active.
        channelStates.add(ChannelState.complete());
        // The state for the second channel that will be active.
        channelStates.add(ChannelState.as(EndpointState.ACTIVE));

        final MockEndpoint endpoint = createEndpoint(CON_ID, channelStates);
        endpoint.arrange();
        final RequestResponseChannelCache channelCache = createCache(endpoint);
        try {
            final Mono<RequestResponseChannel> channelMono = channelCache.get();
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> channelMono)
                    .thenRequest(1)
                    .thenAwait(VIRTUAL_TIME_SHIFT)
                    .expectNextMatches(ch -> {
                        Assertions.assertTrue(endpoint.isCurrentChannel(ch));
                        Assertions.assertFalse(ch.isDisposed());
                        return true;
                    })
                    .verifyComplete();
                // Two channel should be supplied (initial channel and second channel on retry when initial channel
                // complete without emitting any state).
                endpoint.assertChannelCreateCount(channelsCount);
            }
        } finally {
            endpoint.close();
            channelCache.dispose();
        }
        endpoint.assertCurrentChannelClosed();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRetryIfActivationEmitsRetriableError() {
        final int channelsCount = 2;
        final Deque<ChannelState> channelStates = new ArrayDeque<>(channelsCount);
        // The state for the first channel that emits error when cache wait for it to active.
        channelStates.add(ChannelState.error(new AmqpException(true, "retriable", null)));
        // The state for the second channel that will be active.
        channelStates.add(ChannelState.as(EndpointState.ACTIVE));

        final MockEndpoint endpoint = createEndpoint(CON_ID, channelStates);
        endpoint.arrange();
        final RequestResponseChannelCache channelCache = createCache(endpoint);
        try {
            final Mono<RequestResponseChannel> channelMono = channelCache.get();
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> channelMono)
                    .thenRequest(1)
                    .thenAwait(VIRTUAL_TIME_SHIFT)
                    .expectNextMatches(ch -> {
                        Assertions.assertTrue(endpoint.isCurrentChannel(ch));
                        Assertions.assertFalse(ch.isDisposed());
                        return true;
                    })
                    .verifyComplete();
                // Two channel should be supplied (initial channel and second channel on retry when initial channel
                // emits retriable error).
                endpoint.assertChannelCreateCount(channelsCount);
            }
        } finally {
            endpoint.close();
            channelCache.dispose();
        }
        endpoint.assertCurrentChannelClosed();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRetryIfActivationEmitsRetriableErrorsInARaw() {
        final int channelsCount = 4;
        final Deque<ChannelState> channelStates = new ArrayDeque<>(channelsCount);
        channelStates.add(ChannelState.error(new AmqpException(true, "retriable0", null)));
        channelStates.add(ChannelState.error(new RejectedExecutionException("retriable1")));
        channelStates.add(ChannelState.error(new IllegalStateException("retriable2")));
        channelStates.add(ChannelState.as(EndpointState.ACTIVE));

        final MockEndpoint endpoint = createEndpoint(CON_ID, channelStates);
        endpoint.arrange();
        final RequestResponseChannelCache channelCache = createCache(endpoint);
        try {
            final Mono<RequestResponseChannel> channelMono = channelCache.get();
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> channelMono)
                    .thenRequest(1)
                    .thenAwait(VIRTUAL_TIME_SHIFT)
                    .expectNextMatches(ch -> {
                        Assertions.assertTrue(endpoint.isCurrentChannel(ch));
                        Assertions.assertFalse(ch.isDisposed());
                        return true;
                    })
                    .verifyComplete();
                endpoint.assertChannelCreateCount(channelsCount);
            }
        } finally {
            endpoint.close();
            channelCache.dispose();
        }
        endpoint.assertCurrentChannelClosed();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldPropagateIfActivationEmitsNonRetriableError() {
        final int channelsCount = 1;
        final Deque<ChannelState> channelStates = new ArrayDeque<>(channelsCount);
        final Throwable nonRetriableError = new Throwable("non-retriable");
        channelStates.add(ChannelState.error(nonRetriableError));

        final MockEndpoint endpoint = createEndpoint(CON_ID, channelStates);
        endpoint.arrange();
        final RequestResponseChannelCache channelCache = createCache(endpoint);
        try {
            final Mono<RequestResponseChannel> channelMono = channelCache.get();
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> channelMono)
                    .thenRequest(1)
                    .thenAwait(VIRTUAL_TIME_SHIFT)
                    .verifyErrorMatches(e -> e == nonRetriableError);
                endpoint.assertChannelCreateCount(channelsCount);
            }
        } finally {
            endpoint.close();
            channelCache.dispose();
        }
        endpoint.assertCurrentChannelClosed();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldCloseChannelAwaitingToActiveOnCancel() {
        final int channelsCount = 1;
        final Deque<ChannelState> channelStates = new ArrayDeque<>(channelsCount);
        channelStates.add(ChannelState.never());

        final MockEndpoint endpoint = createEndpoint(CON_ID, channelStates);
        endpoint.arrange();
        final RequestResponseChannelCache channelCache = createCache(endpoint);
        try {
            final Mono<RequestResponseChannel> channelMono = channelCache.get();
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> channelMono)
                    .thenRequest(1)
                    .thenAwait(OPERATION_TIMEOUT.minusSeconds(2)) // OPERATION_TIMEOUT
                    // == 3
                    // seconds
                    .thenCancel()
                    .verify();
                // Assert that the cancel while waiting for channel to active will close the channel.
                endpoint.assertCurrentChannelClosed();
                endpoint.assertChannelCreateCount(channelsCount);
            }
        } finally {
            endpoint.close();
            channelCache.dispose();
        }
    }

    private MockEndpoint createEndpoint(String connectionId, Deque<ChannelState> channelStates) {
        return new MockEndpoint(connectionId, CH_ENTITY_PATH, CH_SESSION_NAME, CH_LINK_NAME, channelStates,
            retryPolicy);
    }

    private RequestResponseChannelCache createCache(MockEndpoint ep) {
        return new RequestResponseChannelCache(ep.connection(), CH_ENTITY_PATH, CH_SESSION_NAME, CH_LINK_NAME,
            retryPolicy);
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
}
