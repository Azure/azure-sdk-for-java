// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The response payload of the create audio routing group operation. */
@Immutable
public final class CreateAudioRoutingGroupResult {
    /*
     * The audio routing group id.
     */
    private final String audioRoutingGroupId;

    /**
     * Get the audioRoutingGroupId property: The audio routing group id.
     *
     * @return the audioRoutingGroupId value.
     */
    public String getAudioRoutingGroupId() {
        return this.audioRoutingGroupId;
    }

    /**
     * Initializes a new instance of CreateAudioRoutingGroupResult.
     *
     * @param audioRoutingGroupId the audioRoutingGroupId value to set.
     */
    public CreateAudioRoutingGroupResult(String audioRoutingGroupId) {
        this.audioRoutingGroupId = audioRoutingGroupId;
    }
}
