// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.AudioTranscriptionFormat;
import com.azure.ai.openai.models.AudioTranscriptionOptions;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator class for audio transcription.
 */
public final class AudioTranscriptionValidator {
    private static final ClientLogger LOGGER = new ClientLogger(AudioTranscriptionValidator.class);

    /**
     * Validate the audio response format for transcription.
     *
     * @param audioTranscriptionOptions The audio transcription options.
     */
    public static void validateAudioResponseFormatForTranscription(AudioTranscriptionOptions audioTranscriptionOptions) {
        List<AudioTranscriptionFormat> acceptedFormats = new ArrayList<>();
        acceptedFormats.add(AudioTranscriptionFormat.JSON);
        acceptedFormats.add(AudioTranscriptionFormat.VERBOSE_JSON);
        AudioTranscriptionFormat responseFormat = audioTranscriptionOptions.getResponseFormat();
        if (!acceptedFormats.contains(responseFormat)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "This operation does not support the requested audio format: " + responseFormat
                    + ", supported formats: JSON, VERBOSE_JSON."));
        }
    }

    /**
     * Validate the audio response format for transcription text.
     *
     * @param audioTranscriptionOptions The audio transcription options.
     */
    public static void validateAudioResponseFormatForTranscriptionText(AudioTranscriptionOptions audioTranscriptionOptions) {
        List<AudioTranscriptionFormat> acceptedFormats = new ArrayList<>();
        acceptedFormats.add(AudioTranscriptionFormat.TEXT);
        acceptedFormats.add(AudioTranscriptionFormat.VTT);
        acceptedFormats.add(AudioTranscriptionFormat.SRT);
        AudioTranscriptionFormat responseFormat = audioTranscriptionOptions.getResponseFormat();
        if (!acceptedFormats.contains(responseFormat)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "This operation does not support the requested audio format: " + responseFormat
                    + ", supported formats: TEXT, VTT, SRT."));
        }
    }
}
