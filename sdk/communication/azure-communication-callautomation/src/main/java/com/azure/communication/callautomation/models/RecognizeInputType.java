// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for RecognizeInputTypeInternal. */
public final class RecognizeInputType extends ExpandableStringEnum<RecognizeInputType> {
    /** Static value dtmf for RecognizeInputTypeInternal. */
    public static final RecognizeInputType DTMF = fromString("dtmf");

    /** Static value choices for RecognizeInputTypeInternal. */
    public static final RecognizeInputType CHOICES = fromString("choices");

    /** Static value choices for RecognizeInputTypeInternal. */
    public static final RecognizeInputType SPEECH = fromString("speech");

    /** Static value choices for RecognizeInputTypeInternal. */
    public static final RecognizeInputType SPEECH_OR_DTMF = fromString("speechordtmf");

    /**
     * Creates or finds a RecognizeInputTypeInternal from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RecognizeInputTypeInternal.
     */
    @JsonCreator
    public static RecognizeInputType fromString(String name) {
        return fromString(name, RecognizeInputType.class);
    }

    /** @return known RecognizeInputTypeInternal values. */
    public static Collection<RecognizeInputType> values() {
        return values(RecognizeInputType.class);
    }
}
