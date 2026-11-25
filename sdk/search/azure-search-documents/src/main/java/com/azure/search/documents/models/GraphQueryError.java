// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.models.ResponseError;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/**
 * A graph-query error body with standard error details and optional graph diagnostics.
 * Deserialize the response body retained by {@link HttpResponseException} into this model.
 * Graph client methods continue to throw their existing exceptions.
 */
@Immutable
public final class GraphQueryError implements JsonSerializable<GraphQueryError> {
    private final ResponseError error;
    private final GraphQueryResult result;
    private final String json;

    private GraphQueryError(ResponseError error, GraphQueryResult result, String json) {
        this.error = error;
        this.result = result;
        this.json = json;
    }

    /**
     * Gets the standard service error.
     *
     * @return the service error details.
     */
    public ResponseError getError() {
        return error;
    }

    /**
     * Gets the number of projected rows, when returned with the error.
     *
     * @return the row count, or null when omitted.
     */
    public Long getCount() {
        return result.getCount();
    }

    /**
     * Gets projection rows returned with the error.
     *
     * @return the projection rows, or null when omitted.
     */
    public List<GraphQueryRow> getValue() {
        return result.getValue();
    }

    /**
     * Gets execution statistics returned with the error.
     *
     * @return the statistics, or null when omitted.
     */
    public GraphQueryStatistics getStatistics() {
        return result.getStatistics();
    }

    /**
     * Gets structured graph diagnostics, including specific graph rejection codes.
     *
     * @return the diagnostics, or null when omitted.
     */
    public List<GraphQueryDiagnostic> getDiagnostics() {
        return result.getDiagnostics();
    }

    /**
     * Gets requested explanation stages returned with the error.
     *
     * @return the explanation, or null when omitted.
     */
    public GraphQueryExplain getExplain() {
        return result.getExplain();
    }

    /**
     * Gets hydrated source documents returned with the error.
     *
     * @return the source documents, or null when omitted.
     */
    public GraphQueryProvenanceDocuments getProvenanceDocuments() {
        return result.getProvenanceDocuments();
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeRawValue(json);
    }

    /**
     * Reads a graph-query error body. Optional graph fields may be absent.
     *
     * @param jsonReader the reader positioned at the error body.
     * @return the error model, or null for JSON null.
     * @throws IOException if the body cannot be read.
     * @throws IllegalStateException if required standard error properties are missing.
     */
    public static GraphQueryError fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String json = reader.readChildren();
            try (JsonReader errorReader = JsonProviders.createReader(json);
                JsonReader resultReader = JsonProviders.createReader(json)) {
                return new GraphQueryError(ResponseError.fromJson(errorReader), GraphQueryResult.fromJson(resultReader),
                    json);
            }
        });
    }
}
