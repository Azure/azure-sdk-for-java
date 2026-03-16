// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Represents the result of a semantic rerank operation.
 */
@Beta(value = Beta.SinceVersion.V4_78_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class SemanticRerankResult {
    @JsonProperty("Scores")
    private List<SemanticRerankScore> scores;

    @JsonProperty("latency")
    private Map<String, Object> latency;

    @JsonProperty("token_usage")
    private Map<String, Object> tokenUsage;

    /**
     * Creates a new instance of SemanticRerankResult.
     */
    public SemanticRerankResult() {
    }

    /**
     * Gets the list of scored documents.
     *
     * @return the list of scored documents.
     */
    public List<SemanticRerankScore> getScores() {
        return scores;
    }

    /**
     * Sets the list of scored documents.
     *
     * @param scores the list of scored documents.
     */
    void setScores(List<SemanticRerankScore> scores) {
        this.scores = scores;
    }

    /**
     * Gets the latency information for the operation as a map of metric names to values.
     *
     * @return the latency information map.
     */
    public Map<String, Object> getLatency() {
        return latency;
    }

    /**
     * Sets the latency information for the operation.
     *
     * @param latency the latency information map.
     */
    void setLatency(Map<String, Object> latency) {
        this.latency = latency;
    }

    /**
     * Gets the token usage information for the operation as a map of metric names to values.
     *
     * @return the token usage information map.
     */
    public Map<String, Object> getTokenUsage() {
        return tokenUsage;
    }

    /**
     * Sets the token usage information for the operation.
     *
     * @param tokenUsage the token usage information map.
     */
    void setTokenUsage(Map<String, Object> tokenUsage) {
        this.tokenUsage = tokenUsage;
    }
}
