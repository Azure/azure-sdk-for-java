// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

// BEGIN: com.azure.ai.speech.transcription.error.imports
import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.TranscriptionContent;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.CredentialUnavailableException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
// END: com.azure.ai.speech.transcription.error.imports

/**
 * Sample demonstrates proper error handling patterns when using Azure AI Speech Transcription.
 *
 * This sample shows how to handle:
 * - Authentication errors (invalid API key, Azure AD token errors, or endpoint)
 * - Invalid audio format errors
 * - File not found errors
 * - Service-side errors (rate limiting, service unavailable)
 * - Network errors
 */
public class ErrorHandlingSample {
    /**
     * Main method to run the error handling sample.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("Azure AI Speech Transcription - Error Handling Sample");
        System.out.println("======================================================\n");

        // Example 1: Handling API Key authentication errors
        demonstrateAuthenticationErrorHandling();

        // Example 2: Handling Azure AD authentication errors
        demonstrateAzureAdAuthenticationErrorHandling();

        // Example 3: Handling file I/O errors
        demonstrateFileErrorHandling();

        // Example 4: Handling service errors
        demonstrateServiceErrorHandling();

        // Example 5: Comprehensive error handling
        demonstrateComprehensiveErrorHandling();
    }

    /**
     * Demonstrates handling API Key authentication-related errors.
     */
    private static void demonstrateAuthenticationErrorHandling() {
        System.out.println("Example 1: API Key Authentication Error Handling");
        System.out.println("-------------------------------------------------");

        // Intentionally use invalid credentials to demonstrate error handling
        String invalidEndpoint = "https://invalid-endpoint.cognitiveservices.azure.com/";
        String invalidApiKey = "invalid-api-key";

        // BEGIN: com.azure.ai.speech.transcription.error.authentication.apikey
        try {
            TranscriptionClient client = new TranscriptionClientBuilder()
                .endpoint(invalidEndpoint)
                .credential(new KeyCredential(invalidApiKey))
                .buildClient();

            // This will fail with authentication error
            byte[] dummyAudio = new byte[1024];
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(dummyAudio));
            TranscriptionContent requestContent = new TranscriptionContent()
                .setAudio(audioFileDetails)
                .setOptions(new TranscriptionOptions());

            client.transcribe(requestContent);

        } catch (ClientAuthenticationException e) {
            // Specific exception for authentication failures
            System.err.println("✗ Authentication failed!");
            System.err.println("  Error: " + e.getMessage());
            System.err.println("  → Please verify your API key and endpoint are correct");
            System.err.println("  → Ensure your Azure resource is active and not expired\n");
        } catch (Exception e) {
            System.err.println("✗ Unexpected error: " + e.getMessage() + "\n");
        }
        // END: com.azure.ai.speech.transcription.error.authentication.apikey
    }

    /**
     * Demonstrates handling Azure AD authentication-related errors.
     */
    private static void demonstrateAzureAdAuthenticationErrorHandling() {
        System.out.println("Example 2: Azure AD Authentication Error Handling");
        System.out.println("--------------------------------------------------");

        String endpoint = System.getenv("SPEECH_ENDPOINT");
        if (endpoint == null) {
            endpoint = "https://example.cognitiveservices.azure.com/";
        }

        // BEGIN: com.azure.ai.speech.transcription.error.authentication.azuread
        try {
            TokenCredential credential = new DefaultAzureCredentialBuilder().build();
            TranscriptionClient client = new TranscriptionClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

            // Attempt to transcribe (may fail if Azure AD not configured)
            byte[] dummyAudio = new byte[1024];
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(dummyAudio));
            TranscriptionContent requestContent = new TranscriptionContent()
                .setAudio(audioFileDetails)
                .setOptions(new TranscriptionOptions());

            client.transcribe(requestContent);

        } catch (CredentialUnavailableException e) {
            // No Azure AD credential sources are available
            System.err.println("✗ Azure AD credentials unavailable!");
            System.err.println("  Error: " + e.getMessage());
            System.err.println("  → Ensure you have configured at least one credential source:");
            System.err.println("    - Managed Identity (for Azure-hosted apps)");
            System.err.println("    - Azure CLI (run 'az login')");
            System.err.println("    - Environment variables (AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET)");
            System.err.println("    - Visual Studio Code or IntelliJ authentication\n");
        } catch (ClientAuthenticationException e) {
            // Authentication token invalid or expired
            System.err.println("✗ Azure AD authentication failed!");
            System.err.println("  Error: " + e.getMessage());
            System.err.println("  → Verify your identity has 'Cognitive Services User' role assigned");
            System.err.println("  → Check if your token has expired or is invalid");
            System.err.println("  → Ensure Azure AD authentication is enabled on the resource\n");
        } catch (Exception e) {
            System.err.println("✗ Unexpected error: " + e.getMessage() + "\n");
        }
        // END: com.azure.ai.speech.transcription.error.authentication.azuread
    }

    /**
     * Demonstrates handling file I/O errors.
     */
    private static void demonstrateFileErrorHandling() {
        System.out.println("Example 3: File Error Handling");
        System.out.println("-------------------------------");

        String endpoint = System.getenv("SPEECH_ENDPOINT");
        String apiKey = System.getenv("SPEECH_API_KEY");

        if (endpoint == null || apiKey == null) {
            System.out.println("⚠ SPEECH_ENDPOINT or SPEECH_API_KEY not set, skipping example\n");
            return;
        }

        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .buildClient();

        // Try to load a non-existent file
        String audioFilePath = "non-existent-file.wav";

        // BEGIN: com.azure.ai.speech.transcription.error.file-io
        try {
            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData));
            TranscriptionContent requestContent = new TranscriptionContent()
                .setAudio(audioFileDetails)
                .setOptions(new TranscriptionOptions());

            client.transcribe(requestContent);

        } catch (IOException e) {
            // Handle file I/O errors
            System.err.println("✗ File error occurred!");
            System.err.println("  Error: " + e.getMessage());
            System.err.println("  → Verify the audio file path is correct");
            System.err.println("  → Ensure you have read permissions for the file");
            System.err.println("  → Check that the file exists\n");
        } catch (Exception e) {
            System.err.println("✗ Unexpected error: " + e.getMessage() + "\n");
        }
        // END: com.azure.ai.speech.transcription.error.file-io
    }

    /**
     * Demonstrates handling service-side errors.
     */
    private static void demonstrateServiceErrorHandling() {
        System.out.println("Example 3: Service Error Handling");
        System.out.println("----------------------------------");

        String endpoint = System.getenv("SPEECH_ENDPOINT");
        String apiKey = System.getenv("SPEECH_API_KEY");

        if (endpoint == null || apiKey == null) {
            System.out.println("⚠ SPEECH_ENDPOINT or SPEECH_API_KEY not set, skipping example\n");
            return;
        }

        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .buildClient();

        try {
            // Create invalid audio data to trigger service error
            byte[] invalidAudio = new byte[10]; // Too small to be valid audio
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(invalidAudio))
                .setFilename("invalid.wav");
            TranscriptionContent requestContent = new TranscriptionContent()
                .setAudio(audioFileDetails)
                .setOptions(new TranscriptionOptions());

            client.transcribe(requestContent);

        // BEGIN: com.azure.ai.speech.transcription.error.service-errors
        } catch (HttpResponseException e) {
            // Handle HTTP response errors from the service
            System.err.println("✗ Service error occurred!");
            System.err.println("  Status code: " + e.getResponse().getStatusCode());
            System.err.println("  Error: " + e.getMessage());

            HttpResponse response = e.getResponse();
            int statusCode = response.getStatusCode();

            if (statusCode == 400) {
                System.err.println("  → Bad Request: Check your audio format and request parameters");
            } else if (statusCode == 401) {
                System.err.println("  → Unauthorized: Verify your API key");
            } else if (statusCode == 403) {
                System.err.println("  → Forbidden: Check your subscription permissions");
            } else if (statusCode == 429) {
                System.err.println("  → Rate Limited: Too many requests, please retry later");
            } else if (statusCode >= 500) {
                System.err.println("  → Server Error: The service is experiencing issues, please retry");
            }
            System.err.println();
        // END: com.azure.ai.speech.transcription.error.service-errors
        } catch (Exception e) {
            System.err.println("✗ Unexpected error: " + e.getMessage() + "\n");
        }
    }

    /**
     * Demonstrates comprehensive error handling with all best practices.
     */
    private static void demonstrateComprehensiveErrorHandling() {
        System.out.println("Example 4: Comprehensive Error Handling");
        System.out.println("----------------------------------------");

        String endpoint = System.getenv("SPEECH_ENDPOINT");
        String apiKey = System.getenv("SPEECH_API_KEY");

        if (endpoint == null || apiKey == null) {
            System.err.println("✗ Configuration error: Environment variables not set");
            System.err.println("  → Please set SPEECH_ENDPOINT and SPEECH_API_KEY");
            System.err.println();
            return;
        }

        TranscriptionClient client = null;

        try {
            // Step 1: Create client
            client = new TranscriptionClientBuilder()
                .endpoint(endpoint)
                .credential(new KeyCredential(apiKey))
                .buildClient();
            System.out.println("✓ Client created successfully");

            // Step 2: Read audio file with validation
            String audioFilePath = "sample-audio.wav";

            if (!Files.exists(Paths.get(audioFilePath))) {
                throw new IllegalArgumentException("Audio file not found: " + audioFilePath);
            }

            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));
            System.out.println("✓ Audio file loaded (" + audioData.length + " bytes)");

            // Step 3: Validate audio size
            final int maxFileSize = 250 * 1024 * 1024; // 250 MB
            if (audioData.length > maxFileSize) {
                throw new IllegalArgumentException(
                    "Audio file too large: " + audioData.length + " bytes (max: " + maxFileSize + ")");
            }
            System.out.println("✓ Audio size validated");

            // Step 4: Prepare transcription request
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
                .setFilename(audioFilePath);
            TranscriptionOptions options = new TranscriptionOptions();
            TranscriptionContent requestContent = new TranscriptionContent()
                .setAudio(audioFileDetails)
                .setOptions(options);
            System.out.println("✓ Request prepared");

            // BEGIN: com.azure.ai.speech.transcription.error.retry-logic
            // Step 5: Transcribe with retry logic
            int maxRetries = 3;
            int retryCount = 0;
            TranscriptionResult result = null;

            while (retryCount < maxRetries) {
                try {
                    System.out.println("  Attempting transcription (attempt " + (retryCount + 1) + "/" + maxRetries + ")...");
                    result = client.transcribe(requestContent);
                    break; // Success, exit retry loop
                } catch (HttpResponseException e) {
                    if (e.getResponse().getStatusCode() == 429 || e.getResponse().getStatusCode() >= 500) {
                        // Retryable error
                        retryCount++;
                        if (retryCount < maxRetries) {
                            long backoffMs = (long) (1000 * Math.pow(2, retryCount - 1)); // Exponential backoff
                            System.out.println("  → Retryable error, waiting " + backoffMs + "ms before retry...");
                            Thread.sleep(backoffMs);
                        } else {
                            throw e; // Max retries reached
                        }
                    } else {
                        throw e; // Non-retryable error
                    }
                }
            }
            // END: com.azure.ai.speech.transcription.error.retry-logic

            // Step 6: Process results
            if (result != null) {
                System.out.println("\n✓ Transcription completed successfully!");
                System.out.println("  Duration: " + result.getDuration() + " ms");
                if (result.getCombinedPhrases() != null && !result.getCombinedPhrases().isEmpty()) {
                    System.out.println("  Phrases: " + result.getCombinedPhrases().size());
                }
            }

        } catch (IllegalArgumentException e) {
            System.err.println("\n✗ Validation error: " + e.getMessage());
        } catch (ClientAuthenticationException e) {
            System.err.println("\n✗ Authentication error: " + e.getMessage());
            System.err.println("  → Check your API key and endpoint");
        } catch (HttpResponseException e) {
            System.err.println("\n✗ Service error: " + e.getMessage());
            System.err.println("  → Status code: " + e.getResponse().getStatusCode());
        } catch (IOException e) {
            System.err.println("\n✗ File I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("\n✗ Interrupted during retry: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("\n✗ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
    }
}
