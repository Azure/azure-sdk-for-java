// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Configuration for GenAI tracing. Manages enablement flags, content recording,
 * and trace context propagation settings.
 *
 * <p>All GenAI tracing is experimental and must be explicitly enabled.</p>
 *
 * <p>Configuration precedence: programmatic options &gt; environment variables &gt; defaults.</p>
 */
public final class GenAiTracingConfiguration {

    private static final ClientLogger LOGGER = new ClientLogger(GenAiTracingConfiguration.class);

    /**
     * Environment variable that enables GenAI tracing when set to "true" or "1".
     */
    static final String ENV_ENABLE_GENAI_TRACING = "AZURE_EXPERIMENTAL_ENABLE_GENAI_TRACING";

    /**
     * Environment variable that enables content recording when set to "true" or "1".
     */
    static final String ENV_CONTENT_RECORDING = "OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT";

    /**
     * Environment variable that controls trace context propagation. Default is ON;
     * set to "false" or "0" to disable.
     */
    static final String ENV_TRACE_CONTEXT_PROPAGATION = "AZURE_TRACING_GEN_AI_ENABLE_TRACE_CONTEXT_PROPAGATION";

    private static final AtomicReference<ConfigState> STATE = new AtomicReference<>(ConfigState.DEFAULTS);

    private GenAiTracingConfiguration() {
        // utility class
    }

    /**
     * Enables GenAI tracing with the specified options.
     * <p>
     * Each call resets ALL options to the specified values (or environment variable / default if not provided).
     * </p>
     *
     * @param options the tracing options to apply; if {@code null}, environment variables and defaults are used.
     */
    public static void enableGenAiTracing(GenAiTracingOptions options) {
        GenAiTracingOptions effective = options != null ? options : new GenAiTracingOptions();
        boolean contentRecording = resolveBoolean(effective.isContentRecording(), ENV_CONTENT_RECORDING, false);
        boolean propagation
            = resolveBoolean(effective.isTraceContextPropagation(), ENV_TRACE_CONTEXT_PROPAGATION, true);

        STATE.set(new ConfigState(true, contentRecording, propagation));
        LOGGER.atVerbose()
            .log("GenAI tracing enabled: contentRecording={}, propagation={}", contentRecording, propagation);
    }

    /**
     * Disables GenAI tracing and resets all flags to defaults.
     */
    public static void disableGenAiTracing() {
        STATE.set(ConfigState.DEFAULTS);
        LOGGER.atVerbose().log("GenAI tracing disabled");
    }

    /**
     * Returns whether GenAI tracing is currently enabled and applied.
     *
     * @return {@code true} if tracing is enabled.
     */
    public static boolean isTracingEnabled() {
        return STATE.get().enabled;
    }

    /**
     * Returns whether content recording is enabled (messages include full text).
     *
     * @return {@code true} if content recording is on.
     */
    public static boolean isContentRecordingEnabled() {
        return STATE.get().contentRecording;
    }

    /**
     * Returns whether trace context propagation (W3C traceparent/tracestate headers) is enabled.
     *
     * @return {@code true} if trace context propagation is on.
     */
    public static boolean isTraceContextPropagationEnabled() {
        return STATE.get().traceContextPropagation;
    }

    private static boolean resolveBoolean(Boolean programmatic, String envVar, boolean defaultValue) {
        if (programmatic != null) {
            return programmatic;
        }
        String envValue = Configuration.getGlobalConfiguration().get(envVar);
        if (envValue != null) {
            String normalized = envValue.trim().toLowerCase();
            return "true".equals(normalized) || "1".equals(normalized);
        }
        return defaultValue;
    }

    /**
     * Internal state holder.
     */
    private static final class ConfigState {
        static final ConfigState DEFAULTS = new ConfigState(false, false, true);

        final boolean enabled;
        final boolean contentRecording;
        final boolean traceContextPropagation;

        ConfigState(boolean enabled, boolean contentRecording, boolean traceContextPropagation) {
            this.enabled = enabled;
            this.contentRecording = contentRecording;
            this.traceContextPropagation = traceContextPropagation;
        }
    }
}
