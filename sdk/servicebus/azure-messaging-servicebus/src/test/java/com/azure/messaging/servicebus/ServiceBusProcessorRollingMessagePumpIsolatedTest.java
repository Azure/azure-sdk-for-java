// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.ServiceBusProcessor.RollingMessagePump;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.io.IOException;
import java.time.Duration;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
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
public class ServiceBusProcessorRollingMessagePumpIsolatedTest {
    private static final ServiceBusReceiverInstrumentation INSTRUMENTATION = new ServiceBusReceiverInstrumentation(null, null, "FQDN", "entityPath",
        null, ReceiverKind.PROCESSOR);
    @Captor
    private ArgumentCaptor<ServiceBusReceivedMessage> messageCaptor;
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
    public void shouldRetryOnConnectionTermination() {
        final Duration connectionStatePollInterval = Duration.ofSeconds(20);
        final ServiceBusReceiverClientBuilder builder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient firstClient = mock(ServiceBusReceiverAsyncClient.class);


        final AtomicInteger buildClientCalls = new AtomicInteger();
        when(builder.buildAsyncClientForProcessor()).thenAnswer(new Answer<ServiceBusReceiverAsyncClient>() {
            @Override
            public ServiceBusReceiverAsyncClient answer(InvocationOnMock invocation) {
                if (buildClientCalls.incrementAndGet() == 1) {
                    return firstClient;
                } else {
                    throw new UnsupportedOperationException("NoSecondClient");
                }
            }
        });
        when(firstClient.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(firstClient.nonSessionProcessorReceiveV2()).thenReturn(Flux.never());
        when(firstClient.isConnectionClosed()).thenReturn(false, true); // connection transition to terminated on second poll.
        doNothing().when(firstClient).close();

        final RollingMessagePump pump = new RollingMessagePump(builder, m -> { }, e -> { }, 1, false);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.beginIntern())
                .thenAwait(connectionStatePollInterval.multipliedBy(3))
                .verifyErrorSatisfies(e -> {
                    // The assertions confirm that the RollingMessagePump attempted to create the second pump
                    // by requesting a second client, that proves the retry happened upon receiving
                    // MessagePumpTerminatedException indicating the connection termination.
                    // ISE will be thrown by ServiceBusProcessor.RollingMessagePump.retrySpecForNextPump() when retry
                    // gets UnsupportedOperationException.
                    Assertions.assertTrue(e instanceof IllegalStateException);
                    Assertions.assertNotNull(e.getCause());
                    Assertions.assertTrue(e.getCause() instanceof UnsupportedOperationException);
                    Assertions.assertEquals("NoSecondClient", e.getCause().getMessage());
                });
        }

        Assertions.assertEquals(2, buildClientCalls.get());
        verify(firstClient).close();
        verify(firstClient, times(2)).isConnectionClosed();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRetryOnReceiverTerminalError() {
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverClientBuilder builder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient firstClient = mock(ServiceBusReceiverAsyncClient.class);

        final AtomicInteger buildClientCalls = new AtomicInteger();
        when(builder.buildAsyncClientForProcessor()).thenAnswer(new Answer<ServiceBusReceiverAsyncClient>() {
            @Override
            public ServiceBusReceiverAsyncClient answer(InvocationOnMock invocation) {
                if (buildClientCalls.incrementAndGet() == 1) {
                    return firstClient;
                } else {
                    throw new UnsupportedOperationException("NoSecondClient");
                }
            }
        });
        when(firstClient.getInstrumentation()).thenReturn(INSTRUMENTATION);
        final Flux<ServiceBusReceivedMessage> messagesToStream = Flux.concat(Flux.just(message), Flux.error(new RuntimeException("receiver-error")));
        when(firstClient.nonSessionProcessorReceiveV2()).thenReturn(messagesToStream);
        when(firstClient.isConnectionClosed()).thenReturn(false);
        doNothing().when(firstClient).close();

        final Deque<ServiceBusReceivedMessage> consumedMessages = new ConcurrentLinkedDeque<>();
        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            consumedMessages.add(messageContext.getMessage());
        };
        final RollingMessagePump pump = new RollingMessagePump(builder, messageConsumer, e -> { }, 1, false);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.beginIntern())
                .thenAwait(Duration.ofSeconds(30))
                .verifyErrorSatisfies(e -> {
                    // The assertions confirm that the RollingMessagePump attempted to create the second pump
                    // by requesting a second client, that proves the retry happened upon receiving
                    // MessagePumpTerminatedException indicating the receiver terminal error.
                    // ISE will be thrown by ServiceBusProcessor.RollingMessagePump.retrySpecForNextPump() when retry
                    // gets UnsupportedOperationException.
                    Assertions.assertTrue(e instanceof IllegalStateException);
                    Assertions.assertNotNull(e.getCause());
                    Assertions.assertTrue(e.getCause() instanceof UnsupportedOperationException);
                    Assertions.assertEquals("NoSecondClient", e.getCause().getMessage());
                });
        }

        Assertions.assertEquals(1, consumedMessages.size());
        Assertions.assertEquals(message, consumedMessages.pop());
        Assertions.assertEquals(2, buildClientCalls.get());
        verify(firstClient).close();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRetryOnReceiverTerminalCompletion() {
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverClientBuilder builder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient firstClient = mock(ServiceBusReceiverAsyncClient.class);

        final AtomicInteger buildClientCalls = new AtomicInteger();
        when(builder.buildAsyncClientForProcessor()).thenAnswer(new Answer<ServiceBusReceiverAsyncClient>() {
            @Override
            public ServiceBusReceiverAsyncClient answer(InvocationOnMock invocation) {
                if (buildClientCalls.incrementAndGet() == 1) {
                    return firstClient;
                } else {
                    throw new UnsupportedOperationException("NoSecondClient");
                }
            }
        });
        when(firstClient.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(firstClient.nonSessionProcessorReceiveV2()).thenReturn(Flux.just(message)); // <- emit a message and terminate receiver with the completion.
        when(firstClient.isConnectionClosed()).thenReturn(false);
        doNothing().when(firstClient).close();

        final Deque<ServiceBusReceivedMessage> consumedMessages = new ConcurrentLinkedDeque<>();
        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            consumedMessages.add(messageContext.getMessage());
        };
        final RollingMessagePump pump = new RollingMessagePump(builder, messageConsumer, e -> { }, 1, false);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.beginIntern())
                .thenAwait(Duration.ofSeconds(30))
                .verifyErrorSatisfies(e -> {
                    // The assertions confirm that the RollingMessagePump attempted to create the second pump
                    // by requesting a second client, that proves the retry happened upon receiving
                    // MessagePumpTerminatedException indicating the receiver terminal completion.
                    // ISE will be thrown by ServiceBusProcessor.RollingMessagePump.retrySpecForNextPump() when retry
                    // gets UnsupportedOperationException.
                    Assertions.assertTrue(e instanceof IllegalStateException);
                    Assertions.assertNotNull(e.getCause());
                    Assertions.assertTrue(e.getCause() instanceof UnsupportedOperationException);
                    Assertions.assertEquals("NoSecondClient", e.getCause().getMessage());
                });
        }

        Assertions.assertEquals(1, consumedMessages.size());
        Assertions.assertEquals(message, consumedMessages.pop());
        Assertions.assertEquals(2, buildClientCalls.get());
        verify(firstClient).close();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldReportReceiverTerminalError() {
        final ServiceBusReceivedMessage message0 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverClientBuilder builder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient client0 = mock(ServiceBusReceiverAsyncClient.class);
        final ServiceBusReceiverAsyncClient client1 = mock(ServiceBusReceiverAsyncClient.class);

        final AtomicInteger buildClientCalls = new AtomicInteger(0);
        when(builder.buildAsyncClientForProcessor()).thenAnswer(new Answer<ServiceBusReceiverAsyncClient>() {
            @Override
            public ServiceBusReceiverAsyncClient answer(InvocationOnMock invocation) {
                final int callCount = buildClientCalls.incrementAndGet();
                if (callCount == 1) {
                    return client0;
                } else if (callCount == 2) {
                    return client1;
                } else {
                    throw new UnsupportedOperationException("NoThirdClient");
                }
            }
        });

        when(client0.getInstrumentation()).thenReturn(INSTRUMENTATION);
        final RuntimeException client0TerminalError = new RuntimeException("receiver-0-error");
        final Flux<ServiceBusReceivedMessage> client0Messages = Flux.concat(Flux.just(message0), Flux.error(client0TerminalError));
        when(client0.nonSessionProcessorReceiveV2()).thenReturn(client0Messages);
        when(client0.isConnectionClosed()).thenReturn(false);
        doNothing().when(client0).close();

        when(client1.getInstrumentation()).thenReturn(INSTRUMENTATION);
        final ServiceBusException client1TerminalError = new ServiceBusException(new RuntimeException("inner-error-1"), ServiceBusErrorSource.RECEIVE);
        final Flux<ServiceBusReceivedMessage> client1Messages = Flux.concat(Flux.just(message1), Flux.error(client1TerminalError));
        when(client1.nonSessionProcessorReceiveV2()).thenReturn(client1Messages);
        when(client1.isConnectionClosed()).thenReturn(false);
        doNothing().when(client1).close();

        final Deque<ServiceBusReceivedMessage> consumedMessages = new ConcurrentLinkedDeque<>();
        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            consumedMessages.add(messageContext.getMessage());
        };
        final Deque<Throwable> consumedErrors = new ConcurrentLinkedDeque<>();
        final Consumer<ServiceBusErrorContext> errorConsumer = (errorContext) -> {
            consumedErrors.add(errorContext.getException());
        };

        final RollingMessagePump pump = new RollingMessagePump(builder, messageConsumer, errorConsumer, 1, false);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.beginIntern())
                .thenAwait(Duration.ofSeconds(30))
                .verifyErrorSatisfies(e -> {
                    // The assertions confirm that the RollingMessagePump attempted to create the third pump
                    // by requesting a third client, that proves the retry happened upon receiving
                    // MessagePumpTerminatedException indicating the receiver terminal error.
                    // ISE will be thrown by ServiceBusProcessor.RollingMessagePump.retrySpecForNextPump() when retry
                    // gets UnsupportedOperationException.
                    Assertions.assertTrue(e instanceof IllegalStateException);
                    Assertions.assertNotNull(e.getCause());
                    Assertions.assertTrue(e.getCause() instanceof UnsupportedOperationException);
                    Assertions.assertEquals("NoThirdClient", e.getCause().getMessage());
                });
        }

        Assertions.assertEquals(2, consumedMessages.size());
        Assertions.assertEquals(message0, consumedMessages.pop());
        Assertions.assertEquals(message1, consumedMessages.pop());
        Assertions.assertEquals(2, consumedErrors.size());
        // client0 throws a RuntimeException, Processor must wrap it in ServiceBusException.
        final Throwable e0 = consumedErrors.pop();
        Assertions.assertInstanceOf(ServiceBusException.class, e0);
        Assertions.assertNotNull(e0.getCause());
        Assertions.assertEquals(client0TerminalError, e0.getCause());
        // client1 throws a ServiceBusException, Processor should just forward it without wrapping.
        final Throwable e1 = consumedErrors.pop();
        Assertions.assertEquals(client1TerminalError, e1);
        Assertions.assertNotNull(e0.getCause());
        Assertions.assertEquals("inner-error-1", e1.getCause().getMessage());
        Assertions.assertEquals(3, buildClientCalls.get());
        verify(client0).close();
        verify(client1).close();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldCompleteMessageOnSuccessfulProcessing() {
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverClientBuilder builder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);

        when(builder.buildAsyncClientForProcessor()).thenReturn(client);
        when(client.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(client.nonSessionProcessorReceiveV2()).thenReturn(Flux.concat(Flux.just(message), Flux.never()));
        when(client.complete(any())).thenReturn(Mono.empty());
        doNothing().when(client).close();

        final Deque<ServiceBusReceivedMessage> consumedMessages = new ConcurrentLinkedDeque<>();
        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            consumedMessages.add(messageContext.getMessage());
        };
        final RollingMessagePump pump = new RollingMessagePump(builder, messageConsumer, e -> { }, 1, true);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.beginIntern())
                .thenAwait()
                .thenCancel()
                .verify();
        }

        Assertions.assertEquals(1, consumedMessages.size());
        Assertions.assertEquals(message, consumedMessages.pop());
        verify(client).complete(messageCaptor.capture());
        final ServiceBusReceivedMessage completedMessage = messageCaptor.getValue();
        Assertions.assertEquals(message, completedMessage);
        verify(client).close();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldAbandonMessageOnErroredProcessing() {
        final RuntimeException userError = new RuntimeException("UserError");
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverClientBuilder builder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);

        when(builder.buildAsyncClientForProcessor()).thenReturn(client);
        when(client.getInstrumentation()).thenReturn(INSTRUMENTATION);
        when(client.nonSessionProcessorReceiveV2()).thenReturn(Flux.concat(Flux.just(message), Flux.never()));
        when(client.abandon(any())).thenReturn(Mono.empty());
        doNothing().when(client).close();

        final Deque<ServiceBusReceivedMessage> consumedMessages = new ConcurrentLinkedDeque<>();
        final Consumer<ServiceBusReceivedMessageContext> messageConsumer = (messageContext) -> {
            consumedMessages.add(messageContext.getMessage());
            throw userError;
        };

        final Deque<Throwable> consumedErrors = new ConcurrentLinkedDeque<>();
        final Consumer<ServiceBusErrorContext> errorConsumer = (errorContext) -> {
            consumedErrors.add(errorContext.getException());
        };

        final RollingMessagePump pump = new RollingMessagePump(builder, messageConsumer, errorConsumer, 1, true);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> pump.beginIntern())
                .thenAwait()
                .thenCancel()
                .verify();
        }

        Assertions.assertEquals(1, consumedMessages.size());
        Assertions.assertEquals(message, consumedMessages.pop());
        Assertions.assertEquals(1, consumedErrors.size());
        final Throwable e = consumedErrors.pop();
        Assertions.assertTrue(e instanceof ServiceBusException);
        Assertions.assertEquals(userError, e.getCause());

        verify(client).abandon(messageCaptor.capture());
        final ServiceBusReceivedMessage abandonedMessage = messageCaptor.getValue();
        Assertions.assertEquals(message, abandonedMessage);
        verify(client).close();
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
