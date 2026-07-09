// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.ai.agents.telemetry.GenAiTracingConfiguration;
import com.azure.ai.agents.telemetry.GenAiTracingOptions;
import com.openai.models.responses.ResponseStreamEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link GenAiResponseTracing} async methods verifying response operation tracing.
 */
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class GenAiResponseTracingAsyncTests {

    private static final URI TEST_ENDPOINT
        = URI.create("https://test-resource.services.ai.azure.com/api/projects/test");

    @BeforeEach
    void setUp() {
        GenAiTracingConfiguration.disableGenAiTracing();
    }

    @AfterEach
    void tearDown() {
        GenAiTracingConfiguration.disableGenAiTracing();
    }

    // --- Non-streaming async tests ---

    @Test
    void traceResponseAsync_tracingDisabled_operationStillExecutes() {
        AtomicBoolean called = new AtomicBoolean(false);
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("Hello");

        Mono<com.openai.models.responses.Response> result
            = GenAiResponseTracing.traceResponseAsync(GenAiConstants.OPERATION_CHAT, "gpt-4.1", null, TEST_ENDPOINT,
                inputMessages, null, null, Mono.fromCallable(() -> {
                    called.set(true);
                    return null; // Response would be returned here
                }));

        StepVerifier.create(result).verifyComplete();
        assertTrue(called.get());
    }

    @Test
    void traceResponseAsync_tracingEnabled_operationExecutes() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicBoolean called = new AtomicBoolean(false);
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("Hello");

        Mono<com.openai.models.responses.Response> result
            = GenAiResponseTracing.traceResponseAsync(GenAiConstants.OPERATION_CHAT, "gpt-4.1", null, TEST_ENDPOINT,
                inputMessages, null, null, Mono.fromCallable(() -> {
                    called.set(true);
                    return null;
                }));

        StepVerifier.create(result).verifyComplete();
        assertTrue(called.get());
    }

    @Test
    void traceResponseAsync_operationThrows_propagatesError() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("Hello");

        Mono<com.openai.models.responses.Response> result
            = GenAiResponseTracing.traceResponseAsync(GenAiConstants.OPERATION_CHAT, "gpt-4.1", null, TEST_ENDPOINT,
                inputMessages, null, null, Mono.error(new RuntimeException("Network error")));

        StepVerifier.create(result)
            .expectErrorMatches(e -> e instanceof RuntimeException && "Network error".equals(e.getMessage()))
            .verify();
    }

    @Test
    void traceResponseAsync_invokeAgent_withAgentName() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicBoolean called = new AtomicBoolean(false);
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("What's the weather?");

        Mono<com.openai.models.responses.Response> result
            = GenAiResponseTracing.traceResponseAsync(GenAiConstants.OPERATION_INVOKE_AGENT, "WeatherAgent",
                "WeatherAgent", TEST_ENDPOINT, inputMessages, null, null, Mono.fromCallable(() -> {
                    called.set(true);
                    return null;
                }));

        StepVerifier.create(result).verifyComplete();
        assertTrue(called.get());
    }

    @Test
    void traceResponseAsync_isLazy_doesNotExecuteUntilSubscribed() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicBoolean called = new AtomicBoolean(false);
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("Hello");

        Mono<com.openai.models.responses.Response> result
            = GenAiResponseTracing.traceResponseAsync(GenAiConstants.OPERATION_CHAT, "gpt-4.1", null, TEST_ENDPOINT,
                inputMessages, null, null, Mono.fromCallable(() -> {
                    called.set(true);
                    return null;
                }));

        // Should not have been called yet (lazy evaluation via Mono.defer)
        assertTrue(!called.get(), "Operation should not execute until subscribed");

        StepVerifier.create(result).verifyComplete();
        assertTrue(called.get());
    }

    // --- Streaming async tests ---

    @Test
    void traceStreamingResponseAsync_tracingDisabled_returnsStream() {
        AtomicBoolean subscribed = new AtomicBoolean(false);
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("Hello");

        Flux<ResponseStreamEvent> result = GenAiResponseTracing.traceStreamingResponseAsync(
            GenAiConstants.OPERATION_CHAT, "gpt-4.1", null, TEST_ENDPOINT, inputMessages, null, null,
            Flux.<ResponseStreamEvent>empty().doOnSubscribe(s -> subscribed.set(true)));

        StepVerifier.create(result).verifyComplete();
        assertTrue(subscribed.get());
    }

    @Test
    void traceStreamingResponseAsync_tracingEnabled_returnsStream() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicBoolean subscribed = new AtomicBoolean(false);
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("Hello");

        Flux<ResponseStreamEvent> result = GenAiResponseTracing.traceStreamingResponseAsync(
            GenAiConstants.OPERATION_CHAT, "gpt-4.1", null, TEST_ENDPOINT, inputMessages, null, null,
            Flux.<ResponseStreamEvent>empty().doOnSubscribe(s -> subscribed.set(true)));

        StepVerifier.create(result).verifyComplete();
        assertTrue(subscribed.get());
    }

    @Test
    void traceStreamingResponseAsync_operationThrows_propagatesError() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("Hello");

        Flux<ResponseStreamEvent> result
            = GenAiResponseTracing.traceStreamingResponseAsync(GenAiConstants.OPERATION_CHAT, "gpt-4.1", null,
                TEST_ENDPOINT, inputMessages, null, null, Flux.error(new RuntimeException("Stream error")));

        StepVerifier.create(result)
            .expectErrorMatches(e -> e instanceof RuntimeException && "Stream error".equals(e.getMessage()))
            .verify();
    }

    @Test
    void traceStreamingResponseAsync_invokeAgent_withAgentName() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicBoolean subscribed = new AtomicBoolean(false);
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("What's the weather?");

        Flux<ResponseStreamEvent> result = GenAiResponseTracing.traceStreamingResponseAsync(
            GenAiConstants.OPERATION_INVOKE_AGENT, "WeatherAgent", "WeatherAgent", TEST_ENDPOINT, inputMessages, null,
            null, Flux.<ResponseStreamEvent>empty().doOnSubscribe(s -> subscribed.set(true)));

        StepVerifier.create(result).verifyComplete();
        assertTrue(subscribed.get());
    }

    @Test
    void traceStreamingResponseAsync_isLazy_doesNotExecuteUntilSubscribed() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicBoolean subscribed = new AtomicBoolean(false);
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("Hello");

        Flux<ResponseStreamEvent> result = GenAiResponseTracing.traceStreamingResponseAsync(
            GenAiConstants.OPERATION_CHAT, "gpt-4.1", null, TEST_ENDPOINT, inputMessages, null, null,
            Flux.<ResponseStreamEvent>empty().doOnSubscribe(s -> subscribed.set(true)));

        // Should not have subscribed yet (lazy evaluation via Flux.defer)
        assertTrue(!subscribed.get(), "Stream should not execute until subscribed");

        StepVerifier.create(result).verifyComplete();
        assertTrue(subscribed.get());
    }
}
