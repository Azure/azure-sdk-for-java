// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.DocumentClassificationPropertiesHelper;
import com.azure.core.annotation.Immutable;

/**
 * The document classification result which contains the classified category and the confidence score on it.
 */
@Immutable
public final class ClassificationCategory {
    private String category;
    private double confidenceScore;

    static {
        DocumentClassificationPropertiesHelper.setAccessor(
            new DocumentClassificationPropertiesHelper.DocumentClassificationAccessor() {
                @Override
                public void setCategory(ClassificationCategory classificationCategory, String category) {
                    classificationCategory.setCategory(category);
                }

                @Override
                public void setConfidenceScore(ClassificationCategory classificationCategory, double confidenceScore) {
                    classificationCategory.setConfidenceScore(confidenceScore);
                }
            });
    }

    /**
     * Gets the classified category of document.
     *
     * @return The {@link String}.
     */
    public String getCategory() {
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

    private void setCategory(String category) {
        this.category = category;
    }

    private void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}
