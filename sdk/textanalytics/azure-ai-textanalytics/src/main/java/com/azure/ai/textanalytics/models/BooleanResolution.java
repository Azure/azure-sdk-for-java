// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/** A resolution for boolean expressions. */
public final class BooleanResolution extends BaseResolution {
    /*
     * The value property.
     */
    private final boolean value;

    /**
     * Create a resolution for boolean expressions.
     *
     * @param value The value property.
     */
    public BooleanResolution(boolean value) {
        this.value = value;
    }

    /**
     * Get the value property: The value property.
     *
     * @return the value value.
     */
    public boolean isValue() {
        return this.value;
    }
}
