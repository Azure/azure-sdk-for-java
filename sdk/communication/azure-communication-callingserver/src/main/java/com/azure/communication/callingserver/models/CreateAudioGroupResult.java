// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The response payload of the create audio group operation. */
@Immutable
public final class CreateAudioGroupResult {
    /*
     * The audio group id.
     */
    private final String audioGroupId;

    /**
     * Get the audioGroupId property: The audio group id.
     *
     * @return the audioGroupId value.
     */
    public String getAudioGroupId() {
        return this.audioGroupId;
    }

    /**
     * Initializes a new instance of CreateaudioGroupResult.
     *
     * @param audioGroupId the audioGroupId value to set.
     */
    public CreateAudioGroupResult(String audioGroupId) {
        this.audioGroupId = audioGroupId;
    }
}
