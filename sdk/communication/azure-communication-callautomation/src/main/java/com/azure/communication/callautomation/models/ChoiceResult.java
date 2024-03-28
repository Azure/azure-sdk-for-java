// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The ChoiceResult model. */
@Fluent
@Immutable
public final class ChoiceResult extends RecognizeResult {

    private ChoiceResult() {
    }

    /*
     * Label is the primary identifier for the choice detected
     */
    @JsonProperty(value = "label")
    private String label;

    /*
     * Phrases are set to the value if choice is selected via phrase detection.
     * If Dtmf input is recognized, then Label will be the identifier for the
     * choice detected and phrases will be set to null
     */
    @JsonProperty(value = "recognizedPhrase")
    private String recognizedPhrase;

    /**
     * Get the label property: Label is the primary identifier for the choice detected.
     *
     * @return the label value.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Get the recognizedPhrase property: Phrases are set to the value if choice is selected via phrase detection. If
     * Dtmf input is recognized, then Label will be the identifier for the choice detected and phrases will be set to
     * null.
     *
     * @return the recognizedPhrase value.
     */
    public String getRecognizedPhrase() {
        return this.recognizedPhrase;
    }
}
