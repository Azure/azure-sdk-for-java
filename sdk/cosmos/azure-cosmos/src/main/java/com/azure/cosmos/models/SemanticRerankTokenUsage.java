// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents token usage information for semantic rerank operations.
 */
@Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class SemanticRerankTokenUsage {
    @JsonProperty("total_tokens")
    private int totalTokens;

    /**
     * Creates a new instance of SemanticRerankTokenUsage.
     */
    public SemanticRerankTokenUsage() {
    }

    /**
     * Gets the total number of tokens used in the operation.
     *
     * @return the total token count.
     */
    public int getTotalTokens() {
        return totalTokens;
    }

    /**
     * Sets the total number of tokens used in the operation.
     *
     * @param totalTokens the total token count.
     */
    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }
}
