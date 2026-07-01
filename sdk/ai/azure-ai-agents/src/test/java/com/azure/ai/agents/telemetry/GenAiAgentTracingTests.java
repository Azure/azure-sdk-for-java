// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link GenAiAgentTracing} verifying agent CRUD tracing integration.
 */
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class GenAiAgentTracingTests {

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
    void traceCreateAgent_tracingDisabled_operationStillExecutes() {
        AtomicBoolean called = new AtomicBoolean(false);

        String result = GenAiAgentTracing.traceCreateAgent("MyAgent", TEST_ENDPOINT, "MyAgent:1", "1", "prompt",
            "gpt-4.1", 0.7, 0.9, "You are helpful.", () -> {
                called.set(true);
                return "success";
            });

        assertTrue(called.get());
        assertEquals("success", result);
    }

    @Test
    void traceCreateAgent_tracingEnabled_operationStillExecutes() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicBoolean called = new AtomicBoolean(false);

        String result = GenAiAgentTracing.traceCreateAgent("MyAgent", TEST_ENDPOINT, "MyAgent:1", "1", "prompt",
            "gpt-4.1", 0.7, 0.9, "You are helpful.", () -> {
                called.set(true);
                return "success";
            });

        assertTrue(called.get());
        assertEquals("success", result);
    }

    @Test
    void traceCreateAgent_operationThrows_propagatesException() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));

        assertThrows(RuntimeException.class, () -> {
            GenAiAgentTracing.traceCreateAgent("MyAgent", TEST_ENDPOINT, "MyAgent:1", "1", "prompt", "gpt-4.1", null,
                null, null, () -> {
                    throw new RuntimeException("API error");
                });
        });
    }

    @Test
    void traceCreateHostedAgent_tracingDisabled_operationStillExecutes() {
        AtomicBoolean called = new AtomicBoolean(false);

        String result = GenAiAgentTracing.traceCreateHostedAgent("HostedAgent", TEST_ENDPOINT, "HostedAgent:1", "1",
            "gpt-4.1", null, null, "Instructions", "0.5", "1Gi", "image:latest", "responses", "1.0.0", () -> {
                called.set(true);
                return "hosted-result";
            });

        assertTrue(called.get());
        assertEquals("hosted-result", result);
    }

    @Test
    void traceCreateConversation_tracingDisabled_operationStillExecutes() {
        AtomicBoolean called = new AtomicBoolean(false);

        String result = GenAiAgentTracing.traceCreateConversation(TEST_ENDPOINT, () -> {
            called.set(true);
            return "conversation-id";
        });

        assertTrue(called.get());
        assertEquals("conversation-id", result);
    }

    @Test
    void traceCreateAgent_calledMultipleTimes_eachCallSucceeds() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicInteger callCount = new AtomicInteger(0);

        for (int i = 0; i < 3; i++) {
            GenAiAgentTracing.traceCreateAgent("Agent" + i, TEST_ENDPOINT, "Agent" + i + ":1", "1", "prompt", "gpt-4.1",
                null, null, null, () -> {
                    callCount.incrementAndGet();
                    return "ok";
                });
        }

        assertEquals(3, callCount.get());
    }

    @Test
    void traceCreateAgent_afterDisable_noTracingOverhead() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        GenAiTracingConfiguration.disableGenAiTracing();

        AtomicBoolean called = new AtomicBoolean(false);

        assertDoesNotThrow(() -> {
            GenAiAgentTracing.traceCreateAgent("MyAgent", TEST_ENDPOINT, "MyAgent:1", "1", "prompt", "gpt-4.1", null,
                null, null, () -> {
                    called.set(true);
                    return "result";
                });
        });

        assertTrue(called.get());
    }
}
