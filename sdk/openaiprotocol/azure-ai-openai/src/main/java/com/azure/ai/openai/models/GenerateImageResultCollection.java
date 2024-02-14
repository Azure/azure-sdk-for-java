package com.azure.ai.openai.models;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The GenerateImageResultCollection model.
 */
public final class GenerateImageResultCollection {
    private OffsetDateTime createdAt;
    private List<GenerateImageResult> results;

    public GenerateImageResultCollection(OffsetDateTime createdAt, List<GenerateImageResult> results) {
        this.createdAt = createdAt;
        this.results = results;
    }

    /**
     * @return
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @return
     */
    public List<GenerateImageResult> getResults() {
        return results;
    }
}
