// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The USReceiptType model.
 */
@Immutable
public final class USReceiptType {

    /*
     * The type of the receipt.
     */
    private final String type;

    /*
     * Confidence score.
     */
    private final float confidence;

    /**
     * Constructs a Receipt Type.
     *  @param type The type of the receipt.
     * @param confidence The confidence score.
     */
    public USReceiptType(final String type, final float confidence) {
        this.type = type;
        this.confidence = confidence;
    }

    /**
     * Gets the type of the receipt.
     *
     * @return The type of the receipt.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Gets the confidence score of the detected type of the receipt.
     *
     * @return The confidence score of the detected type of the receipt.
     */
    public float getConfidence() {
        return this.confidence;
    }
}
