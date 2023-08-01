// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

/**
 * Class representing the source of the training content.
 */
public abstract class TrainingDataContentSource {
    private final TrainingDataContentSourceKind kind;

    /**
     * Constructs a TrainingDataContentSource object.
     * @param kind the source kind of the training data.
     */
    TrainingDataContentSource(TrainingDataContentSourceKind kind) {
        this.kind = kind;
    }

    /**
     * Get the source kind of the training data.
     * @return the sourceKind value.
     */
    public TrainingDataContentSourceKind getKind() {
        return kind;
    }
}
