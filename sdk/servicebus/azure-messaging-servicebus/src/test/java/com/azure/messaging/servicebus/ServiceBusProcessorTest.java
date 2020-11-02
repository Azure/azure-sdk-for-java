// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.experimental.util.BinaryData;
import com.azure.messaging.servicebus.implementation.models.ServiceBusProcessorClientOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
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
        Flux<ServiceBusReceivedMessageContext> messageFlux =
            Flux.create(emitter -> {
                for (int i = 0; i < 5; i++) {
                    ServiceBusReceivedMessage serviceBusReceivedMessage =
                        new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
                    serviceBusReceivedMessage.setMessageId(String.valueOf(i));
                    ServiceBusReceivedMessageContext serviceBusReceivedMessageContext =
                        new ServiceBusReceivedMessageContext(serviceBusReceivedMessage);
                    emitter.next(serviceBusReceivedMessageContext);
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
        Flux<ServiceBusReceivedMessageContext> messageFlux =
            Flux.create(emitter -> {
                for (int i = 0; i < numberOfMessages; i++) {
                    ServiceBusReceivedMessage serviceBusReceivedMessage =
                        new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
                    serviceBusReceivedMessage.setMessageId(String.valueOf(i));
                    serviceBusReceivedMessage.setSessionId(String.valueOf(i % 3));
                    ServiceBusReceivedMessageContext serviceBusReceivedMessageContext =
                        new ServiceBusReceivedMessageContext(serviceBusReceivedMessage);
                    emitter.next(serviceBusReceivedMessageContext);
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
        AtomicReference<FluxSink<ServiceBusReceivedMessageContext>> sink = new AtomicReference<>();
        Flux<ServiceBusReceivedMessageContext> messageFlux = Flux.create(sink::set);
        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder = getBuilder(messageFlux);

        AtomicInteger messageId = new AtomicInteger();
        AtomicReference<CountDownLatch> countDownLatch = new AtomicReference<>();
        countDownLatch.set(new CountDownLatch(2));

        AtomicBoolean assertionFailed = new AtomicBoolean();
        ServiceBusProcessorClient serviceBusProcessorClient = new ServiceBusProcessorClient(receiverBuilder,
            messageContext -> {
                try {
                    System.out.println("Received message " + messageContext.getMessage().getMessageId());
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
            ServiceBusReceivedMessageContext serviceBusReceivedMessageContext =
                new ServiceBusReceivedMessageContext(serviceBusReceivedMessage);
            sink.get().next(serviceBusReceivedMessageContext);
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
            ServiceBusReceivedMessageContext serviceBusReceivedMessageContext =
                new ServiceBusReceivedMessageContext(serviceBusReceivedMessage);
            sink.get().next(serviceBusReceivedMessageContext);
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

        List<ServiceBusReceivedMessageContext> messageList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            ServiceBusReceivedMessage serviceBusReceivedMessage =
                new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
            serviceBusReceivedMessage.setMessageId(String.valueOf(i));
            ServiceBusReceivedMessageContext serviceBusReceivedMessageContext =
                new ServiceBusReceivedMessageContext(serviceBusReceivedMessage);
            messageList.add(serviceBusReceivedMessageContext);
        }
        Flux<ServiceBusReceivedMessageContext> messageFlux = Flux.concat(Flux.just(messageList.get(0),
            messageList.get(1)), Flux.error(new IllegalStateException("error")));

        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder = getBuilder(messageFlux);

        AtomicInteger messageId = new AtomicInteger();
        AtomicReference<CountDownLatch> countDownLatch = new AtomicReference<>();
        countDownLatch.set(new CountDownLatch(4));

        AtomicBoolean assertionFailed = new AtomicBoolean();
        ServiceBusProcessorClient serviceBusProcessorClient = new ServiceBusProcessorClient(receiverBuilder,
            messageContext -> {
                try {
                    assertEquals(String.valueOf(messageId.getAndIncrement() % 2),
                        messageContext.getMessage().getMessageId());
                } catch (AssertionError error) {
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
        Assertions.assertTrue(!assertionFailed.get() && success, "Failed to receive all expected messages");
    }

    /**
     * Tests user message processing code throwing an error which should result in the message being abandoned.
     * @throws InterruptedException If the test is interrupted.
     */
    @Test
    public void testUserMessageHandlerError() throws InterruptedException {

        Flux<ServiceBusReceivedMessageContext> messageFlux =
            Flux.create(emitter -> {
                for (int i = 0; i < 5; i++) {
                    ServiceBusReceivedMessage serviceBusReceivedMessage =
                        new ServiceBusReceivedMessage(BinaryData.fromString("hello"));
                    serviceBusReceivedMessage.setMessageId(String.valueOf(i));
                    ServiceBusReceivedMessageContext serviceBusReceivedMessageContext =
                        new ServiceBusReceivedMessageContext(serviceBusReceivedMessage);
                    emitter.next(serviceBusReceivedMessageContext);
                }
            });

        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder =
            mock(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class);

        ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        when(receiverBuilder.buildAsyncClient()).thenReturn(asyncClient);
        when(asyncClient.receiveMessages()).thenReturn(messageFlux);
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        doNothing().when(asyncClient).close();

        AtomicInteger messageId = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(5);
        ServiceBusProcessorClient serviceBusProcessorClient = new ServiceBusProcessorClient(receiverBuilder,
            messageContext -> {
                System.out.println("Message " + messageContext.getMessage().getMessageId());
                assertEquals(String.valueOf(messageId.getAndIncrement()), messageContext.getMessage().getMessageId());
                throw new IllegalStateException(); // throw error from user handler
            },
            error -> countDownLatch.countDown(),
            new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1));

        serviceBusProcessorClient.start();
        boolean success = countDownLatch.await(5, TimeUnit.SECONDS);
        serviceBusProcessorClient.close();
        assertTrue(success, "Failed to receive all expected messages");

        verify(asyncClient, times(5)).abandon(any(ServiceBusReceivedMessage.class));
    }

    private ServiceBusClientBuilder.ServiceBusReceiverClientBuilder getBuilder(
        Flux<ServiceBusReceivedMessageContext> messageFlux) {

        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverBuilder =
            mock(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class);

        ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        when(receiverBuilder.buildAsyncClient()).thenReturn(asyncClient);
        when(asyncClient.receiveMessages()).thenReturn(messageFlux);
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        doNothing().when(asyncClient).close();
        return receiverBuilder;
    }

    private ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder getSessionBuilder(
        Flux<ServiceBusReceivedMessageContext> messageFlux) {

        ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverBuilder =
            mock(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class);

        ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        when(receiverBuilder.buildAsyncClientForProcessor()).thenReturn(asyncClient);
        when(asyncClient.receiveMessages()).thenReturn(messageFlux);
        when(asyncClient.isConnectionClosed()).thenReturn(false);
        doNothing().when(asyncClient).close();
        return receiverBuilder;
    }
}
