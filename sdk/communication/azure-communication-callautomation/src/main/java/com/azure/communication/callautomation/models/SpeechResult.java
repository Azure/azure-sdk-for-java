// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The continuous speech recognition result. */
@Fluent
@Immutable
public final class SpeechResult extends RecognizeResult {

    private SpeechResult() {
    }

    /*
     * The recognized speech in string.
     */
    @JsonProperty(value = "speech")
    private String speech;

    /**
     * Get the speech property: The recognized speech in string.
     *
     * @return the speech value.
     */
    public String getSpeech() {
        return this.speech;
    }
}
