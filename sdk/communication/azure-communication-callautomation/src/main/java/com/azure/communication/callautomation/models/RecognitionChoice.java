// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/** The RecognitionChoice model. */
public final class RecognitionChoice implements JsonSerializable<RecognitionChoice> {
    /*
     * Identifier for a given choice
     */
    private String label;

    /*
     * List of phrases to recognize
     */
    private List<String> phrases;

    /*
     * The tone property.
     */
    private DtmfTone tone;

    /**
     * Get the label property: Identifier for a given choice.
     *
     * @return the label value.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Set the label property: Identifier for a given choice.
     *
     * @param label the label value to set.
     * @return the RecognitionChoice object itself.
     */
    public RecognitionChoice setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Get the phrases property: List of phrases to recognize.
     *
     * @return the phrases value.
     */
    public List<String> getPhrases() {
        return this.phrases;
    }

    /**
     * Set the phrases property: The phrases property.
     *
     * @param phrases the phrases value to set.
     * @return the RecognitionChoice object itself.
     */
    public RecognitionChoice setPhrases(List<String> phrases) {
        this.phrases = phrases;
        return this;
    }

    /**
     * Get the tone property: The tone property.
     *
     * @return the tone value.
     */
    public DtmfTone getTone() {
        return this.tone;
    }

    /**
     * Set the tone property: The tone property.
     *
     * @param tone the tone value to set.
     * @return the RecognitionChoice object itself.
     */
    public RecognitionChoice setTone(DtmfTone tone) {
        this.tone = tone;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("label", this.label);
        jsonWriter.writeArrayField("phrases", this.phrases, JsonWriter::writeString);
        jsonWriter.writeStringField("tone", this.tone.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CallConnectionPropertiesInternal from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CallConnectionPropertiesInternal if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the CallConnectionPropertiesInternal.
     */
    public static RecognitionChoice fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final RecognitionChoice choice = new RecognitionChoice();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("label".equals(fieldName)) {
                    choice.label = reader.getString();
                } else if ("phrases".equals(fieldName)) {
                    choice.phrases = reader.readArray(JsonReader::getString);
                } else if ("tone".equals(fieldName)) {
                    choice.tone = DtmfTone.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return choice;
        });
    }
}
