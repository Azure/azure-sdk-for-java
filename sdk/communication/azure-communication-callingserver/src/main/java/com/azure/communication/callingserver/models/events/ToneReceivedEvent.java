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
     * The call connection id.
     */
    @JsonProperty(value = "callConnectionId")
    private String callConnectionId;

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
     * Get the callConnectionId property: The call connection id.
     *
     * @return the callConnectionId value.
     */
    public String getCallConnectionId() {
        return this.callConnectionId;
    }

    /**
     * Set the callConnectionId property: The call connection id.
     *
     * @param callConnectionId the callConnectionId value to set.
     * @return the ToneReceivedEvent object itself.
     */
    public ToneReceivedEvent setCallConnectionId(String callConnectionId) {
        this.callConnectionId = callConnectionId;
        return this;
    }

    /**
     * Deserialize {@link com.azure.communication.callingserver.models.events.ToneReceivedEvent} event.
     *
     * @param eventData binary data for event
     * @return {@link com.azure.communication.callingserver.models.events.ToneReceivedEvent} event.
     */
    public static com.azure.communication.callingserver.models.events.ToneReceivedEvent deserialize(BinaryData eventData) {
        return eventData == null ? null : eventData.toObject(com.azure.communication.callingserver.models.events.ToneReceivedEvent.class);
    }
}
