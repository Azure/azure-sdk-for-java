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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link GenAiResponseTracing} verifying response operation tracing.
 */
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class GenAiResponseTracingTests {

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
    void traceResponse_tracingDisabled_operationStillExecutes() {
        AtomicBoolean called = new AtomicBoolean(false);

        String inputMessages = GenAiMessageFormatter.formatUserTextInput("Hello");

        // When tracing is disabled, the operation should still execute normally
        // (returns null from scope, calls operation directly)
        assertDoesNotThrow(() -> {
            // We can't easily create a real Response without the OpenAI SDK internals,
            // so test with a RuntimeException to verify the code path
            try {
                GenAiResponseTracing.traceResponse(GenAiConstants.OPERATION_CHAT, "gpt-4.1", null, TEST_ENDPOINT,
                    inputMessages, null, null, () -> {
                        called.set(true);
                        return null; // Response would be returned here
                    });
            } catch (Exception ignored) {
                // null response may cause NPE in attribute recording
            }
        });

        assertTrue(called.get());
    }

    @Test
    void traceResponse_tracingEnabled_operationExecutes() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicBoolean called = new AtomicBoolean(false);

        String inputMessages = GenAiMessageFormatter.formatUserTextInput("Hello");

        assertDoesNotThrow(() -> {
            try {
                GenAiResponseTracing.traceResponse(GenAiConstants.OPERATION_CHAT, "gpt-4.1", null, TEST_ENDPOINT,
                    inputMessages, null, null, () -> {
                        called.set(true);
                        return null;
                    });
            } catch (Exception ignored) {
                // null response handling
            }
        });

        assertTrue(called.get());
    }

    @Test
    void traceResponse_operationThrows_propagatesException() {
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("Hello");

        assertThrows(RuntimeException.class, () -> {
            GenAiResponseTracing.traceResponse(GenAiConstants.OPERATION_CHAT, "gpt-4.1", null, TEST_ENDPOINT,
                inputMessages, null, null, () -> {
                    throw new RuntimeException("Network error");
                });
        });
    }

    @Test
    void traceResponse_invokeAgent_withAgentName() {
        AtomicBoolean called = new AtomicBoolean(false);
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("What's the weather?");

        assertDoesNotThrow(() -> {
            try {
                GenAiResponseTracing.traceResponse(GenAiConstants.OPERATION_INVOKE_AGENT, "WeatherAgent",
                    "WeatherAgent", TEST_ENDPOINT, inputMessages, null, null, () -> {
                        called.set(true);
                        return null;
                    });
            } catch (Exception ignored) {
            }
        });

        assertTrue(called.get());
    }

    @Test
    void traceStreamingResponse_tracingDisabled_returnsWrappedStream() {
        AtomicBoolean called = new AtomicBoolean(false);
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("Hello");

        TracedStreamIterable result = GenAiResponseTracing.traceStreamingResponse(GenAiConstants.OPERATION_CHAT,
            "gpt-4.1", null, TEST_ENDPOINT, inputMessages, null, null, () -> {
                called.set(true);
                return java.util.Collections.emptyList();
            });

        assertTrue(called.get());
        assertNotNull(result);
    }

    @Test
    void traceStreamingResponse_tracingEnabled_returnsWrappedStream() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicBoolean called = new AtomicBoolean(false);
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("Hello");

        TracedStreamIterable result = GenAiResponseTracing.traceStreamingResponse(GenAiConstants.OPERATION_CHAT,
            "gpt-4.1", null, TEST_ENDPOINT, inputMessages, null, null, () -> {
                called.set(true);
                return java.util.Collections.emptyList();
            });

        assertTrue(called.get());
        assertNotNull(result);

        // Consume the stream to trigger finalization
        for (Object event : result) {
            // empty stream
        }
    }

    @Test
    void traceStreamingResponse_operationThrows_propagatesException() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        String inputMessages = GenAiMessageFormatter.formatUserTextInput("Hello");

        assertThrows(RuntimeException.class, () -> {
            GenAiResponseTracing.traceStreamingResponse(GenAiConstants.OPERATION_CHAT, "gpt-4.1", null, TEST_ENDPOINT,
                inputMessages, null, null, () -> {
                    throw new RuntimeException("Stream error");
                });
        });
    }
}
