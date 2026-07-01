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
        boolean experimentalAcknowledged = resolveBoolean(effective.isExperimental(), ENV_ENABLE_GENAI_TRACING, false);
        boolean contentRecording = resolveBoolean(effective.isContentRecording(), ENV_CONTENT_RECORDING, false);

        STATE.set(new ConfigState(true, experimentalAcknowledged, contentRecording));
        LOGGER.atVerbose()
            .log("GenAI tracing enabled: experimental={}, contentRecording={}", experimentalAcknowledged,
                contentRecording);
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
     * Tracing is only applied when both enabled and the experimental flag is acknowledged.
     *
     * @return {@code true} if tracing is enabled and experimental is acknowledged.
     */
    public static boolean isTracingEnabled() {
        ConfigState state = STATE.get();
        return state.enabled && state.experimentalAcknowledged;
    }

    /**
     * Returns whether content recording is enabled (messages include full text).
     *
     * @return {@code true} if content recording is on.
     */
    public static boolean isContentRecordingEnabled() {
        return STATE.get().contentRecording;
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
        static final ConfigState DEFAULTS = new ConfigState(false, false, false);

        final boolean enabled;
        final boolean experimentalAcknowledged;
        final boolean contentRecording;

        ConfigState(boolean enabled, boolean experimentalAcknowledged, boolean contentRecording) {
            this.enabled = enabled;
            this.experimentalAcknowledged = experimentalAcknowledged;
            this.contentRecording = contentRecording;
        }
    }
}
