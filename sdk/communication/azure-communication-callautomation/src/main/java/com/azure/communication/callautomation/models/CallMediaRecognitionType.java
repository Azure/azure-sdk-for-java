// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for RecognitionType. */
public final class CallMediaRecognitionType extends ExpandableStringEnum<CallMediaRecognitionType> {
    /** Static value dtmf for RecognitionType. */
    public static final CallMediaRecognitionType DTMF = fromString("dtmf");
    /** Static value choices for RecognitionTypeInternal. */
    public static final CallMediaRecognitionType CHOICES = fromString("choices");
    /** Static value continuous speech for RecognitionType. */
    public static final CallMediaRecognitionType SPEECH = fromString("speech");

    /**
     * Creates or finds a RecognitionType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding RecognitionType.
     */
    @JsonCreator
    public static CallMediaRecognitionType fromString(String name) {
        return fromString(name, CallMediaRecognitionType.class);
    }

    /** @return known RecognitionType values. */
    public static Collection<CallMediaRecognitionType> values() {
        return values(CallMediaRecognitionType.class);
    }
}
