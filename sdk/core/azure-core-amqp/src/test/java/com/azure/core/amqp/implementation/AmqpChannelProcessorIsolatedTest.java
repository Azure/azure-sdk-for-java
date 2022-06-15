package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.HashMap;

/**
 * Tests for {@link AmqpChannelProcessor} using
 * {@link reactor.test.scheduler.VirtualTimeScheduler} hence needs
 * to run in isolated and sequential.
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class AmqpChannelProcessorIsolatedTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(30);
    private final TestObject connection1 = new TestObject();

    @Mock
    private AmqpRetryPolicy retryPolicy;
    private AmqpChannelProcessor<TestObject> channelProcessor;
    private AutoCloseable mocksCloseable;

    @BeforeEach
    void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
        channelProcessor = new AmqpChannelProcessor<>("namespace-test", TestObject::getStates, retryPolicy, new HashMap<>());
    }

    @AfterEach
    void teardown() throws Exception {
        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMock(this);
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void doesNotEmitConnectionWhenNotActive() {
        // Arrange
        final TestPublisher<TestObject> publisher = TestPublisher.createCold();

        // Act & Assert
        final VirtualTimeScheduler virtualTimeScheduler = VirtualTimeScheduler.create();
        try {
            StepVerifier.withVirtualTime(() -> publisher.next(connection1).flux()
                    .subscribeWith(channelProcessor), () -> virtualTimeScheduler, 1)
                .expectSubscription()
                .thenAwait(Duration.ofMinutes(10))
                .expectNoEvent(Duration.ofMinutes(10))
                .then(() -> connection1.getSink().next(AmqpEndpointState.UNINITIALIZED))
                .expectNoEvent(Duration.ofMinutes(10))
                .thenCancel()
                .verify(VERIFY_TIMEOUT);
        } finally {
            virtualTimeScheduler.dispose();
        }
    }

    /**
     * Verifies that this AmqpChannelProcessor won't time out even if the 5 minutes default timeout occurs. This is
     * possible when there is a disconnect for a long period of time.
     */
    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void waitsLongPeriodOfTimeForConnection() {
        // Arrange
        final TestPublisher<TestObject> publisher = TestPublisher.createCold();

        // Act & Assert
        final VirtualTimeScheduler virtualTimeScheduler = VirtualTimeScheduler.create();
        try {
            StepVerifier.withVirtualTime(() -> publisher.next(connection1).flux()
                    .subscribeWith(channelProcessor), () -> virtualTimeScheduler, 1)
                .expectSubscription()
                .thenAwait(Duration.ofMinutes(10))
                .then(() -> connection1.getSink().next(AmqpEndpointState.ACTIVE))
                .expectNext(connection1)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
        } finally {
            virtualTimeScheduler.dispose();
        }
    }

    /**
     * Verifies that this AmqpChannelProcessor won't time out even if the 5 minutes default timeout occurs. This is
     * possible when there is a disconnect for a long period of time.
     */
    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void waitsLongPeriodOfTimeForChainedConnections() {
        // Arrange
        final TestPublisher<TestObject> publisher = TestPublisher.createCold();
        final String contents = "Emitted something after 10 minutes.";

        // Act & Assert
        final VirtualTimeScheduler virtualTimeScheduler = VirtualTimeScheduler.create();
        try {
            StepVerifier.withVirtualTime(() -> {
                    return publisher.next(connection1).flux()
                        .subscribeWith(channelProcessor).flatMap(e -> Mono.just(contents));
                }, () -> virtualTimeScheduler, 1)
                .expectSubscription()
                .thenAwait(Duration.ofMinutes(10))
                .then(() -> connection1.getSink().next(AmqpEndpointState.ACTIVE))
                .expectNext(contents)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
        } finally {
            virtualTimeScheduler.dispose();
        }
    }

    static final class TestObject {
        private final TestPublisher<AmqpEndpointState> processor = TestPublisher.createCold();

        public Flux<AmqpEndpointState> getStates() {
            return processor.flux();
        }

        public TestPublisher<AmqpEndpointState> getSink() {
            return processor;
        }
    }
}
