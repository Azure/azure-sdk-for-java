// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for RecognitionType. */
public final class RecognitionType extends ExpandableStringEnum<RecognitionType> {
    /** Static value dtmf for RecognitionType. */
    public static final RecognitionType DTMF = fromString("dtmf");

    /**
     * Creates or finds a RecognitionType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RecognitionType.
     */
    @JsonCreator
    public static RecognitionType fromString(String name) {
        return fromString(name, RecognitionType.class);
    }

    /** @return known RecognitionType values. */
    public static Collection<RecognitionType> values() {
        return values(RecognitionType.class);
    }
}
