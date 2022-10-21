// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/** A resolution for ordinal numbers entity instances. */
@Immutable
public final class OrdinalResolution extends BaseResolution {
    /*
     * The offset With respect to the reference (e.g., offset = -1 in "show me the second to last"
     */
    private final String offset;

    /*
     * The reference point that the ordinal number denotes.
     */
    private final RelativeTo relativeTo;

    /*
     * A simple arithmetic expression that the ordinal denotes.
     */
    private final String value;

    /**
     * Create a resolution for ordinal numbers entity instances.
     *
     * @param offset The offset With respect to the reference (e.g., offset = -1 in "show me the second to last".
     * @param relativeTo The reference point that the ordinal number denotes.
     * @param value A simple arithmetic expression that the ordinal denotes.
     */
    public OrdinalResolution(String offset, RelativeTo relativeTo, String value) {
        this.offset = offset;
        this.relativeTo = relativeTo;
        this.value = value;
    }

    /**
     * Get the offset property: The offset With respect to the reference (e.g., offset = -1 in "show me the second to
     * last".
     *
     * @return the offset value.
     */
    public String getOffset() {
        return this.offset;
    }

    /**
     * Get the relativeTo property: The reference point that the ordinal number denotes.
     *
     * @return the relativeTo value.
     */
    public RelativeTo getRelativeTo() {
        return this.relativeTo;
    }

    /**
     * Get the value property: A simple arithmetic expression that the ordinal denotes.
     *
     * @return the value value.
     */
    public String getValue() {
        return this.value;
    }
}
