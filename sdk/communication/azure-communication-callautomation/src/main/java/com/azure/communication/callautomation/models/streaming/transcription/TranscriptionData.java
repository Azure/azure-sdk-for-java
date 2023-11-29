// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.streaming.transcription;

import com.azure.communication.callautomation.models.streaming.StreamingData;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.util.logging.ClientLogger;

import java.util.List;

/**
 * The TranscriptionData model.
 */
public final class TranscriptionData extends StreamingData {

    private static final ClientLogger LOGGER = new ClientLogger(TranscriptionData.class);

    /*
     * The display form of the recognized word
     */
    private final String text;

    /*
     * The format of text
     */
    private final TextFormat format;

    /*
     * Confidence of recognition of the whole phrase, from 0.0 (no confidence) to 1.0 (full confidence)
     */
    private final double confidence;

    /*
     * The position of this payload
     */
    private final long offset;

    /*
     * The result for each word of the phrase
     */
    private final List<Word> words;

    /*
     * The identified speaker based on participant raw ID
     */
    private final CommunicationIdentifier participant;

    /**
     * Status of the result of transcription
     */
    private final ResultStatus resultStatus;

    /**
     * The TranscriptionData constructor
     *
     * @param text The display form of the recognized word
     * @param format The format of text
     * @param confidence Confidence of recognition of the whole phrase, from 0.0 (no confidence) to 1.0 (full confidence)
     * @param offset The position of this payload
     * @param words The result for each word of the phrase
     * @param participantRawID The identified speaker based on participant raw ID
     * @param resultStatus Status of the result of transcription
     */
    public TranscriptionData(String text, String format, double confidence, long offset, List<Word> words, String participantRawID, String resultStatus) {
        this.text = text;
        this.format = convertToTextFormatEnum(format);
        this.confidence = confidence;
        this.offset = offset;
        this.words = words;
        if (participantRawID != null && !participantRawID.isEmpty()) {
            this.participant = new CommunicationUserIdentifier(participantRawID);
        } else {
            participant = null;
        }
        this.resultStatus = convertToResultStatusEnum(resultStatus);
    }

    private ResultStatus convertToResultStatusEnum(String resultStatus) {
        if ("Intermediate".equalsIgnoreCase(resultStatus)) {
            return ResultStatus.INTERMEDIATE;
        } else if ("Final".equalsIgnoreCase(resultStatus)) {
            return ResultStatus.FINAL;
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(resultStatus));

        }
    }

    private TextFormat convertToTextFormatEnum(String format) {
        if ("Display".equalsIgnoreCase(format)) {
            return TextFormat.DISPLAY;
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(format));
        }
    }

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
    public TextFormat getFormat() {
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
    public Long getOffset() {
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
    public CommunicationIdentifier getParticipant() {
        return participant;
    }

    /**
     * Get the resultStatus property.
     *
     * @return the resultStatus value.
     */
    public ResultStatus getResultStatus() {
        return resultStatus;
    }
}
