// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The ContinuousDtmfRecognitionToneReceived model. */
@Immutable
public final class ContinuousDtmfRecognitionToneReceived extends CallAutomationEventBase {

    /*
     * The sequence id which can be used to determine if the same tone was played multiple times or if any tones were missed.
     */
    @JsonProperty(value = "sequenceId", required = true)
    private final Integer sequenceId;

    /*
     * The tone property.
     */
    @JsonProperty(value = "tone", required = true)
    private final DtmfTone tone;

    /*
     * Contains the resulting SIP code, sub-code and message.
     */
    @JsonProperty(value = "resultInformation")
    private final ResultInformation resultInformation;

    /**
     * Constructor for ContinuousDtmfRecognitionToneReceived
     */
    private ContinuousDtmfRecognitionToneReceived() {
        resultInformation = null;
        sequenceId = 0;
        tone = null;
    }

    /**
     * Get sequenceId: The sequence id which can be used to determine if the same tone was played multiple
     * times or if any tones were missed.
     *
     * @return the sequenceId value.
     */
    public int getSequenceId() {
        return this.sequenceId;
    }

    /**
     * Get the tone property:
     *
     * @return the tone value.
     */
    public DtmfTone  getTone() {
        return this.tone;
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
