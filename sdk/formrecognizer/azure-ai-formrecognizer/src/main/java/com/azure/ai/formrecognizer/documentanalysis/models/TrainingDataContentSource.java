// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

/**
 * Class representing the source of the training content.
 */
public abstract class TrainingDataContentSource {
    private final ContentSourceKind sourceKind;

    /**
     * Constructs a TrainingDataContentSource object.
     * @param sourceKind the source kind of the training data.
     */
    private TrainingDataContentSource(ContentSourceKind sourceKind) {
        this.sourceKind = sourceKind;
    }

    /**
     * Get the source kind of the training data.
     * @return the sourceKind value.
     */
    public ContentSourceKind getSourceKind() {
        return sourceKind;
    }
}
