// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

public class ReceiptType {
    private final String type;
    private final Float confidence;

    public ReceiptType(final String type, final Float confidence) {
        this.type = type;
        this.confidence = confidence;
    }

    public String getType() {
        return this.type;
    }

    public Float getConfidence() {
        return this.confidence;
    }
}
