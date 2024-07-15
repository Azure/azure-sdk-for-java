// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonSerializable;

/** The PlaySource model. */
@Fluent
public abstract class PlaySource implements JsonSerializable<PlaySource> {
    /*
     * Defines the identifier to be used for caching related media
     */
    private String playSourceId;

    /**
     * Creates a PlaySource.
     */
    public PlaySource() {
    }

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
