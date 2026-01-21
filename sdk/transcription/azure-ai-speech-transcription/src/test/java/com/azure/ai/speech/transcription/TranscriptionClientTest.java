// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

import com.azure.ai.speech.transcription.models.ProfanityFilterMode;
import com.azure.ai.speech.transcription.models.TranscriptionDiarizationOptions;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for TranscriptionClient (synchronous client).
 */
class TranscriptionClientTest extends TranscriptionClientTestBase {
    private TranscriptionClient client;

    @BeforeEach
    public void setupTest() {
        this.client = configureBuilder(true, true).buildClient();
    }

    /***********************************************************************************
     *
     *                            HAPPY PATH TESTS
     *
     ***********************************************************************************/

    @Test
    public void testTranscribeSyncBasicFromFile() {
        transcribeAndVerifyResult("testTranscribeSyncBasicFromFile", fromAudioFile());
    }

    @Test
    public void testTranscribeSyncWithLanguageFromFile() {
        transcribeAndVerifyResult("testTranscribeSyncWithLanguageFromFile",
            fromAudioFile().setLocales(Collections.singletonList("en-US")));
    }

    @Test
    public void testTranscribeSyncWithMultipleLanguagesFromFile() {
        transcribeAndVerifyResult("testTranscribeSyncWithMultipleLanguagesFromFile",
            fromAudioFile().setLocales(Arrays.asList("en-US", "es-ES", "fr-FR")));
    }

    @Test
    public void testTranscribeSyncWithDiarizationFromFile() {
        transcribeAndVerifyResult("testTranscribeSyncWithDiarizationFromFile",
            fromAudioFile().setDiarizationOptions(new TranscriptionDiarizationOptions().setMaxSpeakers(5)));
    }

    @Test
    public void testTranscribeSyncWithProfanityFilterFromFile() {
        transcribeAndVerifyResult("testTranscribeSyncWithProfanityFilterFromFile",
            fromAudioFile().setProfanityFilterMode(ProfanityFilterMode.MASKED));
    }

    @Test
    public void testTranscribeSyncWithChannelsFromFile() {
        transcribeAndVerifyResult("testTranscribeSyncWithChannelsFromFile",
            fromAudioFile().setActiveChannels(Collections.singletonList(0)));
    }

    @Test
    public void testTranscribeSyncAllOptionsFromFile() {
        TranscriptionOptions options = fromAudioFile().setLocales(Collections.singletonList("en-US"))
            .setDiarizationOptions(new TranscriptionDiarizationOptions().setMaxSpeakers(5))
            .setProfanityFilterMode(ProfanityFilterMode.MASKED)
            .setActiveChannels(Collections.singletonList(0));

        transcribeAndVerifyResult("testTranscribeSyncAllOptionsFromFile", options);
    }

    @Test
    public void testTranscribeSyncBasicFromFileWithResponse() {
        transcribeAndVerifyResponse("testTranscribeSyncBasicFromFileWithResponse", fromAudioFile(),
            new RequestOptions().addHeader(HttpHeaderName.fromString("x-custom-header"), "custom-value"));
    }

    @Test
    public void testTranscribeSyncWithAllOptionsFromFileWithResponse() {
        TranscriptionOptions options = fromAudioFile().setLocales(Arrays.asList("en-US", "es-ES"))
            .setDiarizationOptions(new TranscriptionDiarizationOptions().setMaxSpeakers(5))
            .setProfanityFilterMode(ProfanityFilterMode.REMOVED)
            .setActiveChannels(Arrays.asList(0, 1));

        RequestOptions requestOptions
            = new RequestOptions().addHeader(HttpHeaderName.fromString("x-custom-header"), "custom-value")
                .addQueryParam("test-param", "test-value");

        transcribeAndVerifyResponse("testTranscribeSyncWithAllOptionsFromFileWithResponse", options, requestOptions);
    }

    @Test
    public void testTranscribeSyncWithMultipleChannels() {
        // Test with multiple channel indices
        transcribeAndVerifyResult("testTranscribeSyncWithMultipleChannels",
            fromAudioFile().setActiveChannels(Arrays.asList(0, 1)));
    }

    @Test
    public void testTranscribeSyncWithAudioUrl() {
        // Using a publicly accessible sample audio file from Azure samples
        String audioUrl
            = "https://raw.githubusercontent.com/Azure-Samples/cognitive-services-speech-sdk/master/sampledata/audiofiles/aboutSpeechSdk.wav";

        transcribeAndVerifyResult("testTranscribeSyncWithAudioUrl",
            new TranscriptionOptions(audioUrl).setLocales(Collections.singletonList("en-US")));
    }

    /***********************************************************************************
     *
     *                            ERROR HANDLING TESTS
     *
     ***********************************************************************************/

    @Test
    public void testTranscribeSyncWithNullOptions() {
        // Test that null options throws appropriate exception
        assertThrows(NullPointerException.class, () -> client.transcribe((TranscriptionOptions) null),
            "Transcribe should throw NullPointerException when options is null");
    }

    @Test
    public void testTranscribeSyncWithEmptyAudioData() {
        // Test with empty audio data - this should result in a service error
        // Note: Depending on service behavior, this may throw HttpResponseException
        // The exact behavior should be validated based on actual service responses
    }

    @Test
    public void testTranscribeSyncWithInvalidLanguageCode() {
        // Note: This test requires actual service call to verify behavior
        // In PLAYBACK mode, this would replay the recorded error response
        // Example implementation:
        // TranscriptionOptions options = new TranscriptionOptions((String) null)
        //     .setLocales(Arrays.asList("invalid-locale"));
        // doTranscription(methodName, sync, false, audioFile, options, null);
        // The service should return an error for invalid locale
    }

    private void transcribeAndVerifyResult(String testName, TranscriptionOptions options) {
        validateTranscriptionResult(testName, client.transcribe(options));
    }

    private void transcribeAndVerifyResponse(String testName, TranscriptionOptions options,
        RequestOptions requestOptions) {
        BinaryData multipartBody = createMultipartBody(options, requestOptions);
        Response<BinaryData> response = client.transcribeWithResponse(multipartBody, requestOptions);
        printHttpRequestAndResponse(response);
        validateTranscriptionResult(testName, response.getValue().toObject(TranscriptionResult.class));
    }
}
