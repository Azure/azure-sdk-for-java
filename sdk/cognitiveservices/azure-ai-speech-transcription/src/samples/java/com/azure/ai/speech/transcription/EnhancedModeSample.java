// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

// BEGIN: com.azure.ai.speech.transcription.enhancedmode.imports
import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.EnhancedModeOptions;
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
 * Sample demonstrates how to use EnhancedModeOptions to improve transcription quality
 * with features like custom prompts, translation, and task-specific configurations.
 *
 * This sample shows:
 * - Enabling enhanced mode for improved transcription accuracy
 * - Using custom prompts to guide transcription with domain-specific terminology
 * - Configuring translation to transcribe audio in one language and translate to another
 * - Setting task-specific configurations for different use cases
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

        // Example 1: Basic enhanced mode
        demonstrateBasicEnhancedMode(endpoint, apiKey);

        // Example 2: Enhanced mode with custom prompts
        demonstrateEnhancedModeWithPrompts(endpoint, apiKey);

        // Example 3: Enhanced mode with translation
        demonstrateEnhancedModeWithTranslation(endpoint, apiKey);

        // Example 4: Enhanced mode with task-specific configuration
        demonstrateTaskSpecificEnhancedMode(endpoint, apiKey);
    }

    /**
     * Demonstrates basic usage of enhanced mode to improve transcription quality.
     */
    private static void demonstrateBasicEnhancedMode(String endpoint, String apiKey) {
        System.out.println("Example 1: Basic Enhanced Mode");
        System.out.println("-------------------------------");

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

            // BEGIN: com.azure.ai.speech.transcription.enhancedmode.basic
            // Enable enhanced mode for improved transcription quality
            EnhancedModeOptions enhancedMode = new EnhancedModeOptions();

            TranscriptionOptions options = new TranscriptionOptions(audioFileDetails)
                .setLocales(Arrays.asList("en-US"))
                .setEnhancedModeOptions(enhancedMode);
            // END: com.azure.ai.speech.transcription.enhancedmode.basic

            System.out.println("Transcribing with enhanced mode enabled...");

            TranscriptionResult result = client.transcribe(options);

            System.out.println("✓ Enhanced mode enabled");
            System.out.println("Duration: " + result.getDuration() + " ms");
            if (result.getCombinedPhrases() != null && !result.getCombinedPhrases().isEmpty()) {
                System.out.println("Transcription: " + result.getCombinedPhrases().get(0).getText());
            }
            System.out.println();

        } catch (IOException e) {
            System.err.println("✗ Error reading audio file: " + e.getMessage() + "\n");
        } catch (Exception e) {
            System.err.println("✗ Error during transcription: " + e.getMessage() + "\n");
        }
    }

    /**
     * Demonstrates using enhanced mode with custom prompts to guide transcription.
     * Prompts help improve accuracy for domain-specific terminology, names, or context.
     */
    private static void demonstrateEnhancedModeWithPrompts(String endpoint, String apiKey) {
        System.out.println("Example 2: Enhanced Mode with Custom Prompts");
        System.out.println("---------------------------------------------");

        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .buildClient();

        try {
            String audioFilePath = "medical-consultation.wav";
            if (!Files.exists(Paths.get(audioFilePath))) {
                System.out.println("⚠ Audio file not found: " + audioFilePath);
                System.out.println("  Skipping this example.\n");
                return;
            }

            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
                .setFilename(audioFilePath);

            // BEGIN: com.azure.ai.speech.transcription.enhancedmode.prompts
            // Use prompts to guide transcription with domain-specific terminology
            EnhancedModeOptions enhancedMode = new EnhancedModeOptions()
                .setPrompts(Arrays.asList(
                    "Medical consultation discussing hypertension and diabetes",
                    "Common medications: metformin, lisinopril, atorvastatin",
                    "Patient symptoms and treatment plan"
                ));

            TranscriptionOptions options = new TranscriptionOptions(audioFileDetails)
                .setLocales(Arrays.asList("en-US"))
                .setEnhancedModeOptions(enhancedMode);
            // END: com.azure.ai.speech.transcription.enhancedmode.prompts

            System.out.println("Transcribing with custom prompts...");

            TranscriptionResult result = client.transcribe(options);

            System.out.println("✓ Enhanced mode with custom prompts");
            System.out.println("Prompts used:");
            for (String prompt : enhancedMode.getPrompts()) {
                System.out.println("  - " + prompt);
            }
            if (result.getCombinedPhrases() != null && !result.getCombinedPhrases().isEmpty()) {
                System.out.println("\nTranscription: " + result.getCombinedPhrases().get(0).getText());
            }
            System.out.println();

        } catch (IOException e) {
            System.err.println("✗ Error reading audio file: " + e.getMessage() + "\n");
        } catch (Exception e) {
            System.err.println("✗ Error during transcription: " + e.getMessage() + "\n");
        }
    }

    /**
     * Demonstrates using enhanced mode with translation to transcribe and translate
     * audio from one language to another.
     */
    private static void demonstrateEnhancedModeWithTranslation(String endpoint, String apiKey) {
        System.out.println("Example 3: Enhanced Mode with Translation");
        System.out.println("------------------------------------------");

        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .buildClient();

        try {
            String audioFilePath = "spanish-audio.wav";
            if (!Files.exists(Paths.get(audioFilePath))) {
                System.out.println("⚠ Audio file not found: " + audioFilePath);
                System.out.println("  Skipping this example.\n");
                return;
            }

            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
                .setFilename(audioFilePath);

            // BEGIN: com.azure.ai.speech.transcription.enhancedmode.translation
            // Configure enhanced mode to transcribe Spanish audio and translate to English
            EnhancedModeOptions enhancedMode = new EnhancedModeOptions()
                .setTargetLanguage("en-US"); // Translate to English

            TranscriptionOptions options = new TranscriptionOptions(audioFileDetails)
                .setLocales(Arrays.asList("es-ES")) // Source language: Spanish
                .setEnhancedModeOptions(enhancedMode);
            // END: com.azure.ai.speech.transcription.enhancedmode.translation

            System.out.println("Transcribing and translating...");

            TranscriptionResult result = client.transcribe(options);

            System.out.println("✓ Enhanced mode with translation");
            System.out.println("Source language: es-ES");
            System.out.println("Target language: " + enhancedMode.getTargetLanguage());
            if (result.getCombinedPhrases() != null && !result.getCombinedPhrases().isEmpty()) {
                System.out.println("\nTranscription (translated): " + result.getCombinedPhrases().get(0).getText());
            }
            System.out.println();

        } catch (IOException e) {
            System.err.println("✗ Error reading audio file: " + e.getMessage() + "\n");
        } catch (Exception e) {
            System.err.println("✗ Error during transcription: " + e.getMessage() + "\n");
        }
    }

    /**
     * Demonstrates using enhanced mode with task-specific configuration.
     * Different tasks may benefit from different enhanced mode settings.
     */
    private static void demonstrateTaskSpecificEnhancedMode(String endpoint, String apiKey) {
        System.out.println("Example 4: Task-Specific Enhanced Mode");
        System.out.println("---------------------------------------");

        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .buildClient();

        try {
            String audioFilePath = "business-meeting.wav";
            if (!Files.exists(Paths.get(audioFilePath))) {
                System.out.println("⚠ Audio file not found: " + audioFilePath);
                System.out.println("  Skipping this example.\n");
                return;
            }

            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
                .setFilename(audioFilePath);

            // BEGIN: com.azure.ai.speech.transcription.enhancedmode.taskspecific
            // Configure enhanced mode for a specific task type (e.g., meeting transcription)
            EnhancedModeOptions enhancedMode = new EnhancedModeOptions()
                .setTask("meeting") // Task type: meeting, interview, presentation, etc.
                .setPrompts(Arrays.asList(
                    "Business meeting about Q4 revenue projections",
                    "Participants: CEO, CFO, Sales Director",
                    "Topics: budget, forecasts, market trends"
                ));

            TranscriptionOptions options = new TranscriptionOptions(audioFileDetails)
                .setLocales(Arrays.asList("en-US"))
                .setEnhancedModeOptions(enhancedMode);
            // END: com.azure.ai.speech.transcription.enhancedmode.taskspecific

            System.out.println("Transcribing with task-specific configuration...");

            TranscriptionResult result = client.transcribe(options);

            System.out.println("✓ Enhanced mode with task-specific configuration");
            System.out.println("Task type: " + enhancedMode.getTask());
            System.out.println("Enabled: " + enhancedMode.isEnabled());
            if (result.getPhrases() != null && !result.getPhrases().isEmpty()) {
                System.out.println("\nTranscription phrases:");
                result.getPhrases().forEach(phrase ->
                    System.out.println("  [" + phrase.getOffset() + "ms] " + phrase.getText())
                );
            }
            System.out.println();

        } catch (IOException e) {
            System.err.println("✗ Error reading audio file: " + e.getMessage() + "\n");
        } catch (Exception e) {
            System.err.println("✗ Error during transcription: " + e.getMessage() + "\n");
        }
    }

    /**
     * Demonstrates combining multiple enhanced mode features for optimal results.
     */
    // BEGIN: com.azure.ai.speech.transcription.enhancedmode.complete
    private static TranscriptionResult transcribeWithFullEnhancedMode(
        TranscriptionClient client,
        byte[] audioData,
        String filename
    ) throws Exception {
        // Create audio file details
        AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
            .setFilename(filename);

        // Configure comprehensive enhanced mode settings
        EnhancedModeOptions enhancedMode = new EnhancedModeOptions()
            .setTask("interview")
            .setTargetLanguage("en-US")
            .setPrompts(Arrays.asList(
                "Technical interview about cloud architecture",
                "Topics: Azure, microservices, Kubernetes, DevOps",
                "Key terms: containerization, scalability, CI/CD"
            ));

        // Create transcription options with enhanced mode
        TranscriptionOptions options = new TranscriptionOptions(audioFileDetails)
            .setLocales(Arrays.asList("en-US"))
            .setEnhancedModeOptions(enhancedMode);

        // Transcribe with all enhanced mode features
        return client.transcribe(options);
    }
    // END: com.azure.ai.speech.transcription.enhancedmode.complete
}
