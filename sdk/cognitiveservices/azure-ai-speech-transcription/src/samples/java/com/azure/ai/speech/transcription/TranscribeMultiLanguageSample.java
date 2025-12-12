// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.TranscribedPhrase;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Sample demonstrates how to transcribe audio containing multiple languages.
 *
 * When locales are NOT specified, the service automatically detects and transcribes
 * multiple languages within the same audio file, switching between them as needed.
 * This is useful for:
 * - Multilingual conversations
 * - Code-switched speech (e.g., Spanish-English)
 * - International meetings or interviews
 */
public class TranscribeMultiLanguageSample {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        String endpoint = System.getenv("SPEECH_ENDPOINT");
        String apiKey = System.getenv("SPEECH_API_KEY");

        if (endpoint == null || apiKey == null) {
            System.err.println("Please set SPEECH_ENDPOINT and SPEECH_API_KEY environment variables.");
            System.exit(1);
        }

        System.out.println("Azure AI Speech Transcription - Multi-Language Sample");
        System.out.println("=====================================================\n");

        // Create the transcription client
        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();

        try {
            // Load audio file
            String audioFilePath = "src/samples/assets/sample-audio.wav";
            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));
            AudioFileDetails fileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
                .setFilename(audioFilePath);

            // Configure transcription WITHOUT specifying locales
            // This allows the service to auto-detect and transcribe multiple languages
            // within the same audio file, switching between them as needed
            TranscriptionOptions options = new TranscriptionOptions(fileDetails);

            System.out.println("Transcribing with automatic multi-language detection...");
            System.out.println("(No locale specified - service will detect all languages)\n");

            // Transcribe the audio
            TranscriptionResult result = client.transcribe(options);

            // Display results
            System.out.println("Transcription Results:");
            System.out.println("---------------------");
            System.out.println("Duration: " + result.getDuration());
            System.out.println("Total phrases found: " + (result.getPhrases() != null ? result.getPhrases().size() : 0));
            System.out.println("Total combined phrases: " + (result.getCombinedPhrases() != null ? result.getCombinedPhrases().size() : 0));
            System.out.println();

            // Show detailed phrases with timestamps
            if (result.getPhrases() != null && !result.getPhrases().isEmpty()) {
                System.out.println("Detailed Phrases:");
                System.out.println("-----------------");

                for (int i = 0; i < result.getPhrases().size(); i++) {
                    TranscribedPhrase phrase = result.getPhrases().get(i);
                    long offsetMs = phrase.getOffset();
                    long durationMs = phrase.getDuration().toMillis();

                    System.out.println("\n[Phrase " + (i + 1) + "] " + offsetMs + "ms - " + (offsetMs + durationMs) + "ms");
                    System.out.println("Locale: " + phrase.getLocale());
                    System.out.println("Text: " + phrase.getText());
                }
            }

            // Also show combined phrases per channel
            if (result.getCombinedPhrases() != null && !result.getCombinedPhrases().isEmpty()) {
                System.out.println("\n\nCombined Transcription (All Languages):");
                System.out.println("========================================");
                result.getCombinedPhrases().forEach(phrase -> {
                    System.out.println(phrase.getText());
                });
            }

            System.out.println("\nNote: When no locales are specified, the service transcribes all languages");
            System.out.println("present in the audio. However, the locale field in each phrase may not always");
            System.out.println("accurately reflect the actual language of that specific phrase.");        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
