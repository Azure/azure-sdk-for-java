// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.publisher.TestPublisher;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A type that facade ReactorReceiver operations to improve the test readability.
 */
final class ReactorReceiverFacade {
    private int[] emittedMessages = new int[1];
    private final TestPublisher<ReactorReceiver> upstream;
    private final ReactorReceiver receiver;
    private final TestPublisher<Message> messagesPublisher;
    private final Flux<Message> messages;
    private final Sinks.Many<AmqpEndpointState> endpointStatesSink;
    private final Flux<AmqpEndpointState> endpointStates;
    private final AtomicLong requested = new AtomicLong();

    ReactorReceiverFacade(TestPublisher<ReactorReceiver> upstream, ReactorReceiver receiver) {
        this(upstream, receiver, TestPublisher.<Message>create());
    }

    ReactorReceiverFacade(TestPublisher<ReactorReceiver> upstream, ReactorReceiver receiver,
        TestPublisher<Message> messagesPublisher) {
        this.upstream = upstream;
        this.receiver = receiver;
        this.messagesPublisher = messagesPublisher;
        this.messages
            = messagesPublisher.flux().doOnRequest(r -> requested.addAndGet(r)).doOnNext(__ -> emittedMessages[0]++);
        this.endpointStatesSink = Sinks.many().replay().latestOrDefault(AmqpEndpointState.UNINITIALIZED);
        this.endpointStates = this.endpointStatesSink.asFlux().cache(1);
    }

    Flux<AmqpEndpointState> getEndpointStates() {
        return endpointStates;
    }

    Flux<Message> getMessages() {
        return this.messages;
    }

    boolean wasSubscribedToMessages() {
        return messagesPublisher.wasSubscribed();
    }

    /**
     * @return the total number of subscriptions made to the message publisher.
     */
    long getSubscriptionCountToMessages() {
        return messagesPublisher.subscribeCount();
    }

    /**
     * asserts that all subscription made to the message publisher unsubscribed (i.e. no subscription leaking).
     */
    void assertNoPendingSubscriptionsToMessages() {
        messagesPublisher.assertNoSubscribers();
    }

    long getRequestedMessages() {
        return requested.get();
    }

    int getMessageEmitCount() {
        return emittedMessages[0];
    }

    Runnable emit() {
        return () -> {
            upstream.next(receiver);
        };
    }

    Runnable emitAndCompleteEndpointStates(AmqpEndpointState state) {
        return () -> {
            endpointStatesSink.emitNext(state, Sinks.EmitFailureHandler.FAIL_FAST);
            endpointStatesSink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        };
    }

    Runnable emitAndErrorEndpointStates(AmqpEndpointState state, Throwable error) {
        return () -> {
            endpointStatesSink.emitNext(state, Sinks.EmitFailureHandler.FAIL_FAST);
            endpointStatesSink.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST);
        };
    }

    Runnable emitEndpointStates(AmqpEndpointState state) {
        return () -> {
            endpointStatesSink.emitNext(state, Sinks.EmitFailureHandler.FAIL_FAST);
        };
    }

    Runnable completeEndpointStates() {
        return () -> endpointStatesSink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
    }

    Runnable errorEndpointStates(Throwable error) {
        return () -> endpointStatesSink.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    Runnable emitMessage(Message message) {
        return () -> messagesPublisher.next(message);
    }

    Runnable emitAndCompleteMessages(List<Message> messageList) {
        return () -> messagesPublisher.emit(messageList.toArray(new Message[0]));
    }

    Runnable completeMessages() {
        return () -> messagesPublisher.complete();
    }

    Runnable emitMessages(List<Message> messageList) {
        Assertions.assertTrue(messageList.size() > 0);
        return () -> {
            final int size = messageList.size();
            if (size == 1) {
                messagesPublisher.next(messageList.get(0));
            } else {
                messagesPublisher.next(messageList.get(0), messageList.subList(1, size - 1).toArray(new Message[0]));
            }
        };
    }
}
