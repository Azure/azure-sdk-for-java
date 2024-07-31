// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.util.logging.ClientLogger;

import java.util.List;
import java.time.Duration;

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
     * Duration in ticks. 1 tick = 100 nanoseconds.
     */
    private final long duration;

    /*
     * The result for each word of the phrase
     */
    private final List<WordData> words;

    /*
     * The identified speaker based on participant raw ID
     */
    private final CommunicationIdentifier participant;

    /**
     * Status of the result of transcription
     */
    private final TranscriptionResultState resultState;

    /**
     * The TranscriptionData constructor
     *
     * @param text The display form of the recognized word
     * @param format The format of text
     * @param confidence Confidence of recognition of the whole phrase, from 0.0 (no confidence) to 1.0 (full confidence)
     * @param offset The position of this payload
     * @param duration The Duration in ticks. 1 tick = 100 nanoseconds.
     * @param words The result for each word of the phrase
     * @param participantRawID The identified speaker based on participant raw ID
     * @param resultStatus Status of the result of transcription
     */
    public TranscriptionData(String text, String format, double confidence, long offset, long duration, List<WordData> words, String participantRawID, String resultStatus) {
        this.text = text;
        this.format = convertToTextFormatEnum(format);
        this.confidence = confidence;
        this.offset = offset;
        this.duration = duration;
        this.words = words;
        if (participantRawID != null && !participantRawID.isEmpty()) {
            this.participant = CommunicationIdentifier.fromRawId(participantRawID);
        } else {
            participant = null;
        }
        this.resultState = convertToResultStatusEnum(resultStatus);
    }

    private TranscriptionResultState convertToResultStatusEnum(String resultStatus) {
        if ("Intermediate".equalsIgnoreCase(resultStatus)) {
            return TranscriptionResultState.INTERMEDIATE;
        } else if ("Final".equalsIgnoreCase(resultStatus)) {
            return TranscriptionResultState.FINAL;
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
     * Get the duration property.
     *
     * @return the duration value.
     */
    public Duration getDuration() {
        return Duration.ofNanos(duration * 100);
    }
    /**
     * Get the words property.
     *
     * @return the words value.
     */
    public List<WordData> getWords() {
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
    public TranscriptionResultState getResultStatus() {
        return resultState;
    }
}
