// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The SendDtmfFailed model. */
@Fluent
public final class SendDtmfFailed extends CallAutomationEventBase {

    /*
     * Contains the resulting SIP code, sub-code and message.
     */
    @JsonProperty(value = "resultInformation")
    private final ResultInformation resultInformation;

    /**
     * Constructor for ContinuousDtmfRecognitionToneReceived
     */
    public SendDtmfFailed() {
        resultInformation = null;
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
