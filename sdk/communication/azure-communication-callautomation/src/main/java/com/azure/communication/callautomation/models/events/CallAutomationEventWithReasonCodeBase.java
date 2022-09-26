// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

/**
 * The base event interface with ReasonCodeName added.
 */
public abstract class CallAutomationEventWithReasonCodeBase extends CallAutomationEventBase {

    /**
     * Returns the reason code name of the event
     * @return a ReasonCodeName object.
     * */
    public ReasonCodeName getReasonCodeName() {
        return ReasonCodeName.fromReasonCode(getResultInformation().getSubCode());
    }
}
