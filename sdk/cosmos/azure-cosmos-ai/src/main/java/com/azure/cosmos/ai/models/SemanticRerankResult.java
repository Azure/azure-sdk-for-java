// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.ai.models;

import java.util.List;
import java.util.Map;

/**
 * Represents the result of a semantic rerank operation.
 */
public final class SemanticRerankResult {
    private List<SemanticRerankScore> scores;
    private Map<String, Object> latency;
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
     * Gets the latency information for the operation as a map of metric names to values.
     *
     * @return the latency information map.
     */
    public Map<String, Object> getLatency() {
        return latency;
    }

    /**
     * Gets the token usage information for the operation as a map of metric names to values.
     *
     * @return the token usage information map.
     */
    public Map<String, Object> getTokenUsage() {
        return tokenUsage;
    }

    // Package-private setters used by InferenceResponseParser in the same package
    void setScores(List<SemanticRerankScore> scores) {
        this.scores = scores;
    }

    void setLatency(Map<String, Object> latency) {
        this.latency = latency;
    }

    void setTokenUsage(Map<String, Object> tokenUsage) {
        this.tokenUsage = tokenUsage;
    }
}
