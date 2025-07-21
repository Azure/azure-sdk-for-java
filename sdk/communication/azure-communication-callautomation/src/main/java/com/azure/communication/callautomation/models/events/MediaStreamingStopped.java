// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import java.io.IOException;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/** The MediaStreamingStopped model. */
@Fluent
public final class MediaStreamingStopped extends CallAutomationEventBase {

    /*
     * Defines the result for audio streaming update with the current status
     * and the details about the status
     */
    private MediaStreamingUpdateResult mediaStreamingUpdateResult;

    /**
    * Creates an instance of MediaStreamingStopped class.
    */
    public MediaStreamingStopped() {
        mediaStreamingUpdateResult = null;
    }

    /**
     * Get the getMediaStreamingUpdateResult property: Defines the result for audio streaming update with the current status and
     * the details about the status.
     *
     * @return the MediaStreamingUpdateResult value.
     */
    public MediaStreamingUpdateResult getMediaStreamingUpdateResult() {
        return this.mediaStreamingUpdateResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("mediaStreamingUpdate", mediaStreamingUpdateResult);
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of MediaStreamingStopped from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of MediaStreamingStopped if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the MediaStreamingStopped.
     */
    public static MediaStreamingStopped fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final MediaStreamingStopped event = new MediaStreamingStopped();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("mediaStreamingUpdate".equals(fieldName)) {
                    event.mediaStreamingUpdateResult = MediaStreamingUpdateResult.fromJson(reader);
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
