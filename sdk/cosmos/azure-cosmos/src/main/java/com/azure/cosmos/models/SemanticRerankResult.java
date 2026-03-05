// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the result of a semantic rerank operation.
 */
@Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class SemanticRerankResult {
    @JsonProperty("Scores")
    private List<SemanticRerankScore> scores;

    @JsonProperty("latency")
    private SemanticRerankLatency latency;

    @JsonProperty("token_usage")
    private SemanticRerankTokenUsage tokenUsage;

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
    public void setScores(List<SemanticRerankScore> scores) {
        this.scores = scores;
    }

    /**
     * Gets the latency information for the operation.
     *
     * @return the latency information.
     */
    public SemanticRerankLatency getLatency() {
        return latency;
    }

    /**
     * Sets the latency information for the operation.
     *
     * @param latency the latency information.
     */
    public void setLatency(SemanticRerankLatency latency) {
        this.latency = latency;
    }

    /**
     * Gets the token usage information for the operation.
     *
     * @return the token usage information.
     */
    public SemanticRerankTokenUsage getTokenUsage() {
        return tokenUsage;
    }

    /**
     * Sets the token usage information for the operation.
     *
     * @param tokenUsage the token usage information.
     */
    public void setTokenUsage(SemanticRerankTokenUsage tokenUsage) {
        this.tokenUsage = tokenUsage;
    }
}
