// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The ContinuousDtmfRecognitionToneReceived model. */
@Fluent
public final class ContinuousDtmfRecognitionToneReceived extends CallAutomationEventWithReasonCodeBase {

    /*
     * Information about Tone.
     */
    @JsonProperty(value = "toneInfo", access = JsonProperty.Access.WRITE_ONLY)
    private ToneInfo toneInfo;

    /**
     * Constructor for ContinuousDtmfRecognitionToneReceived
     */
    public ContinuousDtmfRecognitionToneReceived() {
        toneInfo = null;
    }

    /**
     * getter for toneInfo property
     * @return return toneInfo value
     */
    public ToneInfo getToneInfo() {
        return toneInfo;
    }
}
