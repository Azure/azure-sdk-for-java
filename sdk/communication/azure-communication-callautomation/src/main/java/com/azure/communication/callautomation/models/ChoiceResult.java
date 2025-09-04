// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.io.IOException;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/** The ChoiceResult model. */
@Fluent
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

    /*
     * The identified language for a spoken phrase.
     */
    private String languageIdentified;

    /*
     * Gets or sets the sentiment analysis result.
     */
    private SentimentAnalysisResult sentimentAnalysisResult;

    /**
     * Creates an instance of {@link ChoiceResult}.
     */
    ChoiceResult() {
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
     * Set the label property: Label is the primary identifier for the choice detected.
     *
     * @param label the label value to set.
     * @return the ChoiceResult object itself.
     */
    public ChoiceResult setLabel(String label) {
        this.label = label;
        return this;
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

    /**
     * Set the recognizedPhrase property: Phrases are set to the value if choice is selected via phrase detection. If
     * Dtmf input is recognized, then Label will be the identifier for the choice detected and phrases will be set to
     * null.
     *
     * @param recognizedPhrase the recognizedPhrase value to set.
     * @return the ChoiceResult object itself.
     */
    public ChoiceResult setRecognizedPhrase(String recognizedPhrase) {
        this.recognizedPhrase = recognizedPhrase;
        return this;
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
        jsonWriter.writeStringField("label", this.label);
        jsonWriter.writeStringField("recognizedPhrase", this.recognizedPhrase);
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
