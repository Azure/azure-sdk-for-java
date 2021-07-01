// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.implementation.models.ServiceBusProcessorClientOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.MESSAGE_ENQUEUED_TIME;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServiceBusProcessorClient}.
 */
public class ServiceBusProcessorTest {

    /**
     * Tests receiving messages using a {@link ServiceBusProcessorClient}.
     *
     * @throws InterruptedException If the test is interrupted.
     */
    @Test
    public void testReceivingMessagesWithProcessor() throws InterruptedException {
        Flux<ServiceBusMessageContext> messageFlux =
            Flux.create(emitter -> {
                for (int i = 0; i < 5; i++) {
                    ServiceBusReceivedMessage serviceBusReceivedMessage =
                        new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
                    serviceBusReceivedMessage.setMessageId(String.valueOf(i));
                    ServiceBusMessageContext serviceBusMessageContext =
                        new ServiceBusMessageContext(serviceBusReceivedMessage);
                    emitter.next(serviceBusMessageContext);
                }
            });

        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder = getBuilder(messageFlux);

        AtomicInteger messageId = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(5);
        ServiceBusProcessorClient serviceBusProcessorClient = new ServiceBusProcessorClient(receiverBuilder,
            messageContext -> {
                assertEquals(String.valueOf(messageId.getAndIncrement()), messageContext.getMessage().getMessageId());
                countDownLatch.countDown();
            },
            error -> Assertions.fail("Error occurred when receiving messages from the processor"),
            new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1));

        serviceBusProcessorClient.start();
        boolean success = countDownLatch.await(5, TimeUnit.SECONDS);
        serviceBusProcessorClient.close();
        assertTrue(success, "Failed to receive all expected messages");
    }

    /**
     * Tests receiving messages using a session-enabled {@link ServiceBusProcessorClient}.
     *
     * @throws InterruptedException If the test is interrupted.
     */
    @Test
    public void testReceivingMultiSessionMessagesWithProcessor() throws InterruptedException {
        int numberOfMessages = 10;
        Flux<ServiceBusMessageContext> messageFlux =
            Flux.create(emitter -> {
                for (int i = 0; i < numberOfMessages; i++) {
                    ServiceBusReceivedMessage serviceBusReceivedMessage =
                        new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
                    serviceBusReceivedMessage.setMessageId(String.valueOf(i));
                    serviceBusReceivedMessage.setSessionId(String.valueOf(i % 3));
                    ServiceBusMessageContext serviceBusMessageContext =
                        new ServiceBusMessageContext(serviceBusReceivedMessage);
                    emitter.next(serviceBusMessageContext);
                }
            });

        ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverBuilder = getSessionBuilder(messageFlux);

        AtomicInteger messageId = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(numberOfMessages);
        ServiceBusProcessorClient serviceBusProcessorClient = new ServiceBusProcessorClient(receiverBuilder,
            messageContext -> {
                int expectedMessageId = messageId.getAndIncrement();
                assertEquals(String.valueOf(expectedMessageId), messageContext.getMessage().getMessageId());
                assertEquals(String.valueOf(expectedMessageId % 3), messageContext.getMessage().getSessionId());
                countDownLatch.countDown();
            },
            error -> Assertions.fail("Error occurred when receiving messages from the processor"),
            new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1));

        serviceBusProcessorClient.start();
        boolean success = countDownLatch.await(5, TimeUnit.SECONDS);
        serviceBusProcessorClient.close();
        assertTrue(success, "Failed to receive all expected messages");
    }

    /**
     * Tests receiving messages using a {@link ServiceBusProcessorClient}, pausing the processor and then resuming
     * the processor to continue receiving messages.
     *
     * @throws InterruptedException If the test is interrupted.
     */
    @Test
    public void testStartStopResume() throws InterruptedException {
        AtomicReference<FluxSink<ServiceBusMessageContext>> sink = new AtomicReference<>();
        Flux<ServiceBusMessageContext> messageFlux = Flux.create(sink::set);
        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder = getBuilder(messageFlux);

        AtomicInteger messageId = new AtomicInteger();
        AtomicReference<CountDownLatch> countDownLatch = new AtomicReference<>();
        countDownLatch.set(new CountDownLatch(2));

        AtomicBoolean assertionFailed = new AtomicBoolean();
        ServiceBusProcessorClient serviceBusProcessorClient = new ServiceBusProcessorClient(receiverBuilder,
            messageContext -> {
                try {
                    assertEquals(String.valueOf(messageId.getAndIncrement()),
                        messageContext.getMessage().getMessageId());
                } catch (AssertionError error) {
                    assertionFailed.set(true);
                } finally {
                    countDownLatch.get().countDown();
                }
            },
            error -> Assertions.fail("Error occurred when receiving messages from the processor"),
            new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1));

        serviceBusProcessorClient.start();
        for (int i = 0; i < 2; i++) {
            ServiceBusReceivedMessage serviceBusReceivedMessage =
                new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
            serviceBusReceivedMessage.setMessageId(String.valueOf(i));
            ServiceBusMessageContext serviceBusMessageContext =
                new ServiceBusMessageContext(serviceBusReceivedMessage);
            sink.get().next(serviceBusMessageContext);
        }
        boolean success = countDownLatch.get().await(5, TimeUnit.SECONDS);
        serviceBusProcessorClient.stop();
        assertTrue(!assertionFailed.get() && success, "Failed to receive all expected messages");

        countDownLatch.set(new CountDownLatch(8));
        serviceBusProcessorClient.start();
        for (int i = 2; i < 10; i++) {
            ServiceBusReceivedMessage serviceBusReceivedMessage =
                new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
            serviceBusReceivedMessage.setMessageId(String.valueOf(i));
            ServiceBusMessageContext serviceBusMessageContext =
                new ServiceBusMessageContext(serviceBusReceivedMessage);
            sink.get().next(serviceBusMessageContext);
        }
        success = countDownLatch.get().await(5, TimeUnit.SECONDS);
        serviceBusProcessorClient.close();
        assertTrue(!assertionFailed.get() && success, "Failed to receive all expected messages");
    }


    /**
     * Tests receiving messages using a {@link ServiceBusProcessorClient}, handles errors while receiving messages
     * and then recovers from the error and continues receiving messages.
     *
     * @throws InterruptedException If the test is interrupted.
     */
    @Test
    public void testErrorRecovery() throws InterruptedException {
        List<ServiceBusMessageContext> messageList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            ServiceBusReceivedMessage serviceBusReceivedMessage =
                new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
            serviceBusReceivedMessage.setMessageId(String.valueOf(i));
            ServiceBusMessageContext serviceBusMessageContext =
                new ServiceBusMessageContext(serviceBusReceivedMessage);
            messageList.add(serviceBusMessageContext);
        }

        final Flux<ServiceBusMessageContext> messageFlux = Flux.generate(() -> 0,
            (state, sink) -> {
                ServiceBusReceivedMessage serviceBusReceivedMessage =
                    new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
                serviceBusReceivedMessage.setMessageId(String.valueOf(state));
                ServiceBusMessageContext serviceBusMessageContext =
                    new ServiceBusMessageContext(serviceBusReceivedMessage);
                if (state == 2) {
                    throw new IllegalStateException("error");
                } else {
                    sink.next(serviceBusMessageContext);
                }
                return state + 1;
            });

        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder = getBuilder(messageFlux);
        AtomicInteger messageId = new AtomicInteger();
        AtomicReference<CountDownLatch> countDownLatch = new AtomicReference<>();
        countDownLatch.set(new CountDownLatch(4));
        AtomicBoolean assertionFailed = new AtomicBoolean();
        StringBuffer messageIdNotMatched = new StringBuffer();
        ServiceBusProcessorClient serviceBusProcessorClient = new ServiceBusProcessorClient(receiverBuilder,
            messageContext -> {
                try {
                    assertEquals(String.valueOf(messageId.getAndIncrement() % 2),
                        messageContext.getMessage().getMessageId());
                } catch (AssertionError error) {
                    messageIdNotMatched.append(messageContext.getMessage().getMessageId()).append(",");
                    assertionFailed.set(true);
                } finally {
                    countDownLatch.get().countDown();
                }
            },
            error -> { /* ignored */ },
            new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1));

        serviceBusProcessorClient.start();
        boolean success = countDownLatch.get().await(20, TimeUnit.SECONDS);
        serviceBusProcessorClient.close();
        Assertions.assertTrue(!assertionFailed.get(), "Message id did not match. Invalid message Ids: " + messageIdNotMatched);
        Assertions.assertTrue(success, "Failed to receive all expected messages");
    }

    /**
     * Tests user message processing code throwing an error which should result in the message being abandoned.
     * @throws InterruptedException If the test is interrupted.
     */
    @Test
    public void testUserMessageHandlerError() throws InterruptedException {
        final int numberOfEvents = 5;
        final Flux<ServiceBusMessageContext> messageFlux = Flux.generate(() -> 0,
            (state, sink) -> {
                ServiceBusReceivedMessage serviceBusReceivedMessage =
                    new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
                serviceBusReceivedMessage.setMessageId(String.valueOf(state));
                ServiceBusMessageContext serviceBusMessageContext =
                    new ServiceBusMessageContext(serviceBusReceivedMessage);
                sink.next(serviceBusMessageContext);
                if (state == numberOfEvents) {
                    sink.complete();
                }
                return state + 1;
            });

        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder =
            mock(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);

        when(receiverBuilder.buildAsyncClient()).thenReturn(asyncClient);
        when(asyncClient.receiveMessagesWithContext()).thenReturn(messageFlux);
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        when(asyncClient.abandon(any(ServiceBusReceivedMessage.class))).thenReturn(Mono.empty());
        doNothing().when(asyncClient).close();

        final AtomicInteger messageId = new AtomicInteger();
        final CountDownLatch countDownLatch = new CountDownLatch(numberOfEvents);
        ServiceBusProcessorClient serviceBusProcessorClient = new ServiceBusProcessorClient(receiverBuilder,
            messageContext -> {
                assertEquals(String.valueOf(messageId.getAndIncrement()), messageContext.getMessage().getMessageId());
                throw new IllegalStateException(); // throw error from user handler
            },
            serviceBusProcessErrorContext -> {
                ServiceBusException exception = (ServiceBusException) serviceBusProcessErrorContext.getException();
                assertSame(exception.getErrorSource(), ServiceBusErrorSource.USER_CALLBACK);
                countDownLatch.countDown();
            },
            new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1));

        serviceBusProcessorClient.start();
        boolean success = countDownLatch.await(5, TimeUnit.SECONDS);
        serviceBusProcessorClient.close();
        assertTrue(success, "Failed to receive all expected messages");

        // It's possible that the last event has not been abandoned yet because this is in the background. Which is
        // annoying but is a timing issue.
        verify(asyncClient, atLeast(numberOfEvents - 1))
            .abandon(any(ServiceBusReceivedMessage.class));
    }

    @Test
    public void testUserMessageHandlerErrorWithAutoCompleteDisabled() throws InterruptedException {

        final Flux<ServiceBusMessageContext> messageFlux = Flux.generate(() -> 0,
            (state, sink) -> {
                ServiceBusReceivedMessage serviceBusReceivedMessage =
                    new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
                serviceBusReceivedMessage.setMessageId(String.valueOf(state));
                ServiceBusMessageContext serviceBusMessageContext =
                    new ServiceBusMessageContext(serviceBusReceivedMessage);
                sink.next(serviceBusMessageContext);
                if (state == 5) {
                    sink.complete();
                }
                return state + 1;
            }).publish().autoConnect().cast(ServiceBusMessageContext.class);

        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder =
            mock(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class);

        ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        when(receiverBuilder.buildAsyncClient()).thenReturn(asyncClient);
        when(asyncClient.receiveMessagesWithContext()).thenReturn(messageFlux);
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        doNothing().when(asyncClient).close();

        AtomicInteger messageId = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(5);
        ServiceBusProcessorClient serviceBusProcessorClient = new ServiceBusProcessorClient(receiverBuilder,
            messageContext -> {
                assertEquals(String.valueOf(messageId.getAndIncrement()), messageContext.getMessage().getMessageId());
                throw new IllegalStateException(); // throw error from user handler
            },
            serviceBusProcessErrorContext -> {
                ServiceBusException exception = (ServiceBusException) serviceBusProcessErrorContext.getException();
                assertEquals(ServiceBusErrorSource.USER_CALLBACK, exception.getErrorSource());
                countDownLatch.countDown();
            },
            new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1).setDisableAutoComplete(true));

        serviceBusProcessorClient.start();
        boolean success = countDownLatch.await(30, TimeUnit.SECONDS);
        serviceBusProcessorClient.close();
        assertTrue(success, "Failed to receive all expected messages");

        verify(asyncClient, never()).abandon(any(ServiceBusReceivedMessage.class));
    }

    @Test
    public void testProcessorWithTracingEnabled() throws InterruptedException {
        final Tracer tracer = mock(Tracer.class);
        final List<Tracer> tracers = Collections.singletonList(tracer);
        final int numberOfTimes = 5;
        final TracerProvider tracerProvider = new TracerProvider(tracers);

        String diagnosticId = "00-08ee063508037b1719dddcbf248e30e2-1365c684eb25daed-01";

        when(tracer.extractContext(eq(diagnosticId), any())).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(SPAN_CONTEXT_KEY, "value");
            }
        );
        when(tracer.start(eq("ServiceBus.process"), any(), eq(ProcessKind.PROCESS))).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                assertTrue(passed.getData(MESSAGE_ENQUEUED_TIME).isPresent());
                return passed.addData(SPAN_CONTEXT_KEY, "value1").addData("scope", (AutoCloseable) () -> {
                    return;
                }).addData(PARENT_SPAN_KEY, "value2");
            }
        );
        Flux<ServiceBusMessageContext> messageFlux =
            Flux.create(emitter -> {
                for (int i = 0; i < numberOfTimes; i++) {
                    ServiceBusReceivedMessage serviceBusReceivedMessage =
                        new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
                    serviceBusReceivedMessage.setMessageId(String.valueOf(i));
                    serviceBusReceivedMessage.setEnqueuedTime(OffsetDateTime.now());
                    serviceBusReceivedMessage.getApplicationProperties().put(DIAGNOSTIC_ID_KEY, diagnosticId);
                    ServiceBusMessageContext serviceBusMessageContext =
                        new ServiceBusMessageContext(serviceBusReceivedMessage);
                    emitter.next(serviceBusMessageContext);
                }
            });

        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder = getBuilder(messageFlux);

        AtomicInteger messageId = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(numberOfTimes);
        ServiceBusProcessorClient serviceBusProcessorClient = new ServiceBusProcessorClient(receiverBuilder,
            messageContext -> {
                assertEquals(String.valueOf(messageId.getAndIncrement()), messageContext.getMessage().getMessageId());
                countDownLatch.countDown();
            },
            error -> Assertions.fail("Error occurred when receiving messages from the processor"),
            new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1).setTracerProvider(tracerProvider));

        serviceBusProcessorClient.start();
        boolean success = countDownLatch.await(numberOfTimes, TimeUnit.SECONDS);
        serviceBusProcessorClient.close();

        assertTrue(success, "Failed to receive all expected messages");
        verify(tracer, times(numberOfTimes)).extractContext(eq(diagnosticId), any());
        verify(tracer, times(numberOfTimes)).start(eq("ServiceBus.process"), any(), eq(ProcessKind.PROCESS));

        // This is one less because the processEvent is called before the end span call, so it is possible for
        // to reach this line without calling it the 5th time yet. (Timing issue.)
        verify(tracer, atLeast(numberOfTimes - 1)).end(eq("success"), isNull(), any());

    }

    private ServiceBusClientBuilder.ServiceBusReceiverClientBuilder getBuilder(
        Flux<ServiceBusMessageContext> messageFlux) {

        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder =
            mock(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class);

        ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        when(receiverBuilder.buildAsyncClient()).thenReturn(asyncClient);
        when(asyncClient.receiveMessagesWithContext()).thenReturn(messageFlux);
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        doNothing().when(asyncClient).close();
        return receiverBuilder;
    }

    private ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder getSessionBuilder(
        Flux<ServiceBusMessageContext> messageFlux) {

        ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverBuilder =
            mock(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class);

        ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        when(receiverBuilder.buildAsyncClientForProcessor()).thenReturn(asyncClient);
        when(asyncClient.receiveMessagesWithContext()).thenReturn(messageFlux);
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        doNothing().when(asyncClient).close();
        return receiverBuilder;
    }
}
