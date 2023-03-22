// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.azure.core.annotation.Fluent;

/** The continuous speech recognition result. */
@Fluent
public final class SpeechResult extends RecognizeResult {
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

    /**
     * Set the speech property: The recognized speech in string.
     *
     * @param speech the speech value to set.
     * @return the SpeechResult object itself.
     */
    public SpeechResult setSpeech(String speech) {
        this.speech = speech;
        return this;
    }
}
