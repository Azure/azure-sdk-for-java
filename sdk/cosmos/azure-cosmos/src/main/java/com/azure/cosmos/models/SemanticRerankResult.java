// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.util.Beta;

import java.util.List;
import java.util.Map;

/**
 * Represents the result of a semantic rerank operation.
 */
@Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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

    private void setScores(List<SemanticRerankScore> scores) {
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

    private void setLatency(Map<String, Object> latency) {
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

    private void setTokenUsage(Map<String, Object> tokenUsage) {
        this.tokenUsage = tokenUsage;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.SemanticRerankResultHelper.setSemanticRerankResultAccessor(
            new ImplementationBridgeHelpers.SemanticRerankResultHelper.SemanticRerankResultAccessor() {
                @Override
                public void setScores(SemanticRerankResult result, List<SemanticRerankScore> scores) {
                    result.setScores(scores);
                }

                @Override
                public void setLatency(SemanticRerankResult result, Map<String, Object> latency) {
                    result.setLatency(latency);
                }

                @Override
                public void setTokenUsage(SemanticRerankResult result, Map<String, Object> tokenUsage) {
                    result.setTokenUsage(tokenUsage);
                }
            }
        );
    }

    static { initialize(); }
}
