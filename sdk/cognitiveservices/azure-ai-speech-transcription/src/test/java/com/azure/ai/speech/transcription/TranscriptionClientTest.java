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
 * Tests for TranscriptionClient (synchronous client).
 */
class TranscriptionClientTest extends TranscriptionClientTestBase {

    private final Boolean sync = true; // All tests in this file use the sync client

    /***********************************************************************************
     *
     *                            HAPPY PATH TESTS
     *
     ***********************************************************************************/

    @Test
    public void testTranscribeSyncBasicFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions();

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeSyncWithLanguageFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions().setLocales(Arrays.asList("en-US"));

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeSyncWithMultipleLanguagesFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions().setLocales(Arrays.asList("en-US", "es-ES", "fr-FR"));

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeSyncWithDiarizationFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionDiarizationOptions diarizationOptions
            = new TranscriptionDiarizationOptions().setEnabled(true).setMaxSpeakers(5);

        TranscriptionOptions options = new TranscriptionOptions().setDiarization(diarizationOptions);

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeSyncWithProfanityFilterFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions().setProfanityFilterMode(ProfanityFilterMode.MASKED);

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeSyncWithChannelsFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions().setChannels(Arrays.asList(0));

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeSyncAllOptionsFromFile() {
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
    public void testTranscribeSyncBasicFromFileWithResponse() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions();
        RequestOptions requestOptions
            = new RequestOptions().addHeader(HttpHeaderName.fromString("x-custom-header"), "custom-value");

        doTranscription(methodName, sync, true, audioFile, options, requestOptions);
    }

    @Test
    public void testTranscribeSyncWithAllOptionsFromFileWithResponse() {
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
    public void testTranscribeSyncWithMultipleChannels() {
        // Test with multiple channel indices
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions().setChannels(Arrays.asList(0, 1));

        doTranscription(methodName, sync, false, audioFile, options, null);
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
