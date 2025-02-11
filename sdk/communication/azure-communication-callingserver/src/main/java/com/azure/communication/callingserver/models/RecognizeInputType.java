// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for RecognizeInputTypeInternal. */
public final class RecognizeInputType extends ExpandableStringEnum<RecognizeInputType> {
    /** Static value dtmf for RecognizeInputTypeInternal. */
    public static final RecognizeInputType DTMF = fromString("dtmf");

    /**
     * Creates an instance of {@link RecognizeInputType} with no string value.
     *
     * @deprecated Use {@link #fromString(String)} to create or get an instance of {@link RecognizeInputType} instead.
     */
    @Deprecated
    public RecognizeInputType() {
    }

    /**
     * Creates or finds a RecognizeInputTypeInternal from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RecognizeInputTypeInternal.
     */
    public static RecognizeInputType fromString(String name) {
        return fromString(name, RecognizeInputType.class);
    }

    /**
     * Get the collection of RecognizeInputTypeInternal values.
     * @return known RecognizeInputTypeInternal values.
     */
    public static Collection<RecognizeInputType> values() {
        return values(RecognizeInputType.class);
    }
}
