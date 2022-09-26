// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

public abstract class CallAutomationEventWithReasonCodeBase extends CallAutomationEventBase {

    /** Returns the reason code name of the event */
    public ReasonCodeName getReasonCodeName() {
        return ReasonCodeName.fromReasonCode(getResultInformation().getSubCode());
    }
}
