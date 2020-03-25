// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 *
 */
@Immutable
public class ReceiptType {
    /*
     *
     */
    private final String type;
    /*
     *
     */
    private final Float confidence;

    /**
     *
     * @param type
     * @param confidence
     */
    public ReceiptType(final String type, final Float confidence) {
        this.type = type;
        this.confidence = confidence;
    }

    /**
     *
     * @return
     */
    public String getType() {
        return this.type;
    }

    /**
     *
     * @return
     */
    public Float getConfidence() {
        return this.confidence;
    }
}
