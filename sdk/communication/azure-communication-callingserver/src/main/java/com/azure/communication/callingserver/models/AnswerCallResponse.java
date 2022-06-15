// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The response payload of the answer call operation. */
@Immutable
public final class AnswerCallResponse {
    /*
     * The server call id.
     */
    private final String serverCallId;

    /*
     * The subscription id.
     */
    private final String callConnectionId;

    /**
     * The response of answer call request
     *
     * @param serverCallId The server call id.
     * @param callConnectionId The subscription id.
     */
    public AnswerCallResponse(String serverCallId, String callConnectionId) {
        this.serverCallId = serverCallId;
        this.callConnectionId = callConnectionId;
    }

    /**
     * Get the serverCallId property: The server call id.
     *
     * @return the serverCallId value.
     */
    public String getServerCallId() {
        return this.serverCallId;
    }

    /**
     * Get the callConnectionId property: The subscription id.
     *
     * @return the callConnectionId value.
     */
    public String getCallConnectionId() {
        return this.callConnectionId;
    }
}
