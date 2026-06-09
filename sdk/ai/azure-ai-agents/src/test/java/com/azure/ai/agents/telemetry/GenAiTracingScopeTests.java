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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link GenAiTracingScope} verifying span lifecycle and tracing gate.
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class GenAiTracingScopeTests {

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

    // --- Tracing gate tests ---

    @Test
    void startCreateAgent_tracingDisabled_returnsNull() {
        GenAiTracingScope scope = GenAiTracingScope.startCreateAgent("TestAgent", TEST_ENDPOINT);
        assertNull(scope);
    }

    @Test
    void startInvokeAgent_tracingDisabled_returnsNull() {
        GenAiTracingScope scope = GenAiTracingScope.startInvokeAgent("TestAgent", TEST_ENDPOINT);
        assertNull(scope);
    }

    @Test
    void startChat_tracingDisabled_returnsNull() {
        GenAiTracingScope scope = GenAiTracingScope.startChat("gpt-4.1", TEST_ENDPOINT);
        assertNull(scope);
    }

    @Test
    void startCreateConversation_tracingDisabled_returnsNull() {
        GenAiTracingScope scope = GenAiTracingScope.startCreateConversation(TEST_ENDPOINT);
        assertNull(scope);
    }

    @Test
    void startScope_afterDisable_returnsNull() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions());
        GenAiTracingConfiguration.disableGenAiTracing();

        GenAiTracingScope scope = GenAiTracingScope.startChat("gpt-4.1", TEST_ENDPOINT);
        assertNull(scope);
    }

    // --- Tracing enabled tests (with NoOp tracer) ---
    // Note: Without an actual OpenTelemetry SDK registered, the tracer is a no-op
    // and may return null or not create spans. These tests verify no exceptions are thrown.

    @Test
    void startCreateAgent_tracingEnabled_noException() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions());

        // With no-op tracer, scope may be null (no listeners), but should not throw
        assertDoesNotThrow(() -> {
            GenAiTracingScope scope = GenAiTracingScope.startCreateAgent("TestAgent", TEST_ENDPOINT);
            if (scope != null) {
                scope.setAgentAttributes("TestAgent:1", "TestAgent", "1", "prompt");
                scope.setRequestModelAttributes("gpt-4.1", 0.7, 0.9);
                scope.setSystemInstructions("You are a helpful assistant.");
                scope.close();
            }
        });
    }

    @Test
    void startChat_tracingEnabled_noException() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions());

        assertDoesNotThrow(() -> {
            GenAiTracingScope scope = GenAiTracingScope.startChat("gpt-4.1", TEST_ENDPOINT);
            if (scope != null) {
                scope.setInputMessages("[{\"role\":\"user\",\"parts\":[{\"type\":\"text\"}]}]");
                scope.setResponseAttributes("resp_123", "gpt-4.1", 19L, 8L, null);
                scope.setOutputMessages(
                    "[{\"role\":\"assistant\",\"parts\":[{\"type\":\"text\"}],\"finish_reason\":\"completed\"}]");
                scope.close();
            }
        });
    }

    @Test
    void scope_closeIsIdempotent() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions());

        assertDoesNotThrow(() -> {
            GenAiTracingScope scope = GenAiTracingScope.startChat("gpt-4.1", TEST_ENDPOINT);
            if (scope != null) {
                scope.close();
                scope.close(); // second close should be no-op
            }
        });
    }

    @Test
    void scope_recordError_noException() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions());

        assertDoesNotThrow(() -> {
            GenAiTracingScope scope = GenAiTracingScope.startChat("gpt-4.1", TEST_ENDPOINT);
            if (scope != null) {
                scope.recordError(new RuntimeException("test error"));
                scope.close();
            }
        });
    }

    @Test
    void scope_hostedAgentAttributes_noException() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions());

        assertDoesNotThrow(() -> {
            GenAiTracingScope scope = GenAiTracingScope.startCreateAgent("HostedAgent", TEST_ENDPOINT);
            if (scope != null) {
                scope.setAgentAttributes("HostedAgent:1", "HostedAgent", "1", "hosted");
                scope.setHostedAgentAttributes("0.5", "1Gi", "myregistry.azurecr.io/image:latest", "responses",
                    "1.0.0");
                scope.close();
            }
        });
    }
}
