// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import com.azure.communication.callautomation.models.streaming.transcription.Word;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The TranscriptionDataInternal model.
 */
public final class TranscriptionDataConverter {

    /*
     * The display form of the recognized word
     */
    @JsonProperty(value = "text")
    private String text;

    /*
     * The format of text
     */
    @JsonProperty(value = "format")
    private String format;

    /*
     * Confidence of recognition of the whole phrase, from 0.0 (no confidence) to 1.0 (full confidence)
     */
    @JsonProperty(value = "confidence")
    private double confidence;

    /*
     * The position of this payload
     */
    @JsonProperty(value = "offset")
    private long offset;

    /*
     * TThe result for each word of the phrase
     */
    @JsonProperty(value = "words")
    private List<Word> words;

    /*
     * The participantId.
     */
    @JsonProperty(value = "participantRawID")
    private String participantRawID;

    /*
     * Status of the result of transcription
     */
    @JsonProperty(value = "resultStatus")
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
}
