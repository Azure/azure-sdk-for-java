// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.io.IOException;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/**
 * The SentimentAnalysisResult model.
 */
@Fluent
public class SentimentAnalysisResult implements JsonSerializable<SentimentAnalysisResult> {
    /*
     * Gets or sets the value of the sentiment detected (positive, negative,
     * neutral, mixed).
     */

    private String sentiment;

    /**
     * Creates an instance of SentimentAnalysisResult class.
     */
    public SentimentAnalysisResult() {
    }

    /**
     * Get the sentiment property: Gets or sets the value of the sentiment detected
     * (positive, negative, neutral,
     * mixed).
     * 
     * @return the sentiment value.
     */
    public String getSentiment() {
        return this.sentiment;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("sentiment", this.sentiment);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SentimentAnalysisResult from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of SentimentAnalysisResult if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the SentimentAnalysisResult.
     */
    public static SentimentAnalysisResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SentimentAnalysisResult sentimentAnalysisResult = new SentimentAnalysisResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("sentiment".equals(fieldName)) {
                    sentimentAnalysisResult.sentiment = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return sentimentAnalysisResult;
        });
    }
}
