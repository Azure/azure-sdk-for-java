// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

public class ExtractedWord extends RawItem{
    private final Float confidence;

    public ExtractedWord (BoundingBox boundingBox, String text, Float confidence) {
        super (boundingBox, text);
        this.confidence = confidence;
    }

    public Float getConfidence() {
        return confidence;
    }

}
