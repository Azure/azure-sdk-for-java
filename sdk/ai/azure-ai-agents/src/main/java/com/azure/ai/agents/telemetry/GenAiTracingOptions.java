// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

/**
 * Options for configuring GenAI tracing behavior.
 *
 * <p>All fields are optional. If not specified, the corresponding environment variable
 * (or default) is used.</p>
 */
public final class GenAiTracingOptions {

    private Boolean contentRecording;
    private Boolean traceContextPropagation;

    /**
     * Creates a new instance with all options unset (will resolve from environment or defaults).
     */
    public GenAiTracingOptions() {
    }

    /**
     * Gets the content recording setting.
     *
     * @return the content recording setting, or {@code null} if not set.
     */
    public Boolean isContentRecording() {
        return contentRecording;
    }

    /**
     * Sets whether message content should be recorded in traces.
     * When enabled, full prompt/response text is captured.
     * When disabled (default), only message structure (roles, types) is captured.
     *
     * @param contentRecording {@code true} to enable content recording.
     * @return this options instance.
     */
    public GenAiTracingOptions setContentRecording(Boolean contentRecording) {
        this.contentRecording = contentRecording;
        return this;
    }

    /**
     * Gets the trace context propagation setting.
     *
     * @return the trace context propagation setting, or {@code null} if not set.
     */
    public Boolean isTraceContextPropagation() {
        return traceContextPropagation;
    }

    /**
     * Sets whether W3C trace context (traceparent/tracestate) headers should be injected
     * into outgoing HTTP requests to the AI service. Default is {@code true}.
     *
     * @param traceContextPropagation {@code false} to disable propagation.
     * @return this options instance.
     */
    public GenAiTracingOptions setTraceContextPropagation(Boolean traceContextPropagation) {
        this.traceContextPropagation = traceContextPropagation;
        return this;
    }
}
