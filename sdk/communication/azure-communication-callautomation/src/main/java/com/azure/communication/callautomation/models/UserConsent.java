// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The UserConsent model. */
@Fluent
public final class UserConsent implements JsonSerializable<UserConsent> {
    /*
     * The recording property.
     */
    private Integer recording;

    /** Creates an instance of UserConsent class. */
    public UserConsent() {}

    /**
     * Get the recording property: The recording property.
     *
     * @return the recording value.
     */
    public Integer getRecording() {
        return this.recording;
    }

    /**
     * Set the recording property: The recording property.
     *
     * @param recording the recording value to set.
     * @return the UserConsent object itself.
     */
    public UserConsent setRecording(Integer recording) {
        this.recording = recording;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeNumberField("recording", recording);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of UserConsent from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of UserConsent if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the UserConsent.
     */
    public static UserConsent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            UserConsent consent = new UserConsent();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("recording".equals(fieldName)) {
                    consent.recording = reader.getNullable(JsonReader::getInt);
                } else {
                    reader.skipChildren();
                }
            }
            return consent;
        });
    }
}
