// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The PlaySource model. */
@Fluent
public abstract class PlaySource {
    /*
     * Defines the identifier to be used for caching related media
     */
    @JsonProperty(value = "playSourceId")
    private String playSourceId;

    /**
     * Get the playSourceId property: Defines the identifier to be used for caching related media.
     *
     * @return the playSourceId value.
     */
    public String getPlaySourceId() {
        return this.playSourceId;
    }

    /**
     * Set the playSourceId property: Defines the identifier to be used for caching related media.
     *
     * @param playSourceId the playSourceId value to set.
     * @return the PlaySourceInternal object itself.
     */
    public PlaySource setPlaySourceId(String playSourceId) {
        this.playSourceId = playSourceId;
        return this;
    }
}
