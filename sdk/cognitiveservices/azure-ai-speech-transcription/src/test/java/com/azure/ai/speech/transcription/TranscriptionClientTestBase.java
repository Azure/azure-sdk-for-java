// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.TranscriptionContent;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base class for TranscriptionClient tests. Contains helper methods and common test infrastructure.
 * Supports both API Key (KeyCredential) and Azure AD (TokenCredential) authentication.
 */
class TranscriptionClientTestBase extends TestProxyTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(TranscriptionClientTestBase.class);

    final Boolean printResults = false; // Set to true to print results to console window

    // Sample audio file for testing
    final String audioFile = "./src/test/java/com/azure/ai/speech/transcription/sample.wav";

    // The clients that will be used for tests
    private TranscriptionClient client = null;
    private TranscriptionAsyncClient asyncClient = null;

    /**
     * Sets up the test resources before each test.
     */
    @BeforeEach
    public void setupTest() {
        // Reset clients before each test to ensure clean state
        client = null;
        asyncClient = null;
    }

    /**
     * Cleans up test resources after each test.
     */
    @AfterEach
    public void cleanupTest() {
        // Clean up any resources if needed
        // Note: The clients don't require explicit cleanup as they are managed by the test framework
    }

    /**
     * Creates a client for testing.
     *
     * @param useKeyAuth Whether to use key-based authentication (true) or token-based authentication (false)
     * @param useRealKey Whether to use a real key from environment variables (true) or a fake key (false).
     *                   Only applies when useKeyAuth is true.
     * @param sync Whether to create a synchronous client (true) or asynchronous client (false)
     */
    protected void createClient(Boolean useKeyAuth, Boolean useRealKey, Boolean sync) {
        TestMode testMode = getTestMode();

        // Define endpoint and auth credentials
        String endpoint = "https://fake-resource-name.cognitiveservices.azure.com";
        String key = "00000000000000000000000000000000";

        if (testMode == TestMode.LIVE || testMode == TestMode.RECORD) {
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
        if (getTestMode() == TestMode.LIVE) {
            if (useKeyAuth) {
                transcriptionClientBuilder.credential(new KeyCredential(key));
            } else {
                // Use Azure AD authentication (TokenCredential)
                TokenCredential credential = new DefaultAzureCredentialBuilder().build();
                transcriptionClientBuilder.credential(credential);
            }
        } else if (getTestMode() == TestMode.RECORD) {
            transcriptionClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
            if (useKeyAuth) {
                transcriptionClientBuilder.credential(new KeyCredential(key));
            } else {
                TokenCredential credential = new DefaultAzureCredentialBuilder().build();
                transcriptionClientBuilder.credential(credential);
            }
        } else if (getTestMode() == TestMode.PLAYBACK) {
            transcriptionClientBuilder.httpClient(interceptorManager.getPlaybackClient());
            // In playback mode, use a fake key regardless of authentication method
            transcriptionClientBuilder.credential(new KeyCredential(key));
        }

        // Set recording filters
        if (!interceptorManager.isLiveMode()) {
            // Remove sanitizers that might interfere with transcription-specific headers
            interceptorManager.removeSanitizers("AZSDK2003", "AZSDK2030", "AZSDK3430", "AZSDK3493");
        }

        if (sync) {
            client = transcriptionClientBuilder.buildClient();
        } else {
            asyncClient = transcriptionClientBuilder.buildAsyncClient();
        }
    }

    /**
     * Performs transcription and validates the result.
     *
     * @param testName A label that uniquely defines the test. Used in console printout.
     * @param sync 'true' to use synchronous client, 'false' to use asynchronous client.
     * @param transcribeWithResponse 'true' to use transcribeWithResponse(), 'false' to use transcribe().
     * @param audioFilePath Path to the audio file to transcribe
     * @param options TranscriptionOptions (can be null)
     * @param requestOptions RequestOptions (can be null)
     */
    protected void doTranscription(String testName, Boolean sync, Boolean transcribeWithResponse, String audioFilePath,
        TranscriptionOptions options, RequestOptions requestOptions) {

        try {
            // Load audio file
            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));
            AudioFileDetails audioFileDetails
                = new AudioFileDetails(BinaryData.fromBytes(audioData)).setFilename(new File(audioFilePath).getName());

            TranscriptionContent requestContent
                = new TranscriptionContent().setAudio(audioFileDetails).setOptions(options);

            if (sync) {
                TranscriptionResult result = null;
                if (!transcribeWithResponse) {
                    result = client.transcribe(requestContent);
                } else {
                    // For transcribeWithResponse, we need to manually prepare the multipart request body
                    if (requestOptions == null) {
                        requestOptions = new RequestOptions();
                    }
                    BinaryData multipartBody
                        = new com.azure.ai.speech.transcription.implementation.MultipartFormDataHelper(requestOptions)
                            .serializeJsonField("definition", requestContent.getOptions())
                            .serializeFileField("audio",
                                requestContent.getAudio() == null ? null : requestContent.getAudio().getContent(),
                                requestContent.getAudio() == null ? null : requestContent.getAudio().getContentType(),
                                requestContent.getAudio() == null ? null : requestContent.getAudio().getFilename())
                            .end()
                            .getRequestBody();

                    Response<BinaryData> response = client.transcribeWithResponse(multipartBody, requestOptions);
                    printHttpRequestAndResponse(response);
                    result = response.getValue().toObject(TranscriptionResult.class);
                }
                validateTranscriptionResult(testName, result);
            } else {
                TranscriptionResult result = null;
                if (!transcribeWithResponse) {
                    result = asyncClient.transcribe(requestContent).block();
                } else {
                    // For transcribeWithResponse, we need to manually prepare the multipart request body
                    if (requestOptions == null) {
                        requestOptions = new RequestOptions();
                    }
                    BinaryData multipartBody
                        = new com.azure.ai.speech.transcription.implementation.MultipartFormDataHelper(requestOptions)
                            .serializeJsonField("definition", requestContent.getOptions())
                            .serializeFileField("audio",
                                requestContent.getAudio() == null ? null : requestContent.getAudio().getContent(),
                                requestContent.getAudio() == null ? null : requestContent.getAudio().getContentType(),
                                requestContent.getAudio() == null ? null : requestContent.getAudio().getFilename())
                            .end()
                            .getRequestBody();

                    Response<BinaryData> response
                        = asyncClient.transcribeWithResponse(multipartBody, requestOptions).block();
                    printHttpRequestAndResponse(response);
                    result = response.getValue().toObject(TranscriptionResult.class);
                }
                validateTranscriptionResult(testName, result);
            }
        } catch (Exception e) {
            LOGGER.error("Error in test {}: {}", testName, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates the transcription result.
     *
     * @param testName The name of the test
     * @param result The transcription result to validate
     */
    protected void validateTranscriptionResult(String testName, TranscriptionResult result) {
        if (printResults) {
            System.out.println("\n===== Test: " + testName + " =====");
            System.out.println("Duration: " + result.getDuration() + "ms");
            if (result.getCombinedPhrases() != null) {
                result.getCombinedPhrases().forEach(phrase -> {
                    System.out.println("Channel " + phrase.getChannel() + ": " + phrase.getText());
                });
            }
            if (result.getPhrases() != null) {
                result.getPhrases().forEach(phrase -> {
                    System.out.println("Phrase: " + phrase.getText() + " (confidence: " + phrase.getConfidence() + ")");
                });
            }
        }

        // Basic validation
        assertNotNull(result, "Transcription result should not be null");
        assertNotNull(result.getDuration(), "Duration should not be null");
        assertTrue(result.getDuration() > 0, "Duration should be greater than 0");
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
            assertTrue(phrase.getDuration() > 0, "Phrase duration should be positive");
        });
    }

    /**
     * Prints HTTP request and response details for debugging.
     *
     * @param response The HTTP response
     */
    protected void printHttpRequestAndResponse(Response<?> response) {
        if (printResults) {
            HttpRequest request = response.getRequest();
            System.out.println("\n===== HTTP Request =====");
            System.out.println(request.getHttpMethod() + " " + request.getUrl());
            request.getHeaders().forEach(header -> System.out.println(header.getName() + ": " + header.getValue()));

            System.out.println("\n===== HTTP Response =====");
            System.out.println("Status Code: " + response.getStatusCode());
            response.getHeaders().forEach(header -> System.out.println(header.getName() + ": " + header.getValue()));
        }
    }

    /**
     * Gets the synchronous client.
     *
     * @return The TranscriptionClient
     */
    protected TranscriptionClient getClient() {
        return client;
    }

    /**
     * Gets the asynchronous client.
     *
     * @return The TranscriptionAsyncClient
     */
    protected TranscriptionAsyncClient getAsyncClient() {
        return asyncClient;
    }
}
