// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.AudioTranslationFormat;
import com.azure.ai.openai.models.AudioTranslationOptions;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator class for audio translation.
 */
public class AudioTranslationValidator {
    private static final ClientLogger LOGGER = new ClientLogger(AudioTranslationValidator.class);

    /**
     * Validate the audio response format for translation.
     *
     * @param audioTranslationOptions The audio translation options.
     */
    public static void validateAudioResponseFormatForTranslation(AudioTranslationOptions audioTranslationOptions) {
        List<AudioTranslationFormat> acceptedFormats = new ArrayList<>();
        acceptedFormats.add(AudioTranslationFormat.JSON);
        acceptedFormats.add(AudioTranslationFormat.VERBOSE_JSON);
        AudioTranslationFormat responseFormat = audioTranslationOptions.getResponseFormat();
        if (!acceptedFormats.contains(responseFormat)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "This operation does not support the requested audio format: " + responseFormat
                    + ", supported formats: JSON, VERBOSE_JSON."));
        }
    }

    /**
     * Validate the audio response format for translation text.
     *
     * @param audioTranslationOptions The audio translation options.
     */
    public static void validateAudioResponseFormatForTranslationText(AudioTranslationOptions audioTranslationOptions) {
        List<AudioTranslationFormat> acceptedFormats = new ArrayList<>();
        acceptedFormats.add(AudioTranslationFormat.TEXT);
        acceptedFormats.add(AudioTranslationFormat.VTT);
        acceptedFormats.add(AudioTranslationFormat.SRT);
        AudioTranslationFormat responseFormat = audioTranslationOptions.getResponseFormat();
        if (!acceptedFormats.contains(responseFormat)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "This operation does not support the requested audio format: " + responseFormat
                    + ", supported formats: TEXT, VTT, SRT."));
        }
    }
}
