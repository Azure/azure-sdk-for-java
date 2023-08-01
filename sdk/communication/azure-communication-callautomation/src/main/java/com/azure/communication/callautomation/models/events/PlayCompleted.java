// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The PlayCompleted model. */
@Immutable
public final class PlayCompleted extends CallAutomationEventBaseWithReasonCode {
    /*
     * Contains the resulting SIP code, sub-code and message.
     */
    @JsonProperty(value = "resultInformation")
    private final ResultInformation resultInformation;

    private PlayCompleted() {
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
}
