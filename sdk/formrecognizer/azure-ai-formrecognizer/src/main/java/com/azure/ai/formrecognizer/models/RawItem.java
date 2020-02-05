// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

public class RawItem {
    private final BoundingBox boundingBox;
    private final String text;

    RawItem(final BoundingBox boundingBox, final String text) {
        this.boundingBox = boundingBox;
        this.text = text;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public String getText() {
        return text;
    }
}
