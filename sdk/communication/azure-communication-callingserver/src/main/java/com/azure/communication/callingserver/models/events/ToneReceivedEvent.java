// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.models.ToneInfo;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The subscribe to tone event. */
@Fluent
public final class ToneReceivedEvent extends CallingServerEventBase {
    /*
     * The tone info.
     */
    @JsonProperty(value = "toneInfo")
    private ToneInfo toneInfo;

    /*
     * The call leg.id.
     */
    @JsonProperty(value = "callLegId")
    private String callLegId;

    /**
     * Get the toneInfo property: The tone info.
     *
     * @return the toneInfo value.
     */
    public ToneInfo getToneInfo() {
        return this.toneInfo;
    }

    /**
     * Set the toneInfo property: The tone info.
     *
     * @param toneInfo the toneInfo value to set.
     * @return the ToneReceivedEvent object itself.
     */
    public ToneReceivedEvent setToneInfo(ToneInfo toneInfo) {
        this.toneInfo = toneInfo;
        return this;
    }

    /**
     * Get the callLegId property: The call leg.id.
     *
     * @return the callLegId value.
     */
    public String getCallLegId() {
        return this.callLegId;
    }

    /**
     * Set the callLegId property: The call leg.id.
     *
     * @param callLegId the callLegId value to set.
     * @return the ToneReceivedEvent object itself.
     */
    public ToneReceivedEvent setCallLegId(String callLegId) {
        this.callLegId = callLegId;
        return this;
    }

    /**
     * Deserialize {@link ToneReceivedEvent} event.
     *
     * @param eventData binary data for event
     * @return {@link ToneReceivedEvent} event.
     */
    public static ToneReceivedEvent deserialize(BinaryData eventData) {
        return eventData == null ? null : eventData.toObject(ToneReceivedEvent.class);
    }
}
