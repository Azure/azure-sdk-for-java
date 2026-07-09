// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.ai.agents.telemetry.GenAiTracingConfiguration;
import com.azure.ai.agents.telemetry.GenAiTracingOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link GenAiAgentTracing} async methods verifying agent CRUD tracing integration.
 */
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class GenAiAgentTracingAsyncTests {

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

    @Test
    void traceCreateAgentAsync_tracingDisabled_operationStillExecutes() {
        AtomicBoolean called = new AtomicBoolean(false);

        Mono<String> result = GenAiAgentTracing.traceCreateAgentAsync("MyAgent", TEST_ENDPOINT, "MyAgent:1", "1",
            "prompt", "gpt-4.1", 0.7, 0.9, "You are helpful.", null, Mono.fromCallable(() -> {
                called.set(true);
                return "success";
            }));

        StepVerifier.create(result).expectNext("success").verifyComplete();
        assertTrue(called.get());
    }

    @Test
    void traceCreateAgentAsync_tracingEnabled_operationStillExecutes() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicBoolean called = new AtomicBoolean(false);

        Mono<String> result = GenAiAgentTracing.traceCreateAgentAsync("MyAgent", TEST_ENDPOINT, "MyAgent:1", "1",
            "prompt", "gpt-4.1", 0.7, 0.9, "You are helpful.", null, Mono.fromCallable(() -> {
                called.set(true);
                return "success";
            }));

        StepVerifier.create(result).expectNext("success").verifyComplete();
        assertTrue(called.get());
    }

    @Test
    void traceCreateAgentAsync_operationThrows_propagatesError() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));

        Mono<String> result = GenAiAgentTracing.traceCreateAgentAsync("MyAgent", TEST_ENDPOINT, "MyAgent:1", "1",
            "prompt", "gpt-4.1", null, null, null, null, Mono.error(new RuntimeException("API error")));

        StepVerifier.create(result)
            .expectErrorMatches(e -> e instanceof RuntimeException && "API error".equals(e.getMessage()))
            .verify();
    }

    @Test
    void traceCreateHostedAgentAsync_tracingDisabled_operationStillExecutes() {
        AtomicBoolean called = new AtomicBoolean(false);

        Mono<String> result = GenAiAgentTracing.traceCreateHostedAgentAsync("HostedAgent", TEST_ENDPOINT,
            "HostedAgent:1", "1", "gpt-4.1", null, null, "Instructions", "0.5", "1Gi", "image:latest", "responses",
            "1.0.0", Mono.fromCallable(() -> {
                called.set(true);
                return "hosted-result";
            }));

        StepVerifier.create(result).expectNext("hosted-result").verifyComplete();
        assertTrue(called.get());
    }

    @Test
    void traceCreateHostedAgentAsync_tracingEnabled_operationStillExecutes() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicBoolean called = new AtomicBoolean(false);

        Mono<String> result = GenAiAgentTracing.traceCreateHostedAgentAsync("HostedAgent", TEST_ENDPOINT,
            "HostedAgent:1", "1", "gpt-4.1", null, null, "Instructions", "0.5", "1Gi", "image:latest", "responses",
            "1.0.0", Mono.fromCallable(() -> {
                called.set(true);
                return "hosted-result";
            }));

        StepVerifier.create(result).expectNext("hosted-result").verifyComplete();
        assertTrue(called.get());
    }

    @Test
    void traceCreateAgentAsync_calledMultipleTimes_eachCallSucceeds() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicInteger callCount = new AtomicInteger(0);

        for (int i = 0; i < 3; i++) {
            Mono<String> result = GenAiAgentTracing.traceCreateAgentAsync("Agent" + i, TEST_ENDPOINT,
                "Agent" + i + ":1", "1", "prompt", "gpt-4.1", null, null, null, null, Mono.fromCallable(() -> {
                    callCount.incrementAndGet();
                    return "ok";
                }));

            StepVerifier.create(result).expectNext("ok").verifyComplete();
        }

        assertTrue(callCount.get() == 3);
    }

    @Test
    void traceCreateAgentAsync_afterDisable_noTracingOverhead() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        GenAiTracingConfiguration.disableGenAiTracing();

        AtomicBoolean called = new AtomicBoolean(false);

        Mono<String> result = GenAiAgentTracing.traceCreateAgentAsync("MyAgent", TEST_ENDPOINT, "MyAgent:1", "1",
            "prompt", "gpt-4.1", null, null, null, null, Mono.fromCallable(() -> {
                called.set(true);
                return "result";
            }));

        StepVerifier.create(result).expectNext("result").verifyComplete();
        assertTrue(called.get());
    }

    @Test
    void traceCreateAgentAsync_isLazy_doesNotExecuteUntilSubscribed() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicBoolean called = new AtomicBoolean(false);

        // Create the Mono but don't subscribe
        Mono<String> result = GenAiAgentTracing.traceCreateAgentAsync("MyAgent", TEST_ENDPOINT, "MyAgent:1", "1",
            "prompt", "gpt-4.1", null, null, null, null, Mono.fromCallable(() -> {
                called.set(true);
                return "success";
            }));

        // Operation should NOT have been called yet (lazy evaluation)
        assertTrue(!called.get(), "Operation should not execute until subscribed");

        // Now subscribe
        StepVerifier.create(result).expectNext("success").verifyComplete();
        assertTrue(called.get());
    }
}
