// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The PlayFailed model. */
@Immutable
public final class PlayFailed extends CallAutomationEventBaseWithReasonCode {
    /*
     * Contains the resulting SIP code, sub-code and message.
     */
    @JsonProperty(value = "resultInformation")
    private final ResultInformation resultInformation;

     /*
     * Contains the index of the failed play source.
     */
    @JsonProperty(value = "failedPlaySourceIndex")
    private Integer failedPlaySourceIndex;

    private PlayFailed() {
        this.resultInformation = null;
    }

    /**
     * Get the resultInformation property: Contains the resulting SIP code, sub-code and message.
     *
     * @return the resultInformation value.
     */
    public ResultInformation getResultInformation() {
        return this.resultInformation;
    }

    /**
     * Get the failedPlaySourceIndex property: Contains the index of the failed play source.
     * 
     * @return the failedPlaySourceIndex value.
     */
    public Integer getFailedPlaySourceIndex() {
        return this.failedPlaySourceIndex;
    }
}
