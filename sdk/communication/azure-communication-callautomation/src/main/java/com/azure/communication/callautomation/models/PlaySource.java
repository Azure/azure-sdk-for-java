// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        this.writeJsonImpl(jsonWriter);
        jsonWriter.writeStringField("playSourceCacheId", this.playSourceCacheId);
        return jsonWriter.writeEndObject();
    }

    abstract void writeJsonImpl(JsonWriter jsonWriter) throws IOException;
}
