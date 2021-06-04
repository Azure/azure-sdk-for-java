// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.models.ToneInfo;
import com.azure.core.util.BinaryData;

/**
 * The subscribe to tone event.
 */
public final class ToneReceivedEvent extends CallingServerEventBase {
    /**
     * The event type.
     */
    public static final String EVENT_TYPE = "Microsoft.Communication.DtmfReceived";

    /**
     * The tone info.
     */
    private ToneInfo toneInfo;

    /**
     * Get the tone info.
     * 
     * @return the tone info value.
     */
    public ToneInfo getToneInfo() {
        return this.toneInfo;
    }

    /**
     * Set the tone info.
     * 
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
    private String callLegId;

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
     */
    public ToneReceivedEvent() {

    }

    /**
     * Initializes a new instance of ToneReceivedEvent.
     * 
     * @param toneInfo The tone info.
     * @param callLegId The call leg id.
     * @throws IllegalArgumentException if any parameter is null or empty.
     */
    public ToneReceivedEvent(ToneInfo toneInfo, String callLegId) {
        if (toneInfo == null) {
            throw new IllegalArgumentException("object toneInfo cannot be null");
        }
        if (callLegId == null || callLegId.isEmpty()) {
            throw new IllegalArgumentException("object callLegId cannot be null or empty");
        }
        this.toneInfo = toneInfo;
        this.callLegId = callLegId;
    }

    /**
     * Deserialize {@link ToneReceivedEvent} event.
     * 
     * @param eventData binary data for event
     * @return {@link ToneReceivedEvent} event.
     */
    public static ToneReceivedEvent deserialize(BinaryData eventData) {
        if (eventData == null) {
            return null;
        }
        return eventData.toObject(ToneReceivedEvent.class);
    }
}
