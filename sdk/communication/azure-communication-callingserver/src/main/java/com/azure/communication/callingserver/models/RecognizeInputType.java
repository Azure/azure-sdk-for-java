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
     * Creates a new instance of {@link RecognizeInputType} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link RecognizeInputType} which doesn't
     * have a String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
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
     * Gets known RecognizeInputType values.
     *
     * @return known RecognizeInputType values.
     */
    public static Collection<RecognizeInputType> values() {
        return values(RecognizeInputType.class);
    }
}
