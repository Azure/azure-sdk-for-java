// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/** A resolution for numeric entity instances. */
public final class NumberResolution extends BaseResolution {
    /*
     * The type of the extracted number entity.
     */
    private final NumberKind numberKind;

    /*
     * A numeric representation of what the extracted text denotes.
     */
    private final String value;

    /**
     * Create a resolution for numeric entity instances.
     *
     * @param numberKind The type of the extracted number entity.
     * @param value A numeric representation of what the extracted text denotes.
     */
    public NumberResolution(NumberKind numberKind, String value) {
        this.numberKind = numberKind;
        this.value = value;
    }

    /**
     * Get the numberKind property: The type of the extracted number entity.
     *
     * @return the numberKind value.
     */
    public NumberKind getNumberKind() {
        return this.numberKind;
    }

    /**
     * Get the value property: A numeric representation of what the extracted text denotes.
     *
     * @return the value value.
     */
    public String getValue() {
        return this.value;
    }
}
