// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

import com.azure.ai.speech.transcription.models.ProfanityFilterMode;
import com.azure.ai.speech.transcription.models.TranscriptionDiarizationOptions;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Tests for TranscriptionAsyncClient (asynchronous client).
 */
class TranscriptionAsyncClientTest extends TranscriptionClientTestBase {

    private final Boolean sync = false; // All tests in this file use the async client

    /***********************************************************************************
     *
     *                            HAPPY PATH TESTS
     *
     ***********************************************************************************/

    @Test
    public void testTranscribeAsyncBasicFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions();

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithLanguageFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions().setLocales(Arrays.asList("en-US"));

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithMultipleLanguagesFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions().setLocales(Arrays.asList("en-US", "es-ES", "fr-FR"));

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithDiarizationFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionDiarizationOptions diarizationOptions
            = new TranscriptionDiarizationOptions().setEnabled(true).setMaxSpeakers(5);

        TranscriptionOptions options = new TranscriptionOptions().setDiarization(diarizationOptions);

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithProfanityFilterFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions().setProfanityFilterMode(ProfanityFilterMode.MASKED);

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithChannelsFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions().setChannels(Arrays.asList(0));

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncAllOptionsFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionDiarizationOptions diarizationOptions
            = new TranscriptionDiarizationOptions().setEnabled(true).setMaxSpeakers(5);

        TranscriptionOptions options = new TranscriptionOptions().setLocales(Arrays.asList("en-US"))
            .setDiarization(diarizationOptions)
            .setProfanityFilterMode(ProfanityFilterMode.MASKED)
            .setChannels(Arrays.asList(0));

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncBasicFromFileWithResponse() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions();
        RequestOptions requestOptions
            = new RequestOptions().addHeader(HttpHeaderName.fromString("x-custom-header"), "custom-value");

        doTranscription(methodName, sync, true, audioFile, options, requestOptions);
    }

    @Test
    public void testTranscribeAsyncWithAllOptionsFromFileWithResponse() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionDiarizationOptions diarizationOptions
            = new TranscriptionDiarizationOptions().setEnabled(true).setMaxSpeakers(5);

        TranscriptionOptions options = new TranscriptionOptions().setLocales(Arrays.asList("en-US", "es-ES"))
            .setDiarization(diarizationOptions)
            .setProfanityFilterMode(ProfanityFilterMode.REMOVED)
            .setChannels(Arrays.asList(0, 1));

        RequestOptions requestOptions
            = new RequestOptions().addHeader(HttpHeaderName.fromString("x-custom-header"), "custom-value")
                .addQueryParam("test-param", "test-value");

        doTranscription(methodName, sync, true, audioFile, options, requestOptions);
    }

    @Test
    public void testTranscribeAsyncWithAudioUrl() {
        // Test with audio URL option
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions().setLocales(Arrays.asList("en-US"))
            .setAudioUrl("https://example.com/sample.wav");

        // Note: This test requires a valid URL in live mode
        // doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithDifferentProfanityModes() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        // Test with MASKED mode
        TranscriptionOptions optionsMasked
            = new TranscriptionOptions().setProfanityFilterMode(ProfanityFilterMode.MASKED);
        doTranscription(methodName + ":Masked", sync, false, audioFile, optionsMasked, null);

        // Test with REMOVED mode
        TranscriptionOptions optionsRemoved
            = new TranscriptionOptions().setProfanityFilterMode(ProfanityFilterMode.REMOVED);
        doTranscription(methodName + ":Removed", sync, false, audioFile, optionsRemoved, null);

        // Test with TAGS mode
        TranscriptionOptions optionsTags = new TranscriptionOptions().setProfanityFilterMode(ProfanityFilterMode.TAGS);
        doTranscription(methodName + ":Tags", sync, false, audioFile, optionsTags, null);
    }

    /***********************************************************************************
     *
     *                            ERROR HANDLING TESTS
     *
     ***********************************************************************************/

    // Note: Error handling tests would go here
    // Examples:
    // - Test with invalid audio format
    // - Test with missing required parameters
    // - Test with unsupported language code
    // - Test with invalid diarization settings
    // These would require specific test scenarios and expected exception handling
}
