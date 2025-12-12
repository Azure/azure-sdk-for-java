// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.PhraseListOptions;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Sample demonstrates using a phrase list to improve recognition accuracy for specific terms.
 *
 * Phrase lists help the speech service better recognize domain-specific terminology,
 * proper nouns, and uncommon words that might otherwise be misrecognized.
 */
public class TranscribeWithPhraseListSample {
    public static void main(String[] args) {
        String endpoint = System.getenv("SPEECH_ENDPOINT");
        String apiKey = System.getenv("SPEECH_API_KEY");

        if (endpoint == null || apiKey == null) {
            System.err.println("Please set SPEECH_ENDPOINT and SPEECH_API_KEY environment variables");
            return;
        }

        System.out.println("Azure AI Speech Transcription - Phrase List Sample");
        System.out.println("====================================================\n");

        // Create client
        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .buildClient();

        try {
            // Load audio file
            String audioFilePath = "src/samples/assets/sample-audio.wav";
            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
                .setFilename(audioFilePath);

            // Create phrase list with custom terms
            // Add phrases that appear in your audio for better recognition
            PhraseListOptions phraseListOptions = new PhraseListOptions()
                .setPhrases(Arrays.asList(
                    "Mary",
                    "El Mundo",
                    "Secret Garden",
                    "empleada doméstica",
                    "habitación"
                ))
                .setBiasingWeight(5.0); // Weight range: 1.0-20.0 (higher = stronger bias)

            // Create transcription options with phrase list
            TranscriptionOptions options = new TranscriptionOptions(audioFileDetails)
                .setPhraseListOptions(phraseListOptions);

            System.out.println("Custom phrase list:");
            phraseListOptions.getPhrases().forEach(phrase ->
                System.out.println("  - " + phrase)
            );
            System.out.println("\nBiasing weight: " + phraseListOptions.getBiasingWeight());
            System.out.println("\nTranscribing with phrase list...\n");

            // Transcribe
            TranscriptionResult result = client.transcribe(options);

            System.out.println("Transcription result:");
            System.out.println("---------------------");
            result.getCombinedPhrases().forEach(phrase ->
                System.out.println(phrase.getText())
            );

            // Print individual phrases with timing information
            if (result.getPhrases() != null && !result.getPhrases().isEmpty()) {
                System.out.println("\nDetailed phrases:");
                result.getPhrases().forEach(phrase ->
                    System.out.println(String.format("  [%dms]: %s",
                        phrase.getOffset(),
                        phrase.getText()))
                );
            }

            System.out.println("\n Transcription completed successfully!");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
