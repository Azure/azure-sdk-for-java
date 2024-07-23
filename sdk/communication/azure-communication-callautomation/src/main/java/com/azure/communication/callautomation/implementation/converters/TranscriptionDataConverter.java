// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import com.azure.communication.callautomation.models.streaming.StreamingDataParser;
import com.azure.communication.callautomation.models.streaming.transcription.Word;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;
import java.util.List;

/**
 * The TranscriptionDataInternal model.
 */
public final class TranscriptionDataConverter {

    /*
     * The display form of the recognized word
     */
    private String text;

    /*
     * The format of text
     */
    private String format;

    /*
     * Confidence of recognition of the whole phrase, from 0.0 (no confidence) to 1.0 (full confidence)
     */
    private double confidence;

    /*
     * The position of this payload
     */
    private long offset;

     /*
     * Duration in ticks. 1 tick = 100 nanoseconds.
     */
    private long duration;


    /*
     * TThe result for each word of the phrase
     */
    private List<Word> words;

    /*
     * The participantId.
     */
    private String participantRawID;

    /*
     * Status of the result of transcription
     */
    private String resultStatus;

    /**
     * Get the text property.
     *
     * @return the text value.
     */
    public String getText() {
        return text;
    }

    /**
     * Get the format property.
     *
     * @return the format value.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Get the confidence property.
     *
     * @return the confidence value.
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * Get the duration property.
     *
     * @return the duration value.
     */
    public long getDuration() {
        return duration;
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
     * Get the words property.
     *
     * @return the words value.
     */
    public List<Word> getWords() {
        return words;
    }

    /**
     * Get the participantRawID property.
     *
     * @return the participantRawID value.
     */
    public String getParticipantRawID() {
        return participantRawID;
    }


    /**
     * Get the resultStatus property.
     *
     * @return the resultStatus value.
     */
    public String getResultStatus() {
        return resultStatus;
    }

    /**
     * Reads an instance of TranscriptionDataConverter from the JsonReader.
     *<p>
     * Note: TranscriptionDataConverter does not have to implement JsonSerializable, model is only used in deserialization
     * context internally by {@link StreamingDataParser} and not serialized.
     *</p>
     * @param jsonReader The JsonReader being read.
     * @return An instance of FileSource if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the FileSource.
     */
    public static TranscriptionDataConverter fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final TranscriptionDataConverter converter = new TranscriptionDataConverter();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("text".equals(fieldName)) {
                    converter.text = reader.getString();
                } else if ("format".equals(fieldName)) {
                    converter.format = reader.getString();
                } else if ("confidence".equals(fieldName)) {
                    converter.confidence = reader.getDouble();
                } else if ("offset".equals(fieldName)) {
                    converter.offset = reader.getLong();
                } else if ("duration".equals(fieldName)) {
                    converter.duration = reader.getLong();
                } else if ("words".equals(fieldName)) {
                    converter.words = reader.readArray(Word::fromJson);
                } else if ("participantRawID".equals(fieldName)) {
                    converter.participantRawID = reader.getString();
                } else if ("resultStatus".equals(fieldName)) {
                    converter.resultStatus = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return converter;
        });
    }
}
