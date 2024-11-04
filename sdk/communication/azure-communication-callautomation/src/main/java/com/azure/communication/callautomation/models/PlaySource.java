// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonSerializable;

/** The PlaySource model. */
@Fluent
public abstract class PlaySource implements JsonSerializable<PlaySource> {
    /*
     * Defines the identifier to be used for caching related media
     */
    private String playSourceCacheId;

    /**
     * Get the playSourceCacheId property: Defines the identifier to be used for caching related media.
     *
     * @return the playSourceCacheId value.
     */
    public String getPlaySourceCacheId() {
        return this.playSourceCacheId;
    }

    /**
     * Set the playSourceCacheId property: Defines the identifier to be used for caching related media.
     *
     * @param playSourceCacheId the playSourceCacheId value to set.
     * @return the PlaySourceInternal object itself.
     */
    public PlaySource setPlaySourceCacheId(String playSourceCacheId) {
        this.playSourceCacheId = playSourceCacheId;
        return this;
    }
}
