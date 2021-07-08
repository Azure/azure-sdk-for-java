// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

@Immutable
public final class DocumentClassification {
    private ClassificationCategory category;
    private double confidenceScore;

    public ClassificationCategory getCategory() {
        return category;
    }

    /**
     * Gets the score property: Confidence score between 0 and 1 of the recognized entity.
     *
     * @return The {@code confidenceScore} value.
     */
    public double getConfidenceScore() {
        return this.confidenceScore;
    }
}
