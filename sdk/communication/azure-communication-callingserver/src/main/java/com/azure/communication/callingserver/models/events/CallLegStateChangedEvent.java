// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.models.CallState;
import com.azure.core.util.BinaryData;

/**
 * The call leg state change event.
 */
public final class CallLegStateChangedEvent extends CallingServerEventBase {
    /**
     * The event type.
     */
    public static final String EVENT_TYPE = "Microsoft.Communication.CallLegStateChanged";

    /**
     * The conversation id.
     */
    private String conversationId;

    /**
     * Get the conversation id.
     * 
     * @return the conversationId value.
     */
    public String getConversationId() {
        return this.conversationId;
    }

    /**
     * Set the subject.
     *
     * @param conversationId the conversation id.
     * @return the CallLegStateChangedEvent object itself.
     */
    public CallLegStateChangedEvent setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    /**
     * The call leg.id.
     */
    private String callLegId;

    /**
     * Get the call leg id.
     *
     * @return the call leg id value.
     */
    public String getCallLegId() {
        return this.callLegId;
    }

    /**
     * Set the subject.
     *
     * @param callLegId the call leg id.
     * @return the CallLegStateChangedEvent object itself.
     */
    public CallLegStateChangedEvent setCallLegId(String callLegId) {
        this.callLegId = callLegId;
        return this;
    }

    /**
     * The call state.
     */
    private CallState callState;

    /**
     * Get the call state.
     *
     * @return the call state value.
     */
    public CallState getCallState() {
        return this.callState;
    }

    /**
     * Set the call state.
     *
     * @param callState the call state.
     * @return the CallLegStateChangedEvent object itself.
     */
    public CallLegStateChangedEvent setCallState(CallState callState) {
        this.callState = callState;
        return this;
    }

    /**
     * Initializes a new instance of CallLegStateChangedEvent.
     * 
     * @param conversationId The conversation id.
     * @param callLegId The call leg id.
     * @param callState The call state.
     * @throws IllegalArgumentException if any parameter is null or empty.
     */
    public CallLegStateChangedEvent(String conversationId, String callLegId, CallState callState) {
        if (conversationId == null || conversationId.isEmpty()) {
            throw new IllegalArgumentException("object conversationId cannot be null or empty");
        }
        if (callLegId == null || callLegId.isEmpty()) {
            throw new IllegalArgumentException("object callLegId cannot be null or empty");
        }
        if (callState == null) {
            throw new IllegalArgumentException("object callState cannot be null");
        }
        this.conversationId = conversationId;
        this.callLegId = callLegId;
        this.callState = callState;
    }

    /**
     * Deserialize {@link CallLegStateChangedEvent} event.
     * 
     * @param eventData binary data for event
     * @return {@link CallLegStateChangedEvent} event.
     */
    public static CallLegStateChangedEvent deserialize(BinaryData eventData) {
        if (eventData == null) {
            return null;
        }
        return eventData.toObject(CallLegStateChangedEvent.class);
    }
}
