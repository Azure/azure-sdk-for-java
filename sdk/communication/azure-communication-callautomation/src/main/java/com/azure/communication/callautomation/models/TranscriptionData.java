// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.TranscriptionDataContructorProxy;
import com.azure.communication.callautomation.implementation.converters.TranscriptionDataConverter;
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
    private final Double confidence;

    /*
     * The position of this payload
     */
    private final Long offset;

    /*
     * Duration in ticks. 1 tick = 100 nanoseconds.
     */
    private final Long duration;

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
     * 
     */
    static {
        TranscriptionDataContructorProxy
            .setAccessor(new TranscriptionDataContructorProxy.TranscriptionDataContructorProxyAccessor() {
                @Override
                public TranscriptionData create(TranscriptionDataConverter internalData) {
                    return new TranscriptionData(internalData);
                }
            });
    }

    /**
     * The TranscriptionData constructor
     *
     * @param internalData transcription internal data
     */
    TranscriptionData(TranscriptionDataConverter internalData) {
        this.text = internalData.getText();
        this.format = convertToTextFormatEnum(internalData.getFormat());
        this.confidence = internalData.getConfidence();
        this.offset = internalData.getOffset();
        this.duration = internalData.getDuration();
        this.words = internalData.getWords();
        if (internalData.getParticipantRawID() != null && !internalData.getParticipantRawID().isEmpty()) {
            this.participant = CommunicationIdentifier.fromRawId(internalData.getParticipantRawID());
        } else {
            this.participant = null;
        }

        this.resultState = convertToResultStatusEnum(internalData.getResultStatus());
    }

    /**
     * Create instance of transcription data
     */
    public TranscriptionData() {
        this.text = null;
        this.format = null;
        this.confidence = null;
        this.offset = null;
        this.duration = null;
        this.words = null;
        this.participant = null;
        this.resultState = null;
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
    public List<WordData> getTranscribedWords() {
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
     * Get the resultState property.
     *
     * @return the resultState value.
     */
    public TranscriptionResultState getResultState() {
        return resultState;
    }
}
