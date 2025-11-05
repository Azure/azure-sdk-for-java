// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

// BEGIN: com.azure.ai.speech.transcription.advanced.imports
import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.ChannelCombinedPhrases;
import com.azure.ai.speech.transcription.models.ProfanityFilterMode;
import com.azure.ai.speech.transcription.models.TranscriptionContent;
import com.azure.ai.speech.transcription.models.TranscribedPhrase;
import com.azure.ai.speech.transcription.models.TranscriptionDiarizationOptions;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.BinaryData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
// END: com.azure.ai.speech.transcription.advanced.imports

/**
 * Sample demonstrates advanced features of Azure AI Speech Transcription including:
 * - Custom client configuration (logging, retry policies)
 * - API Key and Azure AD authentication options
 * - Advanced transcription options (locale, profanity filtering, speaker diarization)
 * - Detailed result processing with word-level timings and speaker information
 */
public class AdvancedTranscriptionSample {
    /**
     * Main method to run the advanced transcription sample.
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

        System.out.println("Azure AI Speech Transcription - Advanced Features Sample");
        System.out.println("=========================================================\n");

        // Example 1: Create client with custom configuration
        demonstrateCustomClientConfiguration(endpoint, apiKey);

        // Example 2: Advanced transcription options
        demonstrateAdvancedTranscriptionOptions(endpoint, apiKey);

        // Example 3: Detailed result processing
        demonstrateDetailedResultProcessing(endpoint, apiKey);
    }

    /**
     * Demonstrates creating a client with custom HTTP logging and retry policies.
     */
    private static void demonstrateCustomClientConfiguration(String endpoint, String apiKey) {
        System.out.println("Example 1: Custom Client Configuration");
        System.out.println("---------------------------------------");

        // BEGIN: com.azure.ai.speech.transcription.advanced.custom-client-config
        // Configure HTTP logging for debugging
        HttpLogOptions logOptions = new HttpLogOptions()
            .setLogLevel(HttpLogDetailLevel.HEADERS); // Log request/response headers

        // Configure retry policy with exponential backoff
        RetryOptions retryOptions = new RetryOptions(
            new ExponentialBackoffOptions()
                .setMaxRetries(5)
                .setBaseDelay(Duration.ofSeconds(1))
                .setMaxDelay(Duration.ofSeconds(60))
        );

        // Option 1: Use API Key authentication
        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .httpLogOptions(logOptions)
            .retryOptions(retryOptions)
            .buildClient();

        // Option 2: Use Azure AD authentication (recommended for production)
        // TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        // TranscriptionClient client = new TranscriptionClientBuilder()
        //     .endpoint(endpoint)
        //     .credential(credential)
        //     .httpLogOptions(logOptions)
        //     .retryOptions(retryOptions)
        //     .buildClient();
        // END: com.azure.ai.speech.transcription.advanced.custom-client-config

        System.out.println("✓ Client created with custom logging and retry configuration\n");
    }

    /**
     * Demonstrates advanced transcription options including locale, profanity filtering,
     * and speaker diarization.
     */
    private static void demonstrateAdvancedTranscriptionOptions(String endpoint, String apiKey) {
        System.out.println("Example 2: Advanced Transcription Options");
        System.out.println("------------------------------------------");

        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .buildClient();

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

            // BEGIN: com.azure.ai.speech.transcription.advanced.transcription-options
            // Configure advanced transcription options
            TranscriptionOptions options = new TranscriptionOptions()
                // Set specific locale(s) for transcription
                .setLocales(Arrays.asList("en-US", "es-ES"))
                // Control profanity handling: MASKED, REMOVED, RAW, or TAGS
                .setProfanityFilterMode(ProfanityFilterMode.MASKED)
                // Enable speaker diarization to identify different speakers
                .setDiarizationOptions(new TranscriptionDiarizationOptions()
                    .setEnabled(true)
                    .setMaxSpeakers(5)); // Max number of speakers (2-36)
            // END: com.azure.ai.speech.transcription.advanced.transcription-options

            TranscriptionContent requestContent = new TranscriptionContent()
                .setAudio(audioFileDetails)
                .setOptions(options);

            System.out.println("Configured options:");
            System.out.println("  - Locales: en-US, es-ES");
            System.out.println("  - Profanity filter: MASKED");
            System.out.println("  - Speaker diarization: up to 5 speakers");
            System.out.println("\nTranscribing...");

            TranscriptionResult result = client.transcribe(requestContent);

            System.out.println("\n✓ Transcription completed with advanced options");
            System.out.println("  Duration: " + result.getDuration() + " ms\n");

        } catch (IOException e) {
            System.err.println("✗ Error reading audio file: " + e.getMessage() + "\n");
        } catch (Exception e) {
            System.err.println("✗ Error during transcription: " + e.getMessage() + "\n");
        }
    }

    /**
     * Demonstrates detailed processing of transcription results including
     * word-level timings and speaker attribution.
     */
    private static void demonstrateDetailedResultProcessing(String endpoint, String apiKey) {
        System.out.println("Example 3: Detailed Result Processing");
        System.out.println("--------------------------------------");

        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .buildClient();

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

            // Enable diarization for speaker identification
            TranscriptionOptions options = new TranscriptionOptions()
                .setDiarizationOptions(new TranscriptionDiarizationOptions().setEnabled(true));

            TranscriptionContent requestContent = new TranscriptionContent()
                .setAudio(audioFileDetails)
                .setOptions(options);

            TranscriptionResult result = client.transcribe(requestContent);

            // BEGIN: com.azure.ai.speech.transcription.advanced.detailed-results
            // Process combined phrases (sentence-level results)
            if (result.getCombinedPhrases() != null) {
                System.out.println("Combined Phrases (Sentence-level):");
                for (ChannelCombinedPhrases channelPhrase : result.getCombinedPhrases()) {
                    System.out.printf("  [Channel %d] %s%n",
                        channelPhrase.getChannel(), channelPhrase.getText());
                }
                System.out.println();
            }

            // Process detailed phrases with word-level information
            if (result.getPhrases() != null) {
                System.out.println("Detailed Phrases (Word-level):");
                for (TranscribedPhrase phrase : result.getPhrases()) {
                    System.out.printf("  Speaker %d (%.2f-%.2fs): %s%n",
                        phrase.getSpeaker() != null ? phrase.getSpeaker() : 0,
                        phrase.getOffset() / 1000.0,
                        (phrase.getOffset() + phrase.getDuration()) / 1000.0,
                        phrase.getText());

                    // Show word-level timings
                    if (phrase.getWords() != null) {
                        System.out.println("    Word timings:");
                        phrase.getWords().forEach(word ->
                            System.out.printf("      - \"%s\" [%.2fs]%n",
                                word.getText(),
                                word.getOffset() / 1000.0)
                        );
                    }
                }
            }
            // END: com.azure.ai.speech.transcription.advanced.detailed-results

            System.out.println("\n✓ Detailed result processing completed\n");

        } catch (IOException e) {
            System.err.println("✗ Error reading audio file: " + e.getMessage() + "\n");
        } catch (Exception e) {
            System.err.println("✗ Error during transcription: " + e.getMessage() + "\n");
        }
    }
}

