// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.io.IOException;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

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

    /*
     * The identified language for a spoken phrase.
     */
    private String languageIdentified;

    /*
     * Gets or sets the sentiment analysis result.
     */
    private SentimentAnalysisResult sentimentAnalysisResult;

    /**
     * Get the speech property: The recognized speech in string.
     *
     * @return the speech value.
     */
    public String getSpeech() {
        return this.speech;
    }

    /**
     * Get the languageIdentified property: The identified language for a spoken
     * phrase.
     * 
     * @return the languageIdentified value.
     */
    public String getLanguageIdentified() {
        return this.languageIdentified;
    }

    /**
     * Get the sentimentAnalysisResult property: Gets or sets the sentiment analysis
     * result.
     * 
     * @return the sentimentAnalysisResult value.
     */
    public SentimentAnalysisResult getSentimentAnalysisResult() {
        return this.sentimentAnalysisResult;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("speech", this.speech);
        jsonWriter.writeStringField("languageIdentified", this.languageIdentified);
        if (this.sentimentAnalysisResult != null) {
            jsonWriter.writeFieldName("sentimentAnalysisResult");
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("sentiment", sentimentAnalysisResult.getSentiment());
            jsonWriter.writeEndObject();
        }
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
                } else if ("languageIdentified".equals(fieldName)) {
                    result.languageIdentified = reader.getString();
                } else if ("sentimentAnalysisResult".equals(fieldName)) {
                    result.sentimentAnalysisResult = SentimentAnalysisResult.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return result;
        });
    }
}
