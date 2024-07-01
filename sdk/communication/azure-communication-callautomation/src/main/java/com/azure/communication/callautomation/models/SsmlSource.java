// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The SsmlSource model. */
@Fluent
public final class SsmlSource extends PlaySource {
    /*
     * Ssml string for the cognitive service to be played
     */
    private String ssmlText;

    /*
     * Endpoint where the Custom Voice was deployed.
     */
    private String customVoiceEndpointId;

    /**
     * Get the ssmlText property: Ssml string for the cognitive service to be played.
     *
     * @return the ssmlText value.
     */
    public String getSsmlText() {
        return this.ssmlText;
    }

    /**
     * Set the ssmlText property: Ssml string for the cognitive service to be played.
     *
     * @param ssmlText the ssmlText value to set.
     * @return the SsmlSourceInternal object itself.
     */
    public SsmlSource setSsmlText(String ssmlText) {
        this.ssmlText = ssmlText;
        return this;
    }

    /**
     * Get the customVoiceEndpointId property: Endpoint where the custom voice was deployed.
     *
     * @return the customVoiceEndpointId value.
     */
    public String getCustomVoiceEndpointId() {
        return this.customVoiceEndpointId;
    }

    /**
     * Set the customVoiceEndpointId property: Endpoint where the custom voice was deployed.
     *
     * @param customVoiceEndpointId the customVoiceEndpointId value to set.
     * @return the TextSourceInternal object itself.
     */
    public SsmlSource setCustomVoiceEndpointId(String customVoiceEndpointId) {
        this.customVoiceEndpointId = customVoiceEndpointId;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("ssmlText", this.ssmlText);
        jsonWriter.writeStringField("customVoiceEndpointId", this.customVoiceEndpointId);
        jsonWriter.writeStringField("playSourceCacheId", this.getPlaySourceCacheId());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SsmlSource from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of SsmlSource if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the SsmlSource.
     */
    public static SsmlSource fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final SsmlSource source = new SsmlSource();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("ssmlText".equals(fieldName)) {
                    source.ssmlText = reader.getString();
                } else if ("customVoiceEndpointId".equals(fieldName)) {
                    source.customVoiceEndpointId = reader.getString();
                } else if ("playSourceCacheId".equals(fieldName)) {
                    // Set the property of the base class 'PlaySource'.
                    source.setPlaySourceCacheId(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return source;
        });
    }
}
