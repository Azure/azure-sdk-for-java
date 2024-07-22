// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/** The DocumentSentiment model. */
@Fluent
public final class DocumentSentiment implements JsonSerializable<DocumentSentiment> {
    /*
     * Unique, non-empty document identifier.
     */
    private String id;

    /*
     * Predicted sentiment for document (Negative, Neutral, Positive, or
     * Mixed).
     */
    private DocumentSentimentValue sentiment;

    /*
     * if showStats=true was specified in the request this field will contain
     * information about the document payload.
     */
    private DocumentStatistics statistics;

    /*
     * Document level sentiment confidence scores between 0 and 1 for each
     * sentiment class.
     */
    private SentimentConfidenceScorePerLabel confidenceScores;

    /*
     * Sentence level sentiment analysis.
     */
    private List<SentenceSentiment> sentences;

    /*
     * Warnings encountered while processing document.
     */
    private List<DocumentWarning> warnings;

    /**
     * Get the id property: Unique, non-empty document identifier.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: Unique, non-empty document identifier.
     *
     * @param id the id value to set.
     * @return the DocumentSentiment object itself.
     */
    public DocumentSentiment setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the sentiment property: Predicted sentiment for document (Negative, Neutral, Positive, or Mixed).
     *
     * @return the sentiment value.
     */
    public DocumentSentimentValue getSentiment() {
        return this.sentiment;
    }

    /**
     * Set the sentiment property: Predicted sentiment for document (Negative, Neutral, Positive, or Mixed).
     *
     * @param sentiment the sentiment value to set.
     * @return the DocumentSentiment object itself.
     */
    public DocumentSentiment setSentiment(DocumentSentimentValue sentiment) {
        this.sentiment = sentiment;
        return this;
    }

    /**
     * Get the statistics property: if showStats=true was specified in the request this field will contain information
     * about the document payload.
     *
     * @return the statistics value.
     */
    public DocumentStatistics getStatistics() {
        return this.statistics;
    }

    /**
     * Set the statistics property: if showStats=true was specified in the request this field will contain information
     * about the document payload.
     *
     * @param statistics the statistics value to set.
     * @return the DocumentSentiment object itself.
     */
    public DocumentSentiment setStatistics(DocumentStatistics statistics) {
        this.statistics = statistics;
        return this;
    }

    /**
     * Get the confidenceScores property: Document level sentiment confidence scores between 0 and 1 for each sentiment
     * class.
     *
     * @return the confidenceScores value.
     */
    public SentimentConfidenceScorePerLabel getConfidenceScores() {
        return this.confidenceScores;
    }

    /**
     * Set the confidenceScores property: Document level sentiment confidence scores between 0 and 1 for each sentiment
     * class.
     *
     * @param confidenceScores the confidenceScores value to set.
     * @return the DocumentSentiment object itself.
     */
    public DocumentSentiment setConfidenceScores(SentimentConfidenceScorePerLabel confidenceScores) {
        this.confidenceScores = confidenceScores;
        return this;
    }

    /**
     * Get the sentences property: Sentence level sentiment analysis.
     *
     * @return the sentences value.
     */
    public List<SentenceSentiment> getSentences() {
        return this.sentences;
    }

    /**
     * Set the sentences property: Sentence level sentiment analysis.
     *
     * @param sentences the sentences value to set.
     * @return the DocumentSentiment object itself.
     */
    public DocumentSentiment setSentences(List<SentenceSentiment> sentences) {
        this.sentences = sentences;
        return this;
    }

    /**
     * Get the warnings property: Warnings encountered while processing document.
     *
     * @return the warnings value.
     */
    public List<DocumentWarning> getWarnings() {
        return this.warnings;
    }

    /**
     * Set the warnings property: Warnings encountered while processing document.
     *
     * @param warnings the warnings value to set.
     * @return the DocumentSentiment object itself.
     */
    public DocumentSentiment setWarnings(List<DocumentWarning> warnings) {
        this.warnings = warnings;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", this.id);
        jsonWriter.writeStringField("sentiment", this.sentiment.toString());
        jsonWriter.writeJsonField("statistics", this.statistics);
        jsonWriter.writeJsonField("confidenceScores", this.confidenceScores);
        jsonWriter.writeArrayField("sentences", this.sentences, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("warnings", this.warnings, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DocumentSentiment from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of DocumentSentiment if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the DocumentSentiment.
     */
    public static DocumentSentiment fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DocumentSentiment deserializedDocumentSentiment = new DocumentSentiment();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedDocumentSentiment.id = reader.getString();
                } else if ("sentiment".equals(fieldName)) {
                    deserializedDocumentSentiment.sentiment = DocumentSentimentValue.fromString(reader.getString());
                } else if ("statistics".equals(fieldName)) {
                    deserializedDocumentSentiment.statistics = DocumentStatistics.fromJson(reader);
                } else if ("confidenceScores".equals(fieldName)) {
                    deserializedDocumentSentiment.confidenceScores = SentimentConfidenceScorePerLabel.fromJson(reader);
                } else if ("sentences".equals(fieldName)) {
                    deserializedDocumentSentiment.sentences = reader.readArray(reader1 -> SentenceSentiment.fromJson(reader1));
                } else if ("warnings".equals(fieldName)) {
                    List<DocumentWarning> warnings = reader.readArray(reader1 -> DocumentWarning.fromJson(reader1));
                    deserializedDocumentSentiment.warnings = warnings;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedDocumentSentiment;
        });
    }
}
