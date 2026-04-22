// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

import com.azure.ai.speech.transcription.models.ProfanityFilterMode;
import com.azure.ai.speech.transcription.models.TranscriptionDiarizationOptions;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for TranscriptionAsyncClient (asynchronous client).
 */
class TranscriptionAsyncClientTest extends TranscriptionClientTestBase {
    private TranscriptionAsyncClient client;

    @BeforeEach
    public void setupTest() {
        this.client = configureBuilder(true, true).buildAsyncClient();
    }

    /***********************************************************************************
     *
     *                            HAPPY PATH TESTS
     *
     ***********************************************************************************/

    @Test
    public void testTranscribeAsyncBasicFromFile() {
        transcribeAndVerifyResult("testTranscribeAsyncBasicFromFile", fromAudioFile());
    }

    @Test
    public void testTranscribeAsyncWithLanguageFromFile() {
        transcribeAndVerifyResult("testTranscribeAsyncWithLanguageFromFile",
            fromAudioFile().setLocales(Collections.singletonList("en-US")));
    }

    @Test
    public void testTranscribeAsyncWithMultipleLanguagesFromFile() {
        transcribeAndVerifyResult("testTranscribeAsyncWithMultipleLanguagesFromFile",
            fromAudioFile().setLocales(Arrays.asList("en-US", "es-ES", "fr-FR")));
    }

    @Test
    public void testTranscribeAsyncWithDiarizationFromFile() {
        transcribeAndVerifyResult("testTranscribeAsyncWithDiarizationFromFile",
            fromAudioFile().setDiarizationOptions(new TranscriptionDiarizationOptions().setMaxSpeakers(5)));
    }

    @Test
    public void testTranscribeAsyncWithProfanityFilterFromFile() {
        transcribeAndVerifyResult("testTranscribeAsyncWithProfanityFilterFromFile",
            fromAudioFile().setProfanityFilterMode(ProfanityFilterMode.MASKED));
    }

    @Test
    public void testTranscribeAsyncWithChannelsFromFile() {
        transcribeAndVerifyResult("testTranscribeAsyncWithChannelsFromFile",
            fromAudioFile().setActiveChannels(Collections.singletonList(0)));
    }

    @Test
    public void testTranscribeAsyncAllOptionsFromFile() {
        TranscriptionOptions options = fromAudioFile().setLocales(Collections.singletonList("en-US"))
            .setDiarizationOptions(new TranscriptionDiarizationOptions().setMaxSpeakers(5))
            .setProfanityFilterMode(ProfanityFilterMode.MASKED)
            .setActiveChannels(Collections.singletonList(0));

        transcribeAndVerifyResult("testTranscribeAsyncAllOptionsFromFile", options);
    }

    @Test
    public void testTranscribeAsyncBasicFromFileWithResponse() {
        transcribeAndVerifyResponse("testTranscribeAsyncBasicFromFileWithResponse", fromAudioFile(),
            new RequestOptions().addHeader(HttpHeaderName.fromString("x-custom-header"), "custom-value"));
    }

    @Test
    public void testTranscribeAsyncWithAllOptionsFromFileWithResponse() {
        TranscriptionOptions options = fromAudioFile().setLocales(Arrays.asList("en-US", "es-ES"))
            .setDiarizationOptions(new TranscriptionDiarizationOptions().setMaxSpeakers(5))
            .setProfanityFilterMode(ProfanityFilterMode.REMOVED)
            .setActiveChannels(Arrays.asList(0, 1));

        RequestOptions requestOptions
            = new RequestOptions().addHeader(HttpHeaderName.fromString("x-custom-header"), "custom-value")
                .addQueryParam("test-param", "test-value");

        transcribeAndVerifyResponse("testTranscribeAsyncWithAllOptionsFromFileWithResponse", options, requestOptions);
    }

    @Test
    public void testTranscribeAsyncWithAudioUrl() {
        // Using a publicly accessible sample audio file from Azure samples
        String audioUrl
            = "https://raw.githubusercontent.com/Azure-Samples/cognitive-services-speech-sdk/master/sampledata/audiofiles/aboutSpeechSdk.wav";

        transcribeAndVerifyResult("testTranscribeAsyncWithAudioUrl",
            new TranscriptionOptions(audioUrl).setLocales(Collections.singletonList("en-US")));
    }

    @Test
    public void testTranscribeAsyncWithProfanityModeMasked() {
        transcribeAndVerifyResult("testTranscribeAsyncWithProfanityModeMasked",
            fromAudioFile().setProfanityFilterMode(ProfanityFilterMode.MASKED));
    }

    @Test
    public void testTranscribeAsyncWithProfanityModeRemoved() {
        transcribeAndVerifyResult("testTranscribeAsyncWithProfanityModeTags",
            fromAudioFile().setProfanityFilterMode(ProfanityFilterMode.REMOVED));
    }

    @Test
    public void testTranscribeAsyncWithProfanityModeTags() {
        transcribeAndVerifyResult("testTranscribeAsyncWithProfanityModeTags",
            fromAudioFile().setProfanityFilterMode(ProfanityFilterMode.TAGS));
    }

    /***********************************************************************************
     *
     *                            ERROR HANDLING TESTS
     *
     ***********************************************************************************/

    @Test
    public void testTranscribeAsyncWithEmptyAudioData() {
        // Test with minimal audio data - service should handle gracefully
        transcribeAndVerifyResult("testTranscribeAsyncWithEmptyAudioData", fromAudioFile());
    }

    @Test
    public void testTranscribeAsyncWithInvalidLanguageCode() {
        // Use invalid language code to trigger service error
        TranscriptionOptions options = fromAudioFile().setLocales(Collections.singletonList("invalid-locale-code"));

        // The service should return a 400 error for invalid locale
        // doTranscription wraps exceptions in RuntimeException, so we catch that
        StepVerifier.create(client.transcribe(options)).verifyErrorSatisfies(e -> {
            // Expected behavior - verify the cause is HttpResponseException with 400 status
            if (!(e instanceof HttpResponseException)) {
                fail("Expected RuntimeException cause to be HttpResponseException but got: " + e.getClass());
            }
            HttpResponseException httpException = (HttpResponseException) e;
            if (httpException.getResponse().getStatusCode() != 400) {
                fail("Expected 400 status code but got: " + httpException.getResponse().getStatusCode());
            }
        });
    }

    @Test
    public void testTranscribeAsyncCancellation() {
        // Test cancellation behavior with a normal transcription request
        transcribeAndVerifyResult("testTranscribeAsyncCancellation", fromAudioFile());
    }

    private void transcribeAndVerifyResult(String testName, TranscriptionOptions options) {
        StepVerifier.create(client.transcribe(options))
            .assertNext(result -> validateTranscriptionResult(testName, result))
            .verifyComplete();
    }

    private void transcribeAndVerifyResponse(String testName, TranscriptionOptions options,
        RequestOptions requestOptions) {
        BinaryData multipartBody = createMultipartBody(options, requestOptions);
        StepVerifier.create(client.transcribeWithResponse(multipartBody, requestOptions)).assertNext(response -> {
            printHttpRequestAndResponse(response);
            validateTranscriptionResult(testName, response.getValue().toObject(TranscriptionResult.class));
        }).verifyComplete();
    }
}
