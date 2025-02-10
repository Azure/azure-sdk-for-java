// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.engine.Delivery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Tests for {@link ReceiverUnsettledDeliveries}.
 * <p>
 * See <a href=
 * "https://github.com/Azure/azure-sdk-for-java/wiki/Unit-Testing#stepverifierwithvirtualtime">stepverifierwithvirtualtime</a>
 * for why this test class needs to run in Isolated mode.
 * </p>
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class ReceiverUnsettledDeliveriesIsolatedTest {
    private static final UUID DELIVERY_EMPTY_TAG = new UUID(0L, 0L);
    private static final String HOSTNAME = "hostname";
    private static final String ENTITY_PATH = "/orders";
    private static final String RECEIVER_LINK_NAME = "orders-link";
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration VIRTUAL_TIME_SHIFT = OPERATION_TIMEOUT.plusSeconds(30);
    private final ClientLogger logger = new ClientLogger(ReceiverUnsettledDeliveriesTest.class);
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions();
    private AutoCloseable mocksCloseable;
    @Mock
    private ReactorDispatcher reactorDispatcher;
    @Mock
    private Delivery delivery;

    @BeforeEach
    public void setup() throws IOException {
        mocksCloseable = MockitoAnnotations.openMocks(this);
        retryOptions.setTryTimeout(OPERATION_TIMEOUT);
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
    public void sendDispositionTimeoutOnExpiration() throws Exception {
        final UUID deliveryTag = UUID.randomUUID();

        doAnswer(byRunningRunnable()).when(reactorDispatcher).invoke(any(Runnable.class));

        try (ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries()) {
            deliveries.onDelivery(deliveryTag, delivery);
            final Mono<Void> dispositionMono
                = deliveries.sendDisposition(deliveryTag.toString(), Accepted.getInstance());
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> dispositionMono, VIRTUAL_TIME_SHIFT).expectErrorSatisfies(error -> {
                    Assertions.assertTrue(error instanceof AmqpException);
                    final AmqpException amqpError = (AmqpException) error;
                    Assertions.assertEquals(AmqpErrorCondition.TIMEOUT_ERROR, amqpError.getErrorCondition());
                }).verify(VERIFY_TIMEOUT);
            }
        }
    }

    private ReceiverUnsettledDeliveries createUnsettledDeliveries() {
        return new ReceiverUnsettledDeliveries(HOSTNAME, ENTITY_PATH, RECEIVER_LINK_NAME, reactorDispatcher,
            retryOptions, DELIVERY_EMPTY_TAG, logger);
    }

    private static Answer<Void> byRunningRunnable() {
        return invocation -> {
            final Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        };
    }

    private static final class VirtualTimeStepVerifier implements AutoCloseable {
        private final VirtualTimeScheduler scheduler;

        VirtualTimeStepVerifier() {
            scheduler = VirtualTimeScheduler.create();
        }

        <T> StepVerifier.Step<T> create(Supplier<Mono<T>> scenarioSupplier, Duration timeShift) {
            return StepVerifier.withVirtualTime(scenarioSupplier, () -> scheduler, 1).thenAwait(timeShift);
        }

        @Override
        public void close() {
            scheduler.dispose();
        }
    }
}
