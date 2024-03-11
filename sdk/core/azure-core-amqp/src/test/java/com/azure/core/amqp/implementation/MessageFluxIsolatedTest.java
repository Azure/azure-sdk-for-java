// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.handler.DeliveryNotOnLinkException;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.AtLeast;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MessageFlux}.
 * <p>
 * See <a href=
 * "https://github.com/Azure/azure-sdk-for-java/wiki/Unit-Testing#stepverifierwithvirtualtime">stepverifierwithvirtualtime</a>
 * for why this test class needs to run in Isolated mode.
 * </p>
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class MessageFluxIsolatedTest {
    private static final int MAX_RETRY = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(3);
    private static final Duration UPSTREAM_DELAY_BEFORE_NEXT = RETRY_DELAY.plusSeconds(1);
    private static final AmqpRetryOptions RETRY_OPTIONS
        = new AmqpRetryOptions().setMaxRetries(MAX_RETRY).setDelay(RETRY_DELAY);
    private static final AmqpRetryPolicy RETRY_POLICY = new FixedAmqpRetryPolicy(RETRY_OPTIONS);
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
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldGetNextReceiverWhenCurrentTerminateWithRetriableError(CreditFlowMode creditFlowMode,
        int prefetch) {
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, RETRY_POLICY);

        final ReactorReceiver firstReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade firstReceiverFacade = new ReactorReceiverFacade(upstream, firstReceiver);
        when(firstReceiver.getEndpointStates()).thenReturn(firstReceiverFacade.getEndpointStates());
        when(firstReceiver.receive()).thenReturn(firstReceiverFacade.getMessages());
        when(firstReceiver.closeAsync()).thenReturn(Mono.empty());

        final ReactorReceiver secondReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade secondReceiverFacade = new ReactorReceiverFacade(upstream, secondReceiver);
        when(secondReceiver.getEndpointStates()).thenReturn(secondReceiverFacade.getEndpointStates());
        when(secondReceiver.receive()).thenReturn(secondReceiverFacade.getMessages());
        when(secondReceiver.closeAsync()).thenReturn(Mono.empty());

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(firstReceiverFacade.emit())
                .then(firstReceiverFacade.errorEndpointStates(new AmqpException(true, "retriable", null)))
                .thenAwait(UPSTREAM_DELAY_BEFORE_NEXT)
                .then(secondReceiverFacade.emit())
                .then(() -> firstReceiverFacade.assertNoPendingSubscriptionsToMessages()) // Subscription made to the
                // 1st receiver should not be
                // leaked when switching to
                // 2nd.
                .then(() -> upstream.complete())
                .verifyComplete();
        }

        Assertions.assertTrue(firstReceiverFacade.wasSubscribedToMessages());
        Assertions.assertTrue(secondReceiverFacade.wasSubscribedToMessages());
        secondReceiverFacade.assertNoPendingSubscriptionsToMessages();
        verify(firstReceiver).closeAsync();
        verify(secondReceiver).closeAsync();
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldGetNextReceiverWhenCurrentTerminateWithCompletion(CreditFlowMode creditFlowMode, int prefetch) {
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, RETRY_POLICY);

        final ReactorReceiver firstReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade firstReceiverFacade = new ReactorReceiverFacade(upstream, firstReceiver);
        when(firstReceiver.getEndpointStates()).thenReturn(firstReceiverFacade.getEndpointStates());
        when(firstReceiver.receive()).thenReturn(firstReceiverFacade.getMessages());
        when(firstReceiver.closeAsync()).thenReturn(Mono.empty());

        final ReactorReceiver secondReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade secondReceiverFacade = new ReactorReceiverFacade(upstream, secondReceiver);
        when(secondReceiver.getEndpointStates()).thenReturn(secondReceiverFacade.getEndpointStates());
        when(secondReceiver.receive()).thenReturn(secondReceiverFacade.getMessages());
        when(secondReceiver.closeAsync()).thenReturn(Mono.empty());

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(firstReceiverFacade.emit())
                .then(firstReceiverFacade.completeEndpointStates())
                .thenAwait(UPSTREAM_DELAY_BEFORE_NEXT)
                .then(secondReceiverFacade.emit())
                .then(() -> firstReceiverFacade.assertNoPendingSubscriptionsToMessages())
                .then(() -> upstream.complete())
                .verifyComplete();
        }

        Assertions.assertTrue(firstReceiverFacade.wasSubscribedToMessages());
        Assertions.assertTrue(secondReceiverFacade.wasSubscribedToMessages());
        secondReceiverFacade.assertNoPendingSubscriptionsToMessages();
        verify(firstReceiver).closeAsync();
        verify(secondReceiver).closeAsync();
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldNotGetNextReceiverWhenCurrentTerminateWithNonRetriableError(CreditFlowMode creditFlowMode,
        int prefetch) {
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, RETRY_POLICY);

        final ReactorReceiver firstReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade firstReceiverFacade = new ReactorReceiverFacade(upstream, firstReceiver);
        when(firstReceiver.getEndpointStates()).thenReturn(firstReceiverFacade.getEndpointStates());
        when(firstReceiver.receive()).thenReturn(firstReceiverFacade.getMessages());
        when(firstReceiver.closeAsync()).thenReturn(Mono.empty());

        final ReactorReceiver secondReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade secondReceiverFacade = new ReactorReceiverFacade(upstream, secondReceiver);
        when(secondReceiver.getEndpointStates()).thenReturn(secondReceiverFacade.getEndpointStates());
        when(secondReceiver.receive()).thenReturn(secondReceiverFacade.getMessages());
        when(secondReceiver.closeAsync()).thenReturn(Mono.empty());

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(firstReceiverFacade.emit())
                .then(firstReceiverFacade.errorEndpointStates(new AmqpException(false, "non-retriable", null)))
                .thenAwait(UPSTREAM_DELAY_BEFORE_NEXT)
                .then(() -> firstReceiverFacade.assertNoPendingSubscriptionsToMessages())
                .then(secondReceiverFacade.emit())
                .then(() -> upstream.complete())
                .verifyError();
        }

        Assertions.assertTrue(firstReceiverFacade.wasSubscribedToMessages());
        Assertions.assertFalse(secondReceiverFacade.wasSubscribedToMessages());
        // Expecting closeAsync invocation from two call sites -
        // 1. after identified that receiver is terminated.
        // 2. when operator terminates due to non-retriable error.
        verify(firstReceiver, new AtLeast(2)).closeAsync();
        verify(secondReceiver, never()).closeAsync();
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldTerminateWhenRetriesOfReceiversErrorExhausts(CreditFlowMode creditFlowMode, int prefetch) {
        final AmqpException error = new AmqpException(true, "retriable", null);
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, RETRY_POLICY);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade receiverFacade
            = new ReactorReceiverFacade(upstream, receiver, TestPublisher.<Message>createCold());
        when(receiver.getEndpointStates()).thenReturn(receiverFacade.getEndpointStates());
        when(receiver.receive()).thenReturn(receiverFacade.getMessages());
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(receiverFacade.emit())
                .then(receiverFacade.errorEndpointStates(error))
                .then(receiverFacade.completeMessages())
                .thenAwait(UPSTREAM_DELAY_BEFORE_NEXT)
                .then(receiverFacade.emit())                // response for retry1
                .then(receiverFacade.errorEndpointStates(error))
                .then(receiverFacade.completeMessages())
                .thenAwait(UPSTREAM_DELAY_BEFORE_NEXT)
                .then(receiverFacade.emit())                // response for retry2
                .then(receiverFacade.errorEndpointStates(error))
                .then(receiverFacade.completeMessages())
                .thenAwait(UPSTREAM_DELAY_BEFORE_NEXT)
                .then(receiverFacade.emit())                // response for retry3
                .then(receiverFacade.errorEndpointStates(error))
                .then(receiverFacade.completeMessages())
                .verifyErrorMatches(e -> e == error);
        }

        Assertions.assertEquals(MAX_RETRY + 1, receiverFacade.getSubscriptionCountToMessages());
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldHappenUpdateDispositionOnCurrentReceiver(CreditFlowMode creditFlowMode, int prefetch) {
        final Duration retryDelay = Duration.ofSeconds(1);
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, RETRY_POLICY);

        final ReactorReceiver firstReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade firstReceiverFacade = new ReactorReceiverFacade(upstream, firstReceiver);
        when(firstReceiver.getEndpointStates()).thenReturn(firstReceiverFacade.getEndpointStates());
        when(firstReceiver.receive()).thenReturn(firstReceiverFacade.getMessages());
        final List<String> firstReceiverDispositionTags = new ArrayList<>();
        doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            @SuppressWarnings("unchecked")
            final String deliveryTag = (String) args[0];
            firstReceiverDispositionTags.add(deliveryTag);
            return Mono.empty();
        }).when(firstReceiver).updateDisposition(any(), any());
        when(firstReceiver.closeAsync()).thenReturn(Mono.empty());

        final ReactorReceiver secondReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade secondReceiverFacade = new ReactorReceiverFacade(upstream, secondReceiver);
        when(secondReceiver.getEndpointStates()).thenReturn(secondReceiverFacade.getEndpointStates());
        when(secondReceiver.receive()).thenReturn(secondReceiverFacade.getMessages());
        final List<String> secondReceiverDispositionTags = new ArrayList<>();
        doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            @SuppressWarnings("unchecked")
            final String deliveryTag = (String) args[0];
            secondReceiverDispositionTags.add(deliveryTag);
            return Mono.empty();
        }).when(secondReceiver).updateDisposition(any(), any());
        when(secondReceiver.closeAsync()).thenReturn(Mono.empty());

        final Message message = mock(Message.class);
        final List<Message> firstReceiverMessages = generateMessages(message, 4);
        final List<Message> secondReceiverMessages = generateMessages(message, 6);
        final int firstReceiverMessagesCount = firstReceiverMessages.size();
        final int secondReceiverMessagesCount = secondReceiverMessages.size();
        final List<String> dispositionTags = new ArrayList<>();
        for (int i = 0; i < firstReceiverMessagesCount; i++) {
            dispositionTags.add(UUID.randomUUID().toString());
        }
        for (int i = 0; i < secondReceiverMessagesCount; i++) {
            dispositionTags.add(UUID.randomUUID().toString());
        }
        final int[] idx = new int[1];
        final int request = firstReceiverMessagesCount + secondReceiverMessagesCount + 5;

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux.concatMap(
                m -> messageFlux.updateDisposition(dispositionTags.get(idx[0]++), Accepted.getInstance()).thenReturn(m),
                1))
                .then(firstReceiverFacade.emit())
                .thenRequest(request)
                .then(firstReceiverFacade.emitEndpointStates(AmqpEndpointState.ACTIVE))
                .then(firstReceiverFacade.emitAndCompleteMessages(firstReceiverMessages))
                .then(firstReceiverFacade.completeEndpointStates())
                .thenAwait(retryDelay.plusSeconds(1))
                .then(secondReceiverFacade.emit())
                .then(secondReceiverFacade.emitEndpointStates(AmqpEndpointState.ACTIVE))
                .then(secondReceiverFacade.emitAndCompleteMessages(secondReceiverMessages))
                .then(secondReceiverFacade.completeEndpointStates())
                .then(() -> upstream.complete())
                .expectNextCount(firstReceiverMessagesCount + secondReceiverMessagesCount)
                .thenConsumeWhile(m -> true)
                .verifyComplete();
        }

        int i = 0;
        for (; i < firstReceiverMessagesCount; i++) {
            Assertions.assertEquals(dispositionTags.get(i), firstReceiverDispositionTags.get(i));
        }
        int j = 0;
        for (; i < firstReceiverMessagesCount + secondReceiverMessagesCount; i++, j++) {
            Assertions.assertEquals(dispositionTags.get(i), secondReceiverDispositionTags.get(j));
        }
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    @Execution(ExecutionMode.SAME_THREAD)
    public void updateDispositionShouldErrorIfReceiverIsGone(CreditFlowMode creditFlowMode, int prefetch) {
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, RETRY_POLICY);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade receiverFacade = new ReactorReceiverFacade(upstream, receiver);
        when(receiver.getEndpointStates()).thenReturn(receiverFacade.getEndpointStates());
        when(receiver.receive()).thenReturn(receiverFacade.getMessages());
        when(receiver.closeAsync()).thenReturn(Mono.empty());
        final RuntimeException endpointError = new RuntimeException("endpoint-error");

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(receiverFacade.emit())
                .thenRequest(5)
                .then(receiverFacade.emitEndpointStates(AmqpEndpointState.ACTIVE))
                .then(receiverFacade.errorEndpointStates(endpointError))
                .verifyErrorSatisfies(error -> {
                    Assertions.assertEquals(endpointError, error);
                });
        }
        upstream.assertCancelled();
        // The MessageFlux is terminated as a result of backing link (receiver) error, and that link no longer exists.
        StepVerifier.create(messageFlux.updateDisposition("t", Accepted.getInstance()))
            .verifyErrorSatisfies(dispositionError -> {
                Assertions.assertTrue(dispositionError instanceof DeliveryNotOnLinkException);
                final Throwable[] suppressed = dispositionError.getSuppressed();
                Assertions.assertNotNull(suppressed);
                Assertions.assertTrue(suppressed.length > 0);
                // Assert the endpoint error is included in the dispositionError.
                boolean foundEndpointError = false;
                for (Throwable e : suppressed) {
                    if (!foundEndpointError) {
                        foundEndpointError = (e == endpointError);
                    }
                }
                Assertions.assertTrue(foundEndpointError);
            });
    }

    @ParameterizedTest
    @MethodSource("creditFlowModePrefetch")
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldNotMakeDuplicateRetriesWhenRetryIsInProgress(CreditFlowMode creditFlowMode, int prefetch) {
        final int[] upstreamEmission = new int[1];
        final Sinks.Many<ReactorReceiver> upstreamSink = Sinks.many().unicast().onBackpressureBuffer();
        final Flux<ReactorReceiver> upstream = upstreamSink.asFlux().doOnRequest(r -> {
            Assertions.assertEquals(1, r);

            final ReactorReceiver receiver = mock(ReactorReceiver.class);
            when(receiver.closeAsync()).thenReturn(Mono.empty());

            upstreamEmission[0]++;
            switch (upstreamEmission[0]) {
                case 1:
                    when(receiver.getEndpointStates()).thenReturn(Flux.just(AmqpEndpointState.ACTIVE));
                    when(receiver.receive()).thenReturn(Flux.empty());
                    upstreamSink.emitNext(receiver, Sinks.EmitFailureHandler.FAIL_FAST);
                    break;

                case 2:
                    when(receiver.getEndpointStates())
                        .thenReturn(Flux.just(AmqpEndpointState.ACTIVE).concatWith(Flux.never()));
                    when(receiver.receive()).thenReturn(Flux.never());
                    upstreamSink.emitNext(receiver, Sinks.EmitFailureHandler.FAIL_FAST);
                    break;

                default:
                    when(receiver.getEndpointStates())
                        .thenReturn(Flux.error(new RuntimeException("unexpected request")));
                    when(receiver.receive()).thenReturn(Flux.error(new RuntimeException("unexpected request")));
                    upstreamSink.emitNext(receiver, Sinks.EmitFailureHandler.FAIL_FAST);
            }
        });

        final Duration backoffBeforeRequestingNextReceiver = Duration.ofSeconds(10);
        final AmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(
            new AmqpRetryOptions().setMaxRetries(MAX_RETRY).setDelay(backoffBeforeRequestingNextReceiver));
        final MessageFlux messageFlux = new MessageFlux(upstream, prefetch, creditFlowMode, retryPolicy);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .thenRequest(1)
                // The initial request ^ for one message, that leads to obtaining the first-receiver.
                // Since first-receiver gets terminated, the message-flux will initiate a retry that
                // will get the second-receiver only after 10 seconds.
                .thenAwait(Duration.ofSeconds(1))
                // After 1 sec ^, while there is still 9 seconds remaining in retry backoff, request for one
                // message, resulting a drain-loop iteration, which should not attempt to get another receiver.
                .thenRequest(1)
                .thenAwait(Duration.ofSeconds(15))
                // Await 15 seconds ^ so that retry backoff elapses and message-flux will get second-receiver.
                .thenCancel()
                .verify(Duration.ofSeconds(30));
        }

        // Assert there were only two emissions i.e. there were no attempt to obtain a receiver while retry
        // is in progress.
        Assertions.assertEquals(2, upstreamEmission[0]);
    }

    @ParameterizedTest
    @CsvSource({ "EmissionDriven,5", "RequestDriven,0" })
    @Execution(ExecutionMode.SAME_THREAD)
    public void receiverShouldGetRequestOnceEndpointIsActive(CreditFlowMode creditFlowMode, int prefetch) {
        final int request = 5;
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, RETRY_POLICY);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade receiverFacade = new ReactorReceiverFacade(upstream, receiver);
        when(receiver.getEndpointStates()).thenReturn(receiverFacade.getEndpointStates());
        when(receiver.receive()).thenReturn(receiverFacade.getMessages());
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(receiverFacade.emit())
                .thenRequest(request)
                .then(receiverFacade.emitAndCompleteEndpointStates(AmqpEndpointState.ACTIVE))
                .then(receiverFacade.completeMessages())
                .then(() -> upstream.complete())
                .verifyComplete();
        }

        Assertions.assertEquals(request, receiverFacade.getRequestedMessages());
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @CsvSource({ "EmissionDriven,5", "RequestDriven,0" })
    @Execution(ExecutionMode.SAME_THREAD)
    public void receiverShouldNotGetRequestIfEndpointIsNeverActive(CreditFlowMode creditFlowMode, int prefetch) {
        final int request = 5;
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, RETRY_POLICY);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade receiverFacade = new ReactorReceiverFacade(upstream, receiver);
        when(receiver.getEndpointStates()).thenReturn(receiverFacade.getEndpointStates());
        when(receiver.receive()).thenReturn(receiverFacade.getMessages());
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(receiverFacade.emit())
                .thenRequest(request)
                .then(receiverFacade.emitAndCompleteEndpointStates(AmqpEndpointState.UNINITIALIZED))
                .then(receiverFacade.completeMessages())
                .then(() -> upstream.complete())
                .verifyComplete();
        }

        Assertions.assertEquals(0, receiverFacade.getRequestedMessages());
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @CsvSource({ "EmissionDriven,256", "RequestDriven,0" })
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldDrainErroredReceiverBeforeGettingNextReceiver(CreditFlowMode creditFlowMode, int prefetch) {
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.createCold();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, RETRY_POLICY);

        final ReactorReceiver firstReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade firstReceiverFacade
            = new ReactorReceiverFacade(upstream, firstReceiver, TestPublisher.<Message>createCold());
        final AmqpException error = new AmqpException(true, "retriable", null);
        when(firstReceiver.getEndpointStates()).thenReturn(firstReceiverFacade.getEndpointStates());
        when(firstReceiver.receive()).thenReturn(firstReceiverFacade.getMessages());
        when(firstReceiver.closeAsync()).thenReturn(Mono.empty());

        final ReactorReceiver secondReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade secondReceiverFacade
            = new ReactorReceiverFacade(upstream, secondReceiver, TestPublisher.<Message>createCold());
        when(secondReceiver.getEndpointStates()).thenReturn(secondReceiverFacade.getEndpointStates());
        when(secondReceiver.receive()).thenReturn(secondReceiverFacade.getMessages());
        when(secondReceiver.closeAsync()).thenReturn(Mono.empty());

        final Deque<String> firstReceiverMessageIds = new ArrayDeque<>(4);
        final List<Message> firstReceiverMessages = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            final String id = "F:" + i;
            firstReceiverMessageIds.add(id);
            final Message message = mock(Message.class);
            when(message.getMessageId()).thenReturn(id);
            firstReceiverMessages.add(message);
        }

        final Deque<String> secondReceiverMessageIds = new ArrayDeque<>(6);
        final List<Message> secondReceiverMessages = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            final String id = "S:" + i;
            secondReceiverMessageIds.add(id);
            final Message message = mock(Message.class);
            when(message.getMessageId()).thenReturn(id);
            secondReceiverMessages.add(message);
        }

        final int firstReceiverMessagesCount = firstReceiverMessages.size();
        final int secondReceiverMessagesCount = secondReceiverMessages.size();
        final int request = firstReceiverMessagesCount + secondReceiverMessagesCount + 5;

        final boolean[] isFirstMessage = new boolean[1];
        final int[] firstReceiverRemainingEmissions = new int[1];
        isFirstMessage[0] = true;
        firstReceiverRemainingEmissions[0] = firstReceiverMessagesCount - 1;
        //
        final boolean[] isFifthMessage = new boolean[1];
        final int[] secondReceiverRemainingEmissions = new int[1];
        isFifthMessage[0] = true;
        secondReceiverRemainingEmissions[0] = secondReceiverMessagesCount - 1;

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(firstReceiverFacade.emitEndpointStates(AmqpEndpointState.ACTIVE))
                .then(firstReceiverFacade.emitMessage(firstReceiverMessages.get(0)))
                .then(secondReceiverFacade.emitEndpointStates(AmqpEndpointState.ACTIVE))
                .then(secondReceiverFacade.emitMessage(secondReceiverMessages.get(0)))
                .thenRequest(request)
                .then(firstReceiverFacade.emit())
                .thenAwait(UPSTREAM_DELAY_BEFORE_NEXT)
                .then(secondReceiverFacade.emit())
                .then(() -> upstream.complete())
                .thenConsumeWhile(__ -> {
                    if (isFirstMessage[0]) {
                        isFirstMessage[0] = false;
                        return true;
                    }
                    return false;
                }, firstMessage -> {
                    Assertions.assertEquals(firstReceiverMessageIds.poll(), firstMessage.getMessageId());
                    // The first receiver had backpressure request recorded (256 in EmissionDriven mode and 15 in
                    // RequestDriven mode).
                    // Here we got the first message from the first receiver through the 'messageSubscriber.onNext' call
                    // in the drain loop. While we're inside that 'onNext', we let the receiver emit the remaining 3
                    // messages.
                    //
                    // These 3 message emissions calls into 'ReactorReceiverMediator.onNext', since the WIP counter
                    // is not decremented (because control is not returned from 'messageSubscriber.onNext'), the
                    // messages
                    // gets buffered into 'ReactorReceiverMediator.queue'. After all the buffering, we let the endpoint
                    // signal terminal error.
                    firstReceiverFacade
                        .emitAndCompleteMessages(firstReceiverMessages.subList(1, firstReceiverMessagesCount))
                        .run();
                    firstReceiverFacade.errorEndpointStates(error).run();
                    // Goal of the test is to assert that - the buffered messages in a queue are drained before moving
                    // to the second receiver.
                })
                .thenConsumeWhile(__ -> {
                    if (firstReceiverRemainingEmissions[0] > 0) {
                        firstReceiverRemainingEmissions[0]--;
                        return true;
                    }
                    return false;
                }, m -> {
                    // Messages 2 to 4 (from first receiver).
                    Assertions.assertEquals(firstReceiverMessageIds.poll(), m.getMessageId());
                })
                .thenConsumeWhile(__ -> {
                    if (isFifthMessage[0]) {
                        isFifthMessage[0] = false;
                        return true;
                    }
                    return false;
                }, fifthMessage -> {
                    // Message 5 (from second receiver).
                    Assertions.assertEquals(secondReceiverMessageIds.poll(), fifthMessage.getMessageId());
                    secondReceiverFacade
                        .emitAndCompleteMessages(secondReceiverMessages.subList(1, secondReceiverMessagesCount))
                        .run();
                    secondReceiverFacade.completeEndpointStates().run();
                })
                .thenConsumeWhile(__ -> {
                    if (secondReceiverRemainingEmissions[0] > 0) {
                        secondReceiverRemainingEmissions[0]--;
                        return true;
                    }
                    return false;
                }, m -> {
                    // Messages 6 to 10 (from second receiver).
                    Assertions.assertEquals(secondReceiverMessageIds.poll(), m.getMessageId());
                })
                .verifyComplete();
        }

        Assertions.assertEquals(firstReceiverMessagesCount, firstReceiverFacade.getMessageEmitCount());
        Assertions.assertEquals(secondReceiverMessagesCount, secondReceiverFacade.getMessageEmitCount());
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @CsvSource({ "EmissionDriven,256", "RequestDriven,0" })
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldDrainCompletedReceiverBeforeGettingNextReceiver(CreditFlowMode creditFlowMode, int prefetch) {
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.createCold();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, RETRY_POLICY);

        final ReactorReceiver firstReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade firstReceiverFacade
            = new ReactorReceiverFacade(upstream, firstReceiver, TestPublisher.<Message>createCold());
        when(firstReceiver.getEndpointStates()).thenReturn(firstReceiverFacade.getEndpointStates());
        when(firstReceiver.receive()).thenReturn(firstReceiverFacade.getMessages());
        when(firstReceiver.closeAsync()).thenReturn(Mono.empty());

        final ReactorReceiver secondReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade secondReceiverFacade
            = new ReactorReceiverFacade(upstream, secondReceiver, TestPublisher.<Message>createCold());
        when(secondReceiver.getEndpointStates()).thenReturn(secondReceiverFacade.getEndpointStates());
        when(secondReceiver.receive()).thenReturn(secondReceiverFacade.getMessages());
        when(secondReceiver.closeAsync()).thenReturn(Mono.empty());

        final Deque<String> firstReceiverMessageIds = new ArrayDeque<>(4);
        final List<Message> firstReceiverMessages = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            final String id = "F:" + i;
            firstReceiverMessageIds.add(id);
            final Message message = mock(Message.class);
            when(message.getMessageId()).thenReturn(id);
            firstReceiverMessages.add(message);
        }

        final Deque<String> secondReceiverMessageIds = new ArrayDeque<>(6);
        final List<Message> secondReceiverMessages = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            final String id = "S:" + i;
            secondReceiverMessageIds.add(id);
            final Message message = mock(Message.class);
            when(message.getMessageId()).thenReturn(id);
            secondReceiverMessages.add(message);
        }

        final int firstReceiverMessagesCount = firstReceiverMessages.size();
        final int secondReceiverMessagesCount = secondReceiverMessages.size();
        final int request = firstReceiverMessagesCount + secondReceiverMessagesCount + 5;

        final boolean[] isFirstMessage = new boolean[1];
        final int[] firstReceiverRemainingEmissions = new int[1];
        isFirstMessage[0] = true;
        firstReceiverRemainingEmissions[0] = firstReceiverMessagesCount - 1;
        //
        final boolean[] isFifthMessage = new boolean[1];
        final int[] secondReceiverRemainingEmissions = new int[1];
        isFifthMessage[0] = true;
        secondReceiverRemainingEmissions[0] = secondReceiverMessagesCount - 1;

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(firstReceiverFacade.emitEndpointStates(AmqpEndpointState.ACTIVE))
                .then(firstReceiverFacade.emitMessage(firstReceiverMessages.get(0)))
                .then(secondReceiverFacade.emitEndpointStates(AmqpEndpointState.ACTIVE))
                .then(secondReceiverFacade.emitMessage(secondReceiverMessages.get(0)))
                .thenRequest(request)
                .then(firstReceiverFacade.emit())
                .thenAwait(UPSTREAM_DELAY_BEFORE_NEXT)
                .then(secondReceiverFacade.emit())
                .then(() -> upstream.complete())
                .thenConsumeWhile(__ -> {
                    if (isFirstMessage[0]) {
                        isFirstMessage[0] = false;
                        return true;
                    }
                    return false;
                }, firstMessage -> {
                    Assertions.assertEquals(firstReceiverMessageIds.poll(), firstMessage.getMessageId());
                    // Refer Notes for test: 'shouldDrainErroredReceiverBeforeGettingNextReceiver'
                    firstReceiverFacade
                        .emitAndCompleteMessages(firstReceiverMessages.subList(1, firstReceiverMessagesCount))
                        .run();
                    firstReceiverFacade.completeEndpointStates().run();
                })
                .thenConsumeWhile(__ -> {
                    if (firstReceiverRemainingEmissions[0] > 0) {
                        firstReceiverRemainingEmissions[0]--;
                        return true;
                    }
                    return false;
                }, m -> {
                    // Messages 2 to 4 (from first receiver).
                    Assertions.assertEquals(firstReceiverMessageIds.poll(), m.getMessageId());
                })
                .thenConsumeWhile(__ -> {
                    if (isFifthMessage[0]) {
                        isFifthMessage[0] = false;
                        return true;
                    }
                    return false;
                }, fifthMessage -> {
                    // Message 5 (from second receiver).
                    Assertions.assertEquals(secondReceiverMessageIds.poll(), fifthMessage.getMessageId());
                    secondReceiverFacade
                        .emitAndCompleteMessages(secondReceiverMessages.subList(1, secondReceiverMessagesCount))
                        .run();
                    secondReceiverFacade.completeEndpointStates().run();
                })
                .thenConsumeWhile(__ -> {
                    if (secondReceiverRemainingEmissions[0] > 0) {
                        secondReceiverRemainingEmissions[0]--;
                        return true;
                    }
                    return false;
                }, m -> {
                    // Messages 6 to 10 (from second receiver).
                    Assertions.assertEquals(secondReceiverMessageIds.poll(), m.getMessageId());
                })
                .verifyComplete();
        }

        Assertions.assertEquals(firstReceiverMessagesCount, firstReceiverFacade.getMessageEmitCount());
        Assertions.assertEquals(secondReceiverMessagesCount, secondReceiverFacade.getMessageEmitCount());
        upstream.assertCancelled();
    }

    @ParameterizedTest
    @CsvSource({ "EmissionDriven,true,1", "RequestDriven,true,0", "EmissionDriven,false,1", "RequestDriven,false,0" })
    @Execution(ExecutionMode.SAME_THREAD)
    public void canCompleteDownstreamWithoutUpstreamTermination(CreditFlowMode creditFlowMode,
        boolean takeUntilOtherOrFirstWithSignal, int prefetch) {
        // The test validates it is possible to complete the downstream (message subscriber) by applying
        // 'takeUntilOther' and 'firstWithSignal' operator on MessageFlux without needing MessageFlux's upstream
        // to send a termination signal.
        // There is use case that the consumer client's closure associated with MessageFlux requires
        // completing the downstream while other clients and their MessageFlux are still interested in
        // using the upstream.
        //
        // Test asserts that, using 'takeUntilOther' and 'firstWithSignal' -
        // 1. the MessageFlux's downstream (message subscriber) can be completed.
        // 2. the underlying ReactorReceiver (i.e. AmqpReceiveLink) backing the MessageFlux gets closed (via
        // cancellation).
        //
        // Test also helps to catch any external regression e.g, https://github.com/reactor/reactor-core/issues/3268
        //
        final Sinks.Empty<Void> completionSink = Sinks.empty();
        final AmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(new AmqpRetryOptions());
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), prefetch, creditFlowMode, retryPolicy);

        final ReactorReceiver receiver = mock(ReactorReceiver.class);
        when(receiver.receive()).thenReturn(Flux.never());
        when(receiver.getEndpointStates()).thenReturn(Flux.just(AmqpEndpointState.ACTIVE).concatWith(Flux.never()));
        when(receiver.closeAsync()).thenReturn(Mono.empty());

        final boolean useTakeUntilOther = takeUntilOtherOrFirstWithSignal;
        if (useTakeUntilOther) {
            final Supplier<Flux<Message>> scenario = () -> {
                return messageFlux.takeUntilOther(completionSink.asMono());
            };
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(scenario)
                    .then(() -> upstream.next(receiver))
                    .then(() -> completionSink.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST))
                    .verifyComplete();
            }
        } else {
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                final Supplier<Flux<Void>> scenario = () -> {
                    return Mono.firstWithSignal(messageFlux.ignoreElements().then(), completionSink.asMono()).flux();
                };
                verifier.create(scenario)
                    .then(() -> upstream.next(receiver))
                    .then(() -> completionSink.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST))
                    .verifyComplete();
            }
        }

        verify(receiver).closeAsync();
        upstream.assertCancelled();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldTransferRequestToNextReceiver() {
        final int request = 10;
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), 0, CreditFlowMode.RequestDriven, RETRY_POLICY);

        final ReactorReceiver firstReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade firstReceiverFacade = new ReactorReceiverFacade(upstream, firstReceiver);
        final AmqpException error = new AmqpException(true, "retriable", null);
        when(firstReceiver.getEndpointStates()).thenReturn(firstReceiverFacade.getEndpointStates());
        when(firstReceiver.receive()).thenReturn(firstReceiverFacade.getMessages());
        when(firstReceiver.closeAsync()).thenReturn(Mono.empty());

        final ReactorReceiver secondReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade secondReceiverFacade = new ReactorReceiverFacade(upstream, secondReceiver);
        when(secondReceiver.getEndpointStates()).thenReturn(secondReceiverFacade.getEndpointStates());
        when(secondReceiver.receive()).thenReturn(secondReceiverFacade.getMessages());
        when(secondReceiver.closeAsync()).thenReturn(Mono.empty());

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(firstReceiverFacade.emit())
                .thenRequest(request)
                .then(firstReceiverFacade.emitAndErrorEndpointStates(AmqpEndpointState.ACTIVE, error))
                .then(firstReceiverFacade.completeMessages())
                .thenAwait(UPSTREAM_DELAY_BEFORE_NEXT)
                .then(secondReceiverFacade.emit())
                .then(secondReceiverFacade.emitAndCompleteEndpointStates(AmqpEndpointState.ACTIVE))
                .then(secondReceiverFacade.completeMessages())
                .then(() -> upstream.complete())
                .verifyComplete();
        }

        Assertions.assertEquals(request, firstReceiverFacade.getRequestedMessages());
        Assertions.assertEquals(request, secondReceiverFacade.getRequestedMessages());
        upstream.assertCancelled();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldTransferPendingRequestToNextReceiver() {
        final Duration retryDelay = Duration.ofSeconds(1);
        final TestPublisher<ReactorReceiver> upstream = TestPublisher.create();
        final MessageFlux messageFlux = new MessageFlux(upstream.flux(), 0, CreditFlowMode.RequestDriven, RETRY_POLICY);

        final ReactorReceiver firstReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade firstReceiverFacade = new ReactorReceiverFacade(upstream, firstReceiver);
        when(firstReceiver.getEndpointStates()).thenReturn(firstReceiverFacade.getEndpointStates());
        when(firstReceiver.receive()).thenReturn(firstReceiverFacade.getMessages());
        when(firstReceiver.closeAsync()).thenReturn(Mono.empty());

        final ReactorReceiver secondReceiver = mock(ReactorReceiver.class);
        final ReactorReceiverFacade secondReceiverFacade = new ReactorReceiverFacade(upstream, secondReceiver);
        when(secondReceiver.getEndpointStates()).thenReturn(secondReceiverFacade.getEndpointStates());
        when(secondReceiver.receive()).thenReturn(secondReceiverFacade.getMessages());
        when(secondReceiver.closeAsync()).thenReturn(Mono.empty());

        final Message message = mock(Message.class);
        final List<Message> firstReceiverMessages = generateMessages(message, 4);
        final List<Message> secondReceiverMessages = generateMessages(message, 6);
        final int firstReceiverMessagesCount = firstReceiverMessages.size();
        final int secondReceiverMessagesCount = secondReceiverMessages.size();
        final int request = firstReceiverMessagesCount + secondReceiverMessagesCount + 5; // 15

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> messageFlux)
                .then(firstReceiverFacade.emit())
                .thenRequest(request)
                .then(firstReceiverFacade.emitEndpointStates(AmqpEndpointState.ACTIVE))
                .then(firstReceiverFacade.emitAndCompleteMessages(firstReceiverMessages))
                .then(firstReceiverFacade.completeEndpointStates())
                .thenAwait(retryDelay.plusSeconds(1))
                .then(secondReceiverFacade.emit())
                .then(secondReceiverFacade.emitEndpointStates(AmqpEndpointState.ACTIVE))
                .then(secondReceiverFacade.emitAndCompleteMessages(secondReceiverMessages))
                .then(secondReceiverFacade.completeMessages())
                .then(() -> upstream.complete())
                .expectNextCount(firstReceiverMessagesCount + secondReceiverMessagesCount)
                .thenConsumeWhile(m -> true)
                .verifyComplete();
        }

        final int expectedRequestToFirstReceiver = request;
        final int expectedRequestToSecondReceiver = request - firstReceiverMessagesCount;
        Assertions.assertEquals(expectedRequestToFirstReceiver, firstReceiverFacade.getRequestedMessages());
        Assertions.assertEquals(expectedRequestToSecondReceiver, secondReceiverFacade.getRequestedMessages());
        upstream.assertCancelled();
    }

    private static List<Message> generateMessages(Message message, int count) {
        return IntStream.rangeClosed(1, count).mapToObj(__ -> message).collect(Collectors.toList());
    }
}
