// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

import com.azure.ai.speech.transcription.implementation.MultipartFormDataHelper;
import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base class for TranscriptionClient tests. Contains helper methods and common test infrastructure.
 * Supports both API Key (KeyCredential) and Entra ID (TokenCredential) authentication.
 */
class TranscriptionClientTestBase extends TestProxyTestBase {
    private static final boolean PRINT_RESULTS = false; // Set to true to print results to console window

    private static final String SAMPLE_WAV_FILE_NAME = "sample.wav";
    private static final byte[] SAMPLE_WAV;

    static {
        try {
            SAMPLE_WAV = Files.readAllBytes(Paths
                .get(TranscriptionClientTestBase.class.getClassLoader().getResource(SAMPLE_WAV_FILE_NAME).toURI()));
        } catch (URISyntaxException | IOException ex) {
            throw new RuntimeException("Failed to load audio file for testing.", ex);
        }
    }

    /**
     * Configures a {@link TranscriptionClientBuilder} that will be used to create the specific sync or async client for
     * testing.
     *
     * @param useKeyAuth Whether to use key-based authentication (true) or token-based authentication (false)
     * @param useRealKey Whether to use a real key from environment variables (true) or a fake key (false).
     *                   Only applies when useKeyAuth is true.
     */
    protected TranscriptionClientBuilder configureBuilder(boolean useKeyAuth, boolean useRealKey) {
        // Define endpoint and auth credentials
        String endpoint = "https://fake-resource-name.cognitiveservices.azure.com";
        String key = "00000000000000000000000000000000";

        if (!interceptorManager.isPlaybackMode()) {
            endpoint = Configuration.getGlobalConfiguration().get("SPEECH_ENDPOINT");
            assertTrue(endpoint != null && !endpoint.isEmpty(), "Endpoint URL is required to run live tests.");

            if (useKeyAuth && useRealKey) {
                key = Configuration.getGlobalConfiguration().get("SPEECH_API_KEY");
                assertTrue(key != null && !key.isEmpty(), "API key is required to run live tests with KeyCredential.");
            }
        }

        // Create the client builder
        TranscriptionClientBuilder transcriptionClientBuilder = new TranscriptionClientBuilder().endpoint(endpoint)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Update the client builder with credentials and recording/playback policies
        if (interceptorManager.isLiveMode()) {
            if (useKeyAuth) {
                transcriptionClientBuilder.credential(new KeyCredential(key));
            } else {
                // Use Entra ID authentication (TokenCredential)
                TokenCredential credential = new DefaultAzureCredentialBuilder().build();
                transcriptionClientBuilder.credential(credential);
            }
        } else if (interceptorManager.isRecordMode()) {
            transcriptionClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
            if (useKeyAuth) {
                transcriptionClientBuilder.credential(new KeyCredential(key));
            } else {
                TokenCredential credential = new DefaultAzureCredentialBuilder().build();
                transcriptionClientBuilder.credential(credential);
            }
        } else if (interceptorManager.isPlaybackMode()) {
            transcriptionClientBuilder.httpClient(interceptorManager.getPlaybackClient());
            // In playback mode, use a fake key regardless of authentication method
            transcriptionClientBuilder.credential(new KeyCredential(key));
        }

        // Configure sanitizers - must be done after registering the record policy or playback client
        if (!interceptorManager.isLiveMode()) {
            // Remove default sanitizers that would interfere with Speech service recordings:
            // - AZSDK3430 (id sanitizer): Preserve resource identifiers needed for request matching
            // - AZSDK3493 (name sanitizer): Preserve resource names needed for request matching
            // - AZSDK2003, AZSDK2030: URI-related sanitizers that may affect Speech endpoints
            interceptorManager.removeSanitizers("AZSDK2003", "AZSDK2030", "AZSDK3430", "AZSDK3493");
        }

        return transcriptionClientBuilder;
    }

    protected TranscriptionOptions fromAudioFile() {
        return new TranscriptionOptions(
            new AudioFileDetails(BinaryData.fromBytes(SAMPLE_WAV)).setFilename(SAMPLE_WAV_FILE_NAME));
    }

    protected BinaryData createMultipartBody(TranscriptionOptions options, RequestOptions requestOptions) {
        AudioFileDetails audioFileDetails
            = new AudioFileDetails(BinaryData.fromBytes(SAMPLE_WAV)).setFilename(SAMPLE_WAV_FILE_NAME);

        return new MultipartFormDataHelper(requestOptions).serializeJsonField("definition", options)
            .serializeFileField("audio", audioFileDetails.getContent(), audioFileDetails.getContentType(),
                audioFileDetails.getFilename())
            .end()
            .getRequestBody();
    }

    /**
     * Validates the transcription result.
     *
     * @param testName The name of the test
     * @param result The transcription result to validate
     */
    protected void validateTranscriptionResult(String testName, TranscriptionResult result) {
        if (PRINT_RESULTS) {
            System.out.println("\n===== Test: " + testName + " =====");
            System.out.println("Duration: " + result.getDuration() + "ms");
            if (result.getCombinedPhrases() != null) {
                result.getCombinedPhrases()
                    .forEach(phrase -> System.out.println("Channel " + phrase.getChannel() + ": " + phrase.getText()));
            }
            if (result.getPhrases() != null) {
                result.getPhrases()
                    .forEach(phrase -> System.out
                        .println("Phrase: " + phrase.getText() + " (confidence: " + phrase.getConfidence() + ")"));
            }
        }

        // Basic validation
        assertNotNull(result, "Transcription result should not be null");
        assertNotNull(result.getDuration(), "Duration should not be null");
        assertTrue(result.getDuration().toMillis() > 0, "Duration should be greater than 0");
        assertNotNull(result.getCombinedPhrases(), "Combined phrases should not be null");
        assertFalse(result.getCombinedPhrases().isEmpty(), "Combined phrases should not be empty");
        assertNotNull(result.getPhrases(), "Phrases should not be null");
        assertFalse(result.getPhrases().isEmpty(), "Phrases should not be empty");

        // Validate combined phrases
        result.getCombinedPhrases().forEach(phrase -> {
            assertNotNull(phrase.getText(), "Combined phrase text should not be null");
            assertFalse(phrase.getText().isEmpty(), "Combined phrase text should not be empty");
        });

        // Validate phrases
        result.getPhrases().forEach(phrase -> {
            assertNotNull(phrase.getText(), "Phrase text should not be null");
            assertFalse(phrase.getText().isEmpty(), "Phrase text should not be empty");
            assertTrue(phrase.getConfidence() >= 0 && phrase.getConfidence() <= 1,
                "Confidence should be between 0 and 1");
            assertTrue(phrase.getOffset() >= 0, "Offset should be non-negative");
            assertTrue(phrase.getDuration().toMillis() > 0, "Phrase duration should be positive");
        });
    }

    /**
     * Prints HTTP request and response details for debugging.
     *
     * @param response The HTTP response
     */
    protected void printHttpRequestAndResponse(Response<?> response) {
        if (PRINT_RESULTS) {
            HttpRequest request = response.getRequest();
            System.out.println("\n===== HTTP Request =====");
            System.out.println(request.getHttpMethod() + " " + request.getUrl());
            request.getHeaders().forEach(header -> System.out.println(header.getName() + ": " + header.getValue()));

            System.out.println("\n===== HTTP Response =====");
            System.out.println("Status Code: " + response.getStatusCode());
            response.getHeaders().forEach(header -> System.out.println(header.getName() + ": " + header.getValue()));
        }
    }

}
