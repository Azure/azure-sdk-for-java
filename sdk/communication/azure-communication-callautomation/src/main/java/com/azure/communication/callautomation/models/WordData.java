// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.Duration;

/**
 * The result for each word of the phrase
 */
public class WordData implements JsonSerializable<WordData> {

    /*
     * Text in the phrase.
     */
    private String text;

    /*
     * The word's position within the phrase.
     */
    private long offset;

    /*
     * Duration in ticks. 1 tick = 100 nanoseconds.
     */
    private long duration;

    /**
     * Get the text property.
     *
     * @return the text value.
     */
    public String getText() {
        return text;
    }

    /**
     * Get the offset property.
     *
     * @return the offset value.
     */
    public long getOffset() {
        return offset;
    }

      /**
     * Get the duration property.
     *
     * @return the duration value.
     */
    public Duration getDuration() {
        return Duration.ofNanos(duration * 100);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("text", this.text);
        jsonWriter.writeLongField("offset", this.offset);
        jsonWriter.writeLongField("duration", this.duration);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of Word from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of Word if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the Word.
     */
    public static WordData fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            WordData word = new WordData();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("text".equals(fieldName)) {
                    word.text = reader.getString();
                } else if ("offset".equals(fieldName)) {
                    word.offset = reader.getLong();
                } else if ("duration".equals(fieldName)) {
                    word.duration = reader.getLong();
                } else {
                    reader.skipChildren();
                }
            }
            return word;
        });
    }
}
