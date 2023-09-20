// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.AudioTranscriptionFormat;
import com.azure.ai.openai.models.AudioTranscriptionOptions;
import com.azure.ai.openai.models.AudioTranslationOptions;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility method class.
 */
public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);

    private Utility() {
    }

    /**
     * Get the request options for multipart form data.
     *
     * @param requestOptions The request options.
     * @param result The multipart data serialization result.
     * @param multipartBoundary The multipart boundary.
     * @return The request options.
     */
    public static RequestOptions getRequestOptionsForMultipartFormData(RequestOptions requestOptions,
        MultipartDataSerializationResult result, String multipartBoundary) {
        if (requestOptions == null) {
            requestOptions =
                new RequestOptions()
                    .setHeader(
                        HttpHeaderName.CONTENT_TYPE,
                        "multipart/form-data;" + " boundary=" + multipartBoundary)
                    .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(result.getDataLength()));
        }
        return requestOptions;
    }

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

    /**
     * Validate the audio response format for translation.
     *
     * @param audioTranslationOptions The audio translation options.
     */
    public static void validateAudioResponseFormatForTranslation(AudioTranslationOptions audioTranslationOptions) {
        List<AudioTranscriptionFormat> acceptedFormats = new ArrayList<>();
        acceptedFormats.add(AudioTranscriptionFormat.JSON);
        acceptedFormats.add(AudioTranscriptionFormat.VERBOSE_JSON);
        AudioTranscriptionFormat responseFormat = audioTranslationOptions.getResponseFormat();
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
        List<AudioTranscriptionFormat> acceptedFormats = new ArrayList<>();
        acceptedFormats.add(AudioTranscriptionFormat.TEXT);
        acceptedFormats.add(AudioTranscriptionFormat.VTT);
        acceptedFormats.add(AudioTranscriptionFormat.SRT);
        AudioTranscriptionFormat responseFormat = audioTranslationOptions.getResponseFormat();
        if (!acceptedFormats.contains(responseFormat)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "This operation does not support the requested audio format: " + responseFormat
                    + ", supported formats: TEXT, VTT, SRT."));
        }
    }
}
