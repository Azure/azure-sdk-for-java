// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.models;

import com.azure.ai.projects.implementation.utils.Beta;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.Objects;

/**
 * Trace inputs for a scheduled evaluation. Use the typed factories to select a source.
 * Preview sources may appear in responses for existing schedules but are rejected on create and update.
 */
@Immutable
public final class TraceEvaluationSource implements JsonSerializable<TraceEvaluationSource> {
    private static final ClientLogger LOGGER = new ClientLogger(TraceEvaluationSource.class);

    private final JsonSerializable<?> value;

    private TraceEvaluationSource(JsonSerializable<?> value) {
        this.value = Objects.requireNonNull(value, "'value' cannot be null.");
    }

    /**
     * Creates a source containing trace-query inputs.
     * @param source the trace-query source.
     * @return the trace evaluation source.
     * @throws NullPointerException if {@code source} is null.
     */
    public static TraceEvaluationSource ofTraces(TracesEvaluationRunDataSource source) {
        return new TraceEvaluationSource(source);
    }

    /**
     * Creates a source containing an existing trace-source selection.
     * @param source the trace-source selection.
     * @return the trace evaluation source.
     * @throws NullPointerException if {@code source} is null.
     */
    public static TraceEvaluationSource ofTraceSource(TraceSourceEvaluationRunDataSource source) {
        return new TraceEvaluationSource(source);
    }

    /**
     * Creates a response-only source containing preview trace-query inputs.
     * @param source the preview trace-query source.
     * @return the trace evaluation source.
     * @throws NullPointerException if {@code source} is null.
     */
    @Beta(warningText = "Preview API. Evaluations=V1Preview")
    public static TraceEvaluationSource ofTracesPreview(TracesPreviewEvaluationRunDataSource source) {
        return new TraceEvaluationSource(source);
    }

    /**
     * Creates a response-only source containing a preview trace-source selection.
     * @param source the preview trace-source selection.
     * @return the trace evaluation source.
     * @throws NullPointerException if {@code source} is null.
     */
    @Beta(warningText = "Preview API. Evaluations=V1Preview")
    public static TraceEvaluationSource ofTraceSourcePreview(TraceSourcePreviewEvaluationRunDataSource source) {
        return new TraceEvaluationSource(source);
    }

    /**
     * Gets the trace-query variant.
     * @return the trace-query source, or null if this source contains another variant.
     */
    public TracesEvaluationRunDataSource getTraces() {
        return value instanceof TracesEvaluationRunDataSource ? (TracesEvaluationRunDataSource) value : null;
    }

    /**
     * Gets the trace-source selection variant.
     * @return the trace-source selection, or null if this source contains another variant.
     */
    public TraceSourceEvaluationRunDataSource getTraceSource() {
        return value instanceof TraceSourceEvaluationRunDataSource ? (TraceSourceEvaluationRunDataSource) value : null;
    }

    /**
     * Gets the response-only preview trace-query variant.
     * @return the preview trace-query source, or null if this source contains another variant.
     */
    @Beta(warningText = "Preview API. Evaluations=V1Preview")
    public TracesPreviewEvaluationRunDataSource getTracesPreview() {
        return value instanceof TracesPreviewEvaluationRunDataSource
            ? (TracesPreviewEvaluationRunDataSource) value
            : null;
    }

    /**
     * Gets the response-only preview trace-source selection variant.
     * @return the preview trace-source selection, or null if this source contains another variant.
     */
    @Beta(warningText = "Preview API. Evaluations=V1Preview")
    public TraceSourcePreviewEvaluationRunDataSource getTraceSourcePreview() {
        return value instanceof TraceSourcePreviewEvaluationRunDataSource
            ? (TraceSourcePreviewEvaluationRunDataSource) value
            : null;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return value.toJson(jsonWriter);
    }

    /**
     * Reads a trace evaluation source using the {@code type} discriminator.
     * @param jsonReader the JSON reader.
     * @return the source, or null for JSON null.
     * @throws IOException if the JSON cannot be read.
     * @throws IllegalStateException if the source type is missing or unsupported.
     */
    public static TraceEvaluationSource fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            try (JsonReader bufferedReader = reader.bufferObject()) {
                String type = null;
                bufferedReader.nextToken();
                while (bufferedReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = bufferedReader.getFieldName();
                    bufferedReader.nextToken();
                    if ("type".equals(fieldName)) {
                        type = bufferedReader.getString();
                        break;
                    }
                    bufferedReader.skipChildren();
                }
                if ("azure_ai_traces".equals(type)) {
                    return ofTraces(TracesEvaluationRunDataSource.fromJson(bufferedReader.reset()));
                } else if ("azure_ai_trace_data_source".equals(type)) {
                    return ofTraceSource(TraceSourceEvaluationRunDataSource.fromJson(bufferedReader.reset()));
                } else if ("azure_ai_traces_preview".equals(type)) {
                    return ofTracesPreview(TracesPreviewEvaluationRunDataSource.fromJson(bufferedReader.reset()));
                } else if ("azure_ai_trace_data_source_preview".equals(type)) {
                    return ofTraceSourcePreview(
                        TraceSourcePreviewEvaluationRunDataSource.fromJson(bufferedReader.reset()));
                }
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Unsupported trace evaluation source type: " + type));
            }
        });
    }
}
