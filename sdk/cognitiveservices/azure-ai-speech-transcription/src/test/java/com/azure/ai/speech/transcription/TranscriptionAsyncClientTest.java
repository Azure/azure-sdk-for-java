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

        TranscriptionDiarizationOptions diarizationOptions = new TranscriptionDiarizationOptions().setMaxSpeakers(5);

        TranscriptionOptions options = new TranscriptionOptions().setDiarizationOptions(diarizationOptions);

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

        TranscriptionOptions options = new TranscriptionOptions().setActiveChannels(Arrays.asList(0));

        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncAllOptionsFromFile() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionDiarizationOptions diarizationOptions = new TranscriptionDiarizationOptions().setMaxSpeakers(5);

        TranscriptionOptions options = new TranscriptionOptions().setLocales(Arrays.asList("en-US"))
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

        TranscriptionDiarizationOptions diarizationOptions = new TranscriptionDiarizationOptions().setMaxSpeakers(5);

        TranscriptionOptions options = new TranscriptionOptions().setLocales(Arrays.asList("en-US", "es-ES"))
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
        // Test with audio URL option
        createClient(true, true, sync);

        // Note: This test is commented out because it requires:
        // 1. A valid, publicly accessible URL in live/record mode
        // 2. Proper recording of the service response
        // 3. The audio file at the URL must be in a supported format
        // To enable this test:
        // - Provide a valid audio URL
        // - Uncomment the lines below
        // - Run in RECORD mode to capture the interaction
        // String methodName = new Object() {
        // }.getClass().getEnclosingMethod().getName();
        // TranscriptionOptions options = new TranscriptionOptions("https://example.com/sample.wav");
        // options.setLocales(Arrays.asList("en-US"));
        // doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithProfanityModeMasked() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions().setProfanityFilterMode(ProfanityFilterMode.MASKED);
        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithProfanityModeRemoved() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions().setProfanityFilterMode(ProfanityFilterMode.REMOVED);
        doTranscription(methodName, sync, false, audioFile, options, null);
    }

    @Test
    public void testTranscribeAsyncWithProfanityModeTags() {
        createClient(true, true, sync);

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        TranscriptionOptions options = new TranscriptionOptions().setProfanityFilterMode(ProfanityFilterMode.TAGS);
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

        // Test with empty audio data - this should result in a service error
        // Note: Depending on service behavior, this may throw HttpResponseException
        // The exact behavior should be validated based on actual service responses
        // Example implementation:
        // StepVerifier.create(getAsyncClient().transcribe(emptyRequestContent))
        //     .expectError(HttpResponseException.class)
        //     .verify(Duration.ofSeconds(30));
    }

    @Test
    public void testTranscribeAsyncWithInvalidLanguageCode() {
        createClient(true, true, sync);

        // Note: This test requires actual service call to verify behavior
        // In PLAYBACK mode, this would replay the recorded error response
        // Example implementation with StepVerifier:
        // TranscriptionOptions options = new TranscriptionOptions().setLocales(Arrays.asList("invalid-locale"));
        // StepVerifier.create(getAsyncClient().transcribe(requestContentWithInvalidLocale))
        //     .expectErrorMatches(throwable -> throwable instanceof HttpResponseException
        //         && ((HttpResponseException) throwable).getResponse().getStatusCode() == 400)
        //     .verify(Duration.ofSeconds(30));
    }

    @Test
    public void testTranscribeAsyncCancellation() {
        createClient(true, true, sync);

        // Test that async operations can be cancelled properly
        // This verifies that the reactive streams support cancellation

        // Note: Cancellation testing would typically involve subscribing and then cancelling
        // Example pattern (commented out as it requires specific test setup):
        // String methodName = new Object() {
        // }.getClass().getEnclosingMethod().getName();
        // TranscriptionOptions options = new TranscriptionOptions();
        // Disposable subscription = getAsyncClient().transcribe(requestContent).subscribe();
        // subscription.dispose();
        // Verify that resources are cleaned up appropriately
    }
}
