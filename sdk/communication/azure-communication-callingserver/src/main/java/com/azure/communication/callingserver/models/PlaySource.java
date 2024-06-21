// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/** The PlaySource model. */
@Fluent
public abstract class PlaySource implements JsonSerializable<PlaySource> {
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

    void writeFields(JsonWriter writer) throws IOException {
        writer.writeStringField("playSourceId", this.playSourceId);
    }

    boolean readField(String fieldName, JsonReader reader) throws IOException {
        if ("playSourceId".equals(fieldName)) {
            this.playSourceId = reader.getString();
            return true;
        }
        return false;
    }
}
