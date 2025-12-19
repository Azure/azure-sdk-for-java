// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

// BEGIN: com.azure.ai.speech.transcription.enhancedmode.imports
import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.EnhancedModeOptions;
import com.azure.ai.speech.transcription.models.ProfanityFilterMode;
import com.azure.ai.speech.transcription.models.TranscriptionDiarizationOptions;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
// END: com.azure.ai.speech.transcription.enhancedmode.imports

/**
 * Sample demonstrates how to use EnhancedModeOptions with LLM-enhanced speech transcription
 * combining multiple features for optimal transcription quality.
 *
 * This sample shows:
 * - Using lexical format prompts to guide LLM output
 * - Providing domain-specific context for technical terminology
 * - Enabling diarization (speaker identification) with enhanced mode
 * - Configuring profanity filtering
 *
 * Enhanced mode leverages large language models to improve transcription quality
 * by understanding context and domain-specific terminology.
 */
public class EnhancedModeSample {
    /**
     * Main method to run the enhanced mode sample.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        String endpoint = System.getenv("SPEECH_ENDPOINT");
        String apiKey = System.getenv("SPEECH_API_KEY");

        if (endpoint == null || apiKey == null) {
            System.err.println("Please set SPEECH_ENDPOINT and SPEECH_API_KEY environment variables");
            System.err.println("Example:");
            System.err.println("  set SPEECH_ENDPOINT=https://your-resource-name.cognitiveservices.azure.com/");
            System.err.println("  set SPEECH_API_KEY=your-api-key");
            return;
        }

        System.out.println("Azure AI Speech Transcription - Enhanced Mode Sample");
        System.out.println("=====================================================\n");

        // Demonstrate full enhanced mode with all features combined
        demonstrateFullEnhancedMode(endpoint, apiKey);
    }

    /**
     * Demonstrates using full enhanced mode with multiple features combined.
     * This shows how to use lexical format prompts, domain context, diarization,
     * and profanity filtering together for optimal transcription quality.
     */
    private static void demonstrateFullEnhancedMode(String endpoint, String apiKey) {
        System.out.println("Enhanced Mode with Multiple Features Combined");
        System.out.println("----------------------------------------------");

        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .buildClient();

        try {
            String audioFilePath = "src/samples/assets/sample-audio.wav";
            if (!Files.exists(Paths.get(audioFilePath))) {
                System.out.println("Audio file not found: " + audioFilePath);
                System.out.println("  Skipping this example.\n");
                return;
            }

            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));

            // Use the helper method to demonstrate full configuration
            TranscriptionResult result = transcribeWithFullEnhancedMode(client, audioData, audioFilePath);

            System.out.println(" Full enhanced mode configuration applied");
            System.out.println("Features: LLM prompts, diarization, profanity filtering");
            System.out.println("Duration: " + result.getDuration() + " ms");
            if (result.getCombinedPhrases() != null && !result.getCombinedPhrases().isEmpty()) {
                System.out.println("\nTranscription: " + result.getCombinedPhrases().get(0).getText());
            }
            if (result.getPhrases() != null && !result.getPhrases().isEmpty()) {
                System.out.println("\nPhrases with speakers:");
                result.getPhrases().forEach(phrase ->
                    System.out.println("  [Speaker " + phrase.getSpeaker() + ", "
                        + phrase.getOffset() + " ms] " + phrase.getText())
                );
            }
            System.out.println();

        } catch (IOException e) {
            System.err.println("Error reading audio file: " + e.getMessage() + "\n");
        } catch (Exception e) {
            System.err.println("Error during transcription: " + e.getMessage() + "\n");
        }
    }

    /**
     * Helper method demonstrating how to combine all enhanced mode features.
     * This is a reusable pattern for high-quality LLM-enhanced transcription.
     */
    // BEGIN: com.azure.ai.speech.transcription.enhancedmode.complete
    private static TranscriptionResult transcribeWithFullEnhancedMode(
        TranscriptionClient client,
        byte[] audioData,
        String filename
    ) throws Exception {
        // Create audio file details
        AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData));

        // Configure comprehensive LLM-enhanced mode settings
        // Enhanced mode is automatically enabled when you create EnhancedModeOptions
        // Always include lexical format prompt for best results
        EnhancedModeOptions enhancedMode = new EnhancedModeOptions()
            .setTask("transcribe")
            .setPrompts(Arrays.asList(
                "Output must be in lexical format."
            ));

        // Enable diarization for speaker identification
        TranscriptionDiarizationOptions diarizationOptions = new TranscriptionDiarizationOptions()
            .setMaxSpeakers(5);

        // Create transcription options with all features enabled
        TranscriptionOptions options = new TranscriptionOptions(audioFileDetails)
            .setLocales(Arrays.asList())
            .setEnhancedModeOptions(enhancedMode)
            .setDiarizationOptions(diarizationOptions)
            .setProfanityFilterMode(ProfanityFilterMode.MASKED);

        // Transcribe with full LLM-enhanced mode and diarization
        return client.transcribe(options);
    }
    // END: com.azure.ai.speech.transcription.enhancedmode.complete
}
