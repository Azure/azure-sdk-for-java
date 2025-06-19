// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import java.io.IOException;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/** The MediaStreamingUpdateResult model. */
@Fluent
public final class MediaStreamingUpdateResult implements JsonSerializable<MediaStreamingUpdateResult> {
    /*
     * The contentType property.
     */
    private String contentType;

    /*
     * The mediaStreamingStatus property.
     */
    private MediaStreamingStatus mediaStreamingStatus;

    /*
     * The mediaStreamingStatusDetails property.
     */
    private MediaStreamingStatusDetails mediaStreamingStatusDetails;

    /**
     * Creates an instance of {@link MediaStreamingUpdateResult}.
     */
    public MediaStreamingUpdateResult() {
    }

    /**
     * Get the contentType property: The contentType property.
     *
     * @return the contentType value.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Get the mediaStreamingStatus property: The mediaStreamingStatus property.
     *
     * @return the mediaStreamingStatus value.
     */
    public MediaStreamingStatus getMediaStreamingStatus() {
        return this.mediaStreamingStatus;
    }

    /**
     * Get the mediaStreamingStatusDetails property: The mediaStreamingStatusDetails property.
     *
     * @return the mediaStreamingStatusDetails value.
     */
    public MediaStreamingStatusDetails getMediaStreamingStatusDetails() {
        return this.mediaStreamingStatusDetails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("contentType", contentType);
        jsonWriter.writeStringField("mediaStreamingStatus",
            mediaStreamingStatus != null ? mediaStreamingStatus.toString() : null);
        jsonWriter.writeStringField("mediaStreamingStatusDetails",
            mediaStreamingStatusDetails != null ? mediaStreamingStatusDetails.toString() : null);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of MediaStreamingUpdate from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of MediaStreamingUpdate if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the MediaStreamingUpdateResult.
     */
    public static MediaStreamingUpdateResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final MediaStreamingUpdateResult event = new MediaStreamingUpdateResult();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("contentType".equals(fieldName)) {
                    event.contentType = reader.getString();
                } else if ("mediaStreamingStatus".equals(fieldName)) {
                    event.mediaStreamingStatus = MediaStreamingStatus.fromString(reader.getString());
                } else if ("mediaStreamingStatusDetails".equals(fieldName)) {
                    event.mediaStreamingStatusDetails = MediaStreamingStatusDetails.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return event;
        });
    }
}
