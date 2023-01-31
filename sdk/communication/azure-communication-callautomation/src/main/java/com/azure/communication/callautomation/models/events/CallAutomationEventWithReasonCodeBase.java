// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The base event interface with ReasonCode added.
 */
public abstract class CallAutomationEventWithReasonCodeBase extends CallAutomationEventBase {
    /*
     * Contains the resulting SIP code/sub-code and message from NGC services.
     */
    @JsonProperty(value = "resultInformation")
    private final ResultInformation resultInformation;

    CallAutomationEventWithReasonCodeBase() {
        this.resultInformation = null;
    }

    /**
     * Get the resultInformation property: Contains the resulting SIP code/sub-code and message from NGC services.
     *
     * @return the resultInformation value.
     */
    public ResultInformation getResultInformation() {
        return this.resultInformation;
    }

    /**
     * Returns the reason code of the event
     * @return a ReasonCode object.
     * */
    public ReasonCode getReasonCode() {
        return getResultInformation() != null ? ReasonCode.fromReasonCode(getResultInformation().getSubCode()) : null;
    }
}
