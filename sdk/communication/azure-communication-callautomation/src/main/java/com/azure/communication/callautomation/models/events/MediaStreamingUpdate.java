// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The MediaStreamingUpdate model. */
@Fluent
public final class MediaStreamingUpdate implements JsonSerializable<MediaStreamingUpdate> {
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
     * Get the contentType property: The contentType property.
     *
     * @return the contentType value.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set the contentType property: The contentType property.
     *
     * @param contentType the contentType value to set.
     * @return the MediaStreamingUpdate object itself.
     */
    public MediaStreamingUpdate setContentType(String contentType) {
        this.contentType = contentType;
        return this;
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
     * Set the mediaStreamingStatus property: The mediaStreamingStatus property.
     *
     * @param mediaStreamingStatus the mediaStreamingStatus value to set.
     * @return the MediaStreamingUpdate object itself.
     */
    public MediaStreamingUpdate setMediaStreamingStatus(MediaStreamingStatus mediaStreamingStatus) {
        this.mediaStreamingStatus = mediaStreamingStatus;
        return this;
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
     * Set the mediaStreamingStatusDetails property: The mediaStreamingStatusDetails property.
     *
     * @param mediaStreamingStatusDetails the mediaStreamingStatusDetails value to set.
     * @return the MediaStreamingUpdate object itself.
     */
    public MediaStreamingUpdate setMediaStreamingStatusDetails(
            MediaStreamingStatusDetails mediaStreamingStatusDetails) {
        this.mediaStreamingStatusDetails = mediaStreamingStatusDetails;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("contentType", contentType);
        jsonWriter.writeStringField("mediaStreamingStatus", mediaStreamingStatus != null ? mediaStreamingStatus.toString() : null);
        jsonWriter.writeStringField("mediaStreamingStatusDetails", mediaStreamingStatusDetails != null ? mediaStreamingStatusDetails.toString() : null);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of MediaStreamingUpdate from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of MediaStreamingUpdate if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the MediaStreamingUpdate.
     */
    public static MediaStreamingUpdate fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final MediaStreamingUpdate event = new MediaStreamingUpdate();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("contentType".equals(fieldName)) {
                    event.contentType =  reader.getString();
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
