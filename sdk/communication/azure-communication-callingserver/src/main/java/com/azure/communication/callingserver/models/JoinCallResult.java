// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The response payload of the join call operation. */
@Immutable
public final class JoinCallResult {
    /*
     * The call connection id.
     */
    private final String callConnectionId;

    /**
     * Get the callConnectionId property: The call connection id.
     *
     * @return the callConnectionId value.
     */
    public String getCallConnectionId() {
        return callConnectionId;
    }

    /**
     * Initializes a new instance of JoinCallResult.
     *
     * @param callConnectionId the callConnectionId value to set.
     */
    public JoinCallResult(String callConnectionId) {
        this.callConnectionId = callConnectionId;
    }
}
