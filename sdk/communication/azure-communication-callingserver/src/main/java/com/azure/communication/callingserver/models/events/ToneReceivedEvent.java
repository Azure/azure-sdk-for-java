// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.implementation.models.ToneReceivedEventInternal;
import com.azure.communication.callingserver.models.ToneInfo;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

/** The subscribe to tone event. */
@Immutable
public final class ToneReceivedEvent extends CallingServerEventBase {
    /*
     * The tone info.
     */
    private final ToneInfo toneInfo;

    /*
     * The call connection id.
     */
    private final String callConnectionId;

    /**
     * Get the toneInfo property: The tone info.
     *
     * @return the toneInfo value.
     */
    public ToneInfo getToneInfo() {
        return toneInfo;
    }

    /**
     * Get the callConnectionId property: The call connection id.
     *
     * @return the callConnectionId value.
     */
    public String getCallConnectionId() {
        return callConnectionId;
    }

    /**
     * Initializes a new instance of ToneReceivedEvent.
     *
     * @param toneInfo the toneInfo value.
     * @param callConnectionId the callConnectionId value.
     */
    ToneReceivedEvent(ToneInfo toneInfo, String callConnectionId) {
        this.toneInfo = toneInfo;
        this.callConnectionId = callConnectionId;
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
        ToneReceivedEventInternal toneReceivedEventInternal = eventData.toObject(ToneReceivedEventInternal.class);

        return new ToneReceivedEvent(
            new ToneInfo(
                toneReceivedEventInternal.getToneInfo().getSequenceId(),
                toneReceivedEventInternal.getToneInfo().getTone()),
            toneReceivedEventInternal.getCallConnectionId());
    }
}
