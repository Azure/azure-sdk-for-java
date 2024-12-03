// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.core.annotation.Immutable;

import java.io.IOException;

/** The ChoiceResult model. */
@Immutable
public final class ChoiceResult extends RecognizeResult {
    /*
     * Label is the primary identifier for the choice detected
     */
    private String label;

    /*
     * Phrases are set to the value if choice is selected via phrase detection.
     * If Dtmf input is recognized, then Label will be the identifier for the
     * choice detected and phrases will be set to null
     */
    private String recognizedPhrase;

    private ChoiceResult() {
    }
    
    /**
     * Get the label property: Label is the primary identifier for the choice detected.
     *
     * @return the label value.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Get the recognizedPhrase property: Phrases are set to the value if choice is selected via phrase detection. If
     * Dtmf input is recognized, then Label will be the identifier for the choice detected and phrases will be set to
     * null.
     *
     * @return the recognizedPhrase value.
     */
    public String getRecognizedPhrase() {
        return this.recognizedPhrase;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("label", this.label);
        jsonWriter.writeStringField("recognizedPhrase", this.recognizedPhrase);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ChoiceResult from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ChoiceResult if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ChoiceResult.
     */
    public static ChoiceResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final ChoiceResult result = new ChoiceResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("label".equals(fieldName)) {
                    result.label = reader.getString();
                } else if ("recognizedPhrase".equals(fieldName)) {
                    result.recognizedPhrase = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return result;
        });
    }
}
