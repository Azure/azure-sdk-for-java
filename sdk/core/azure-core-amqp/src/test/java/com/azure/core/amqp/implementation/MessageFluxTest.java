// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MessageFluxTest {
    @Mock
    private AmqpRetryPolicy retryPolicy;

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

    private static Stream<Arguments> creditFlowModePrefetch() {
        return Stream.of(Arguments.of(CreditFlowMode.EmissionDriven, 1), Arguments.of(CreditFlowMode.RequestDriven, 0));
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    public void shouldTerminateWhenUpstreamCompleteWithoutEmittingReceiver(CreditFlowMode creditFlowMode,
        int prefetch) {
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, retryPolicy);

        StepVerifier.create(messageFlux).then(() -> upstream.complete()).verifyComplete();

        upstream.assertCancelled();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    public void shouldTerminateWhenUpstreamErrorsWithoutEmittingReceiver(CreditFlowMode creditFlowMode, int prefetch) {
        final RuntimeException error = new RuntimeException("error-signal");
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, retryPolicy);

        StepVerifier.create(messageFlux).then(() -> upstream.error(error)).verifyErrorMatches(e -> e == error);

        upstream.assertCancelled();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    public void shouldTerminateWhenDownstreamSignalsCancel(CreditFlowMode creditFlowMode, int prefetch) {
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, retryPolicy);

        StepVerifier.create(messageFlux).thenCancel().verify();

        upstream.assertCancelled();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    public void shouldCloseReceiverWhenUpstreamComplete(CreditFlowMode creditFlowMode, int prefetch) {
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, retryPolicy);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        when(receiver.receive()).thenReturn(Flux.never());
        when(receiver.getEndpointStates()).thenReturn(Flux.never());
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        StepVerifier.create(messageFlux)
            .then(() -> upstream.next(receiver))
            .then(() -> upstream.complete())
            .verifyComplete();

        verify(receiver).closeAsync();
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    public void shouldCloseReceiverWhenUpstreamErrors(CreditFlowMode creditFlowMode, int prefetch) {
        final RuntimeException error = new RuntimeException("error-signal");
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, retryPolicy);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        when(receiver.receive()).thenReturn(Flux.never());
        when(receiver.getEndpointStates()).thenReturn(Flux.never());
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        StepVerifier.create(messageFlux)
            .then(() -> upstream.next(receiver))
            .then(() -> upstream.error(error))
            .verifyError();

        verify(receiver).closeAsync();
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    public void shouldCloseReceiverWhenDownstreamSignalsCancel(CreditFlowMode creditFlowMode, int prefetch) {
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, retryPolicy);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        when(receiver.receive()).thenReturn(Flux.never());
        when(receiver.getEndpointStates()).thenReturn(Flux.never());
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        StepVerifier.create(messageFlux).then(() -> upstream.next(receiver)).thenCancel().verify();

        verify(receiver).closeAsync();
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    public void shouldTerminateWhenReceiverEmitsNonRetriableError(CreditFlowMode creditFlowMode, int prefetch) {
        final AmqpException error = new AmqpException(false, "non-retriable", null);
        final AmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(new AmqpRetryOptions());
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, retryPolicy);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        when(receiver.receive()).thenReturn(Flux.empty());
        when(receiver.getEndpointStates()).thenReturn(Flux.error(error));
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        StepVerifier.create(messageFlux).then(() -> upstream.next(receiver)).verifyErrorMatches(e -> e == error);

        // Expecting closeAsync invocation from two call sites -
        // 1. before the retry to obtain the next receiver.
        // 2. when operator terminates due to non-retriable error.
        verify(receiver, atLeast(2)).closeAsync();
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    public void shouldTerminateWhenRetryDisabledAndFirstReceiverEmitsRetriableError(CreditFlowMode creditFlowMode,
        int prefetch) {
        final AmqpException error = new AmqpException(true, "retriable", null);
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux
            = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, MessageFlux.NULL_RETRY_POLICY);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        when(receiver.receive()).thenReturn(Flux.empty());
        when(receiver.getEndpointStates()).thenReturn(Flux.error(error));
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        StepVerifier.create(messageFlux).then(() -> upstream.next(receiver)).verifyErrorMatches(e -> e == error);

        // Expecting closeAsync invocation from two call sites -
        // 1. before the NOP retry (NULL_RETRY_POLICY) when first receiver terminates with (retriable) error
        // 2. when operator terminates after NOP retry
        verify(receiver, atLeast(2)).closeAsync();
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    public void shouldTerminateWhenRetryDisabledAndFirstReceiverCompletes(CreditFlowMode creditFlowMode, int prefetch) {
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux
            = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, MessageFlux.NULL_RETRY_POLICY);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        when(receiver.receive()).thenReturn(Flux.empty());
        when(receiver.getEndpointStates()).thenReturn(Flux.empty());
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        StepVerifier.create(messageFlux).then(() -> upstream.next(receiver)).verifyComplete();

        // Expecting closeAsync invocation from two call sites -
        // 1. before the NOP retry (NULL_RETRY_POLICY) when first receiver terminates with completion
        // 2. when operator terminates after NOP retry
        verify(receiver, atLeast(2)).closeAsync();
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    public void shouldHonorBackpressureRequest(CreditFlowMode creditFlowMode, int prefetch) {
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, retryPolicy);

        final int[] requests = new int[] { 24, 12, 6, 3, 3 };
        final int totalMessages = Arrays.stream(requests).sum();
        final Message message = mock(Message.class);
        final List<Message> messages
            = IntStream.rangeClosed(1, totalMessages).mapToObj(__ -> message).collect(Collectors.toList());
        final TestPublisher<Message> receiverMessages = TestPublisher.createCold();
        receiverMessages.emit(messages.toArray(new Message[0]));

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        when(receiver.getEndpointStates()).thenReturn(Flux.just(AmqpEndpointState.ACTIVE).concatWith(Flux.never()));
        when(receiver.receive()).thenReturn(receiverMessages.flux());
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        StepVerifier.create(messageFlux, 0)
            .then(() -> upstream.next(receiver))
            .thenRequest(requests[0])
            .expectNextCount(requests[0])
            .thenRequest(requests[1])
            .expectNextCount(requests[1])
            .thenRequest(requests[2])
            .expectNextCount(requests[2])
            .thenRequest(requests[3] + 100) // last request, demanding more than available.
            .expectNextCount(requests[3])
            .then(() -> upstream.complete())
            .thenConsumeWhile(m -> true)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    public void updateDispositionShouldErrorIfReceiverIsNotInitialized(CreditFlowMode creditFlowMode, int prefetch) {
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, retryPolicy);

        StepVerifier.create(messageFlux.updateDisposition("t", Accepted.getInstance()))
            .verifyError(IllegalStateException.class);
    }
}
