// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

/**
 * Class representing the source of the data/content.
 */
public abstract class ContentSource {
    private final ContentSourceKind kind;

    /**
     * Constructs a ContentSource object.
     * @param kind the source kind of the training data.
     */
    ContentSource(ContentSourceKind kind) {
        this.kind = kind;
    }

    /**
     * Get the source kind of the training data.
     * @return the sourceKind value.
     */
    public ContentSourceKind getKind() {
        return kind;
    }
}
