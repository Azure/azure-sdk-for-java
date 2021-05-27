// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.models.ToneInfo;

/**
 * The subscribe to tone event.
 */
public final class ToneReceivedEvent extends CallEventBase
{
    /**
     * The event type.
     */
    public static final String EventType = "Microsoft.Communication.DtmfReceived";

    /**
     * The tone info.
     */
    public ToneInfo toneInfo;

    /**
     * Get the tone info.
     * @return the tone info value.
     */
    public ToneInfo getToneInfo() {
        return this.toneInfo;
    }

    /**
     * Set the tone info.
     * @param toneInfo the tone info.
     * @return the ToneReceivedEvent object itself.
     */
    public ToneReceivedEvent setToneInfo(ToneInfo toneInfo) {
        this.toneInfo = toneInfo;
        return this;
    }

    /**
     * The call leg Id.
     */
    public String callLegId;

    /**
     * Get the call leg Id.
     *
     * @return the time of the recording started.
     */
    public String getCallLegId() {
        return this.callLegId;
    }

    /**
     * Set the call leg Id.
     *
     * @param callLegId the call leg id.
     * @return the ToneReceivedEvent object itself.
     */
    public ToneReceivedEvent setCallLegId(String callLegId) {
        this.callLegId = callLegId;
        return this;
    }

    /**
     * Initializes a new instance of ToneReceivedEvent.
     * @param toneInfo The tone info.
     * @param callLegId The call leg id.
     */
    public ToneReceivedEvent(ToneInfo toneInfo, String callLegId)
    {
        if (toneInfo == null)
        {
            throw new IllegalArgumentException(String.format("object '%s' cannot be null", toneInfo.getClass().getName()));
        }
        if (callLegId == null || callLegId.isEmpty())
        {
            throw new IllegalArgumentException(String.format("object '%s' cannot be null", callLegId.getClass().getName()));
        }
        this.toneInfo = toneInfo;
        this.callLegId = callLegId;
    }
}
