// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.ToneInfo;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The ContinuousDtmfRecognitionToneReceived model. */
@Immutable
public final class ContinuousDtmfRecognitionToneReceived extends CallAutomationEventBase {

    /*
     * Information about Tone.
     */
    @JsonProperty(value = "toneInfo", access = JsonProperty.Access.WRITE_ONLY)
    private ToneInfo toneInfo;

    /*
     * Contains the resulting SIP code, sub-code and message.
     */
    @JsonProperty(value = "resultInformation")
    private final ResultInformation resultInformation;

    /**
     * Constructor for ContinuousDtmfRecognitionToneReceived
     */
    public ContinuousDtmfRecognitionToneReceived() {
        toneInfo = null;
        resultInformation = null;
    }

    /**
     * getter for toneInfo property
     * @return return toneInfo value
     */
    public ToneInfo getToneInfo() {
        return toneInfo;
    }

    /**
     * Get the resultInformation property: Contains the resulting SIP code, sub-code and message.
     *
     * @return the resultInformation value.
     */
    public ResultInformation getResultInformation() {
        return this.resultInformation;
    }
}
