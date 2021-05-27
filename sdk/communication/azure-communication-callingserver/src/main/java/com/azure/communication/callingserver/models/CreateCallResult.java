// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The response payload of the create call operation. */
@Fluent
public final class CreateCallResult {
    /*
     * Call leg id of the call.
     */
    @JsonProperty(value = "callLegId")
    private String callLegId;

    /**
     * Get the callLegId property: Call leg id of the call.
     *
     * @return the callLegId value.
     */
    public String getCallLegId() {
        return this.callLegId;
    }

    /**
     * Set the callLegId property: Call leg id of the call.
     *
     * @param callLegId the callLegId value to set.
     * @return the CreateCallResponse object itself.
     */
    public CreateCallResult setCallLegId(String callLegId) {
        this.callLegId = callLegId;
        return this;
    }
}
