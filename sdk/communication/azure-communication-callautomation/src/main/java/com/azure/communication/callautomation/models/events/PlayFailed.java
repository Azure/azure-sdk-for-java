// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The PlayFailed model. */
@Immutable
public final class PlayFailed extends CallAutomationEventBaseWithReasonCode {

    private PlayFailed() {
    }

    /*
     * Contains the index of the failed play source.
     */
    private Integer failedPlaySourceIndex;

    /**
     * Get the failedPlaySourceIndex property: Contains the index of the failed play source.
     * 
     * @return the failedPlaySourceIndex value.
     */
    public Integer getFailedPlaySourceIndex() {
        return this.failedPlaySourceIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeIntField("failedPlaySourceIndex", failedPlaySourceIndex);
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of PlayFailed from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of PlayFailed if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the PlayFailed.
     */
    public static PlayFailed fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final PlayFailed event = new PlayFailed();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("failedPlaySourceIndex".equals(fieldName)) {
                    event.failedPlaySourceIndex = reader.getInt();
                } else {
                    if (!event.readField(fieldName, reader)) {
                        reader.skipChildren();
                    }
                }
            }
            return event;
        });
    }
}
