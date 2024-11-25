// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The PlaySource model. */
@Fluent
public abstract class PlaySource implements JsonSerializable<PlaySource> {
    /*
     * Defines the identifier to be used for caching related media
     */
    private String playSourceId;

    /**
     * Creates a new instance of {@link PlaySource}.
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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject().writeStringField("playSourceId", playSourceId).writeEndObject();
    }

    /**
     * Reads an instance of {@link PlaySource} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read.
     * @return An instance of {@link PlaySource}, or null if the {@link JsonReader} was pointing to
     * {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static PlaySource fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String playSourceId = null;
            boolean uriFound = false;
            String uri = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("playSourceId".equals(fieldName)) {
                    playSourceId = reader.getString();
                } else if ("uri".equals(fieldName)) {
                    uriFound = true;
                    uri = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            PlaySource playSource = uriFound ? new FileSource().setUri(uri) : new PlaySource() {
            };
            playSource.setPlaySourceId(playSourceId);
            return playSource;
        });
    }
}
