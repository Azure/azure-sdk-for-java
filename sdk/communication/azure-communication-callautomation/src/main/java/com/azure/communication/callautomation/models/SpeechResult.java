// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The continuous speech recognition result. */
@Fluent
@Immutable
public final class SpeechResult extends RecognizeResult {

    private SpeechResult() {
    }

    /*
     * The recognized speech in string.
     */
    private String speech;

    /**
     * Get the speech property: The recognized speech in string.
     *
     * @return the speech value.
     */
    public String getSpeech() {
        return this.speech;
    }


    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("speech", this.speech);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SpeechResult from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of SpeechResult if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the SpeechResult.
     */
    public static SpeechResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final SpeechResult result = new SpeechResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("speech".equals(fieldName)) {
                    result.speech = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return result;
        });
    }
}
