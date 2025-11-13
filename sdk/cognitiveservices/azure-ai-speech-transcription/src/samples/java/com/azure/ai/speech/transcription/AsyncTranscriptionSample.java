// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

// BEGIN: com.azure.ai.speech.transcription.async.imports
import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
// END: com.azure.ai.speech.transcription.async.imports

/**
 * Sample demonstrates asynchronous usage of Azure AI Speech Transcription using
 * the TranscriptionAsyncClient with Project Reactor.
 *
 * This sample shows:
 * - Creating a TranscriptionAsyncClient with API Key or Azure AD authentication
 * - Using reactive programming with Mono
 * - Different patterns: subscribe(), block(), and timeout()
 * - Handling async errors with doOnError()
 */
public class AsyncTranscriptionSample {
    /**
     * Main method to run the async transcription sample.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        String endpoint = System.getenv("SPEECH_ENDPOINT");
        String apiKey = System.getenv("SPEECH_API_KEY");

        if (endpoint == null) {
            System.err.println("Please set SPEECH_ENDPOINT environment variable");
            System.err.println("\nFor authentication, choose one of:");
            System.err.println("  Option 1: set SPEECH_API_KEY=your-api-key");
            System.err.println("  Option 2: Configure Azure AD credentials (DefaultAzureCredential)");
            return;
        }

        System.out.println("Azure AI Speech Transcription - Async Sample");
        System.out.println("=============================================\n");

        // Example 1: Using subscribe() with reactive callbacks
        demonstrateSubscribePattern(endpoint, apiKey);

        // Example 2: Using block() to wait for completion
        demonstrateBlockPattern(endpoint, apiKey);

        // Example 3: Using timeout and error handling
        demonstrateTimeoutAndErrorHandling(endpoint, apiKey);
    }

    /**
     * Demonstrates the subscribe() pattern for non-blocking async operations.
     * This is the recommended approach for truly asynchronous code.
     */
    private static void demonstrateSubscribePattern(String endpoint, String apiKey) {
        System.out.println("Example 1: Subscribe Pattern (Non-blocking)");
        System.out.println("--------------------------------------------");

        // Create async client with API Key authentication
        // BEGIN: com.azure.ai.speech.transcription.async.create-client.apikey
        TranscriptionAsyncClient asyncClient = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .buildAsyncClient();
        // END: com.azure.ai.speech.transcription.async.create-client.apikey

        // Alternative: Create async client with Azure AD authentication
        // BEGIN: com.azure.ai.speech.transcription.async.create-client.azuread
        // TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        // TranscriptionAsyncClient asyncClient = new TranscriptionClientBuilder()
        //     .endpoint(endpoint)
        //     .credential(credential)
        //     .buildAsyncClient();
        // END: com.azure.ai.speech.transcription.async.create-client.azuread

        try {
            String audioFilePath = "sample-audio.wav";
            if (!Files.exists(Paths.get(audioFilePath))) {
                System.out.println("⚠ Audio file not found: " + audioFilePath);
                System.out.println("  Skipping this example.\n");
                return;
            }

            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
                .setFilename(audioFilePath);

            // Use the AudioFileDetails constructor for cleaner code
            TranscriptionOptions options = new TranscriptionOptions(audioFileDetails);

            System.out.println("Starting async transcription with subscribe()...");

            // BEGIN: com.azure.ai.speech.transcription.async.subscribe-pattern
            // Use CountDownLatch to wait for async operation in this sample
            // In real applications, you wouldn't need this - the reactive chain would continue
            CountDownLatch latch = new CountDownLatch(1);

            // Subscribe to the Mono with success and error callbacks
            asyncClient.transcribe(options)
                .doOnSubscribe(subscription ->
                    System.out.println("  → Subscribed to transcription operation"))
                .doOnNext(result ->
                    System.out.println("  → Received result"))
                .subscribe(
                    // onNext: Called when result is available
                    result -> {
                        System.out.println("\n✓ Transcription completed!");
                        System.out.println("  Duration: " + result.getDuration() + " ms");
                        if (result.getCombinedPhrases() != null && !result.getCombinedPhrases().isEmpty()) {
                            result.getCombinedPhrases().forEach(phrase ->
                                System.out.println("  Text: " + phrase.getText())
                            );
                        }
                        latch.countDown();
                    },
                    // onError: Called if an error occurs
                    error -> {
                        System.err.println("✗ Error during transcription: " + error.getMessage());
                        latch.countDown();
                    },
                    // onComplete: Called when the Mono completes
                    () -> System.out.println("  → Operation completed\n")
                );

            // Wait for completion (only for this sample - not typical in reactive code)
            latch.await(60, TimeUnit.SECONDS);
            // END: com.azure.ai.speech.transcription.async.subscribe-pattern

        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage() + "\n");
        }
    }

    /**
     * Demonstrates the block() pattern which converts async to sync.
     * Use this when you need to integrate with synchronous code.
     */
    private static void demonstrateBlockPattern(String endpoint, String apiKey) {
        System.out.println("Example 2: Block Pattern (Sync over Async)");
        System.out.println("-------------------------------------------");

        TranscriptionAsyncClient asyncClient = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .buildAsyncClient();

        try {
            String audioFilePath = "sample-audio.wav";
            if (!Files.exists(Paths.get(audioFilePath))) {
                System.out.println("⚠ Audio file not found: " + audioFilePath);
                System.out.println("  Skipping this example.\n");
                return;
            }

            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
                .setFilename(audioFilePath);

            // Use the AudioFileDetails constructor
            TranscriptionOptions options = new TranscriptionOptions(audioFileDetails);

            System.out.println("Starting async transcription with block()...");

            // BEGIN: com.azure.ai.speech.transcription.async.block-pattern
            // block() waits for the operation to complete and returns the result
            // This is useful when integrating async client with synchronous code
            TranscriptionResult result = asyncClient.transcribe(options).block();
            // END: com.azure.ai.speech.transcription.async.block-pattern

            if (result != null) {
                System.out.println("\n✓ Transcription completed!");
                System.out.println("  Duration: " + result.getDuration() + " ms\n");
            }

        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage() + "\n");
        }
    }

    /**
     * Demonstrates timeout configuration and comprehensive error handling.
     */
    private static void demonstrateTimeoutAndErrorHandling(String endpoint, String apiKey) {
        System.out.println("Example 3: Timeout and Error Handling");
        System.out.println("--------------------------------------");

        TranscriptionAsyncClient asyncClient = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .buildAsyncClient();

        try {
            String audioFilePath = "sample-audio.wav";
            if (!Files.exists(Paths.get(audioFilePath))) {
                System.out.println("⚠ Audio file not found: " + audioFilePath);
                System.out.println("  Skipping this example.\n");
                return;
            }

            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
                .setFilename(audioFilePath);

            // Use the AudioFileDetails constructor
            TranscriptionOptions options = new TranscriptionOptions(audioFileDetails);

            System.out.println("Starting async transcription with timeout and error handling...");

            // BEGIN: com.azure.ai.speech.transcription.async.timeout-error-handling
            Mono<TranscriptionResult> transcriptionMono = asyncClient.transcribe(options)
                // Set a timeout for the operation
                .timeout(Duration.ofMinutes(2))
                // Handle errors
                .doOnError(error -> {
                    System.err.println("  → Error occurred: " + error.getClass().getSimpleName());
                    System.err.println("  → Message: " + error.getMessage());
                })
                // Provide fallback behavior on error (optional)
                .onErrorResume(error -> {
                    System.err.println("  → Attempting fallback behavior...");
                    return Mono.empty(); // Return empty result as fallback
                });

            // Execute and wait for result
            TranscriptionResult result = transcriptionMono.block();
            // END: com.azure.ai.speech.transcription.async.timeout-error-handling

            if (result != null) {
                System.out.println("\n✓ Transcription completed successfully!");
            } else {
                System.out.println("\n⚠ No result (possibly due to error fallback)\n");
            }

        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage() + "\n");
        }
    }
}
