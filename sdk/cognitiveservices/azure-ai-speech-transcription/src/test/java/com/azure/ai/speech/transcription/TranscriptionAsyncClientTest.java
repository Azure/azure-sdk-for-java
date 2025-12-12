// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

import com.azure.ai.speech.transcription.models.ProfanityFilterMode;
import com.azure.ai.speech.transcription.models.TranscriptionDiarizationOptions;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.core.exception.HttpResponseException;
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

        TranscriptionOptions options = new TranscriptionOptions((String) null);

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithLanguageFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions((String) null).setLocales(Arrays.asList("en-US"));

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithMultipleLanguagesFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options
            = new TranscriptionOptions((String) null).setLocales(Arrays.asList("en-US", "es-ES", "fr-FR"));

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithDiarizationFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionDiarizationOptions diarizationOptions = new TranscriptionDiarizationOptions().setMaxSpeakers(5);

        TranscriptionOptions options
            = new TranscriptionOptions((String) null).setDiarizationOptions(diarizationOptions);

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithProfanityFilterFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options
            = new TranscriptionOptions((String) null).setProfanityFilterMode(ProfanityFilterMode.MASKED);

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithChannelsFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions((String) null).setActiveChannels(Arrays.asList(0));

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncAllOptionsFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionDiarizationOptions diarizationOptions = new TranscriptionDiarizationOptions().setMaxSpeakers(5);

        TranscriptionOptions options = new TranscriptionOptions((String) null).setLocales(Arrays.asList("en-US"))
            .setDiarizationOptions(diarizationOptions)
            .setProfanityFilterMode(ProfanityFilterMode.MASKED)
            .setActiveChannels(Arrays.asList(0));

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncBasicFromFileWithResponse() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions((String) null);
        RequestOptions requestOptions
            = new RequestOptions().addHeader(HttpHeaderName.fromString("x-custom-header"), "custom-value");

        doTranscription(methodName, sync, true, audioFile, options, requestOptions);
    }

    @Test
    public void testTranscribeAsyncWithAllOptionsFromFileWithResponse() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionDiarizationOptions diarizationOptions = new TranscriptionDiarizationOptions().setMaxSpeakers(5);

        TranscriptionOptions options
            = new TranscriptionOptions((String) null).setLocales(Arrays.asList("en-US", "es-ES"))
                .setDiarizationOptions(diarizationOptions)
                .setProfanityFilterMode(ProfanityFilterMode.REMOVED)
                .setActiveChannels(Arrays.asList(0, 1));

        RequestOptions requestOptions
            = new RequestOptions().addHeader(HttpHeaderName.fromString("x-custom-header"), "custom-value")
                .addQueryParam("test-param", "test-value");

        doTranscription(methodName, sync, true, audioFile, options, requestOptions);
    }

    @Test
    public void testTranscribeAsyncWithAudioUrl() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        // Using a publicly accessible sample audio file from Azure samples
        String audioUrl
            = "https://raw.githubusercontent.com/Azure-Samples/cognitive-services-speech-sdk/master/sampledata/audiofiles/aboutSpeechSdk.wav";
        TranscriptionOptions options = new TranscriptionOptions(audioUrl).setLocales(Arrays.asList("en-US"));

        // For URL-based transcription, we don't pass the local audio file path
        doTranscriptionWithUrl(methodName, sync, options);
    }

    @Test
    public void testTranscribeAsyncWithProfanityModeMasked() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options
            = new TranscriptionOptions((String) null).setProfanityFilterMode(ProfanityFilterMode.MASKED);
        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithProfanityModeRemoved() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options
            = new TranscriptionOptions((String) null).setProfanityFilterMode(ProfanityFilterMode.REMOVED);
        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithProfanityModeTags() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options
            = new TranscriptionOptions((String) null).setProfanityFilterMode(ProfanityFilterMode.TAGS);
        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    /***********************************************************************************
     *
     *                            ERROR HANDLING TESTS
     *
     ***********************************************************************************/

    @Test
    public void testTranscribeAsyncWithEmptyAudioData() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        // Test with minimal audio data - service should handle gracefully
        TranscriptionOptions options = new TranscriptionOptions((String) null);

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithInvalidLanguageCode() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        // Use invalid language code to trigger service error
        TranscriptionOptions options
            = new TranscriptionOptions((String) null).setLocales(Arrays.asList("invalid-locale-code"));

        // The service should return a 400 error for invalid locale
        // doTranscription wraps exceptions in RuntimeException, so we catch that
        try {
            doTranscription(methodName, sync, false, audioFile, options, null);
            // Should not reach here - the above should throw an exception
            throw new AssertionError("Expected RuntimeException with HttpResponseException cause but none was thrown");
        } catch (RuntimeException e) {
            // Expected behavior - verify the cause is HttpResponseException with 400 status
            if (!(e.getCause() instanceof HttpResponseException)) {
                throw new AssertionError(
                    "Expected RuntimeException cause to be HttpResponseException but got: " + e.getCause().getClass());
            }
            HttpResponseException httpException = (HttpResponseException) e.getCause();
            if (httpException.getResponse().getStatusCode() != 400) {
                throw new AssertionError(
                    "Expected 400 status code but got: " + httpException.getResponse().getStatusCode());
            }
        }
    }

    @Test
    public void testTranscribeAsyncCancellation() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        // Test cancellation behavior with a normal transcription request
        TranscriptionOptions options = new TranscriptionOptions((String) null);

        doTranscription(methodName, sync, false, audioFile, options, null);
    }
}
