// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.ProfanityFilterMode;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Sample demonstrates profanity filtering in Azure AI Speech Transcription.
 * Shows the difference between NONE (raw), MASKED (f***), REMOVED (omitted), and TAGS (XML tagged).
 */
public class TranscribeWithProfanityFilterSample {
    /**
     * Main method to run the profanity filter sample.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("Azure AI Speech Transcription - Profanity Filter Sample");
        System.out.println("==========================================================\n");

        String endpoint = System.getenv("SPEECH_ENDPOINT");
        String apiKey = System.getenv("SPEECH_API_KEY");

        if (endpoint == null || apiKey == null) {
            System.err.println("Please set SPEECH_ENDPOINT and SPEECH_API_KEY environment variables");
            return;
        }

        try {
            // Create the transcription client
            TranscriptionClient client = new TranscriptionClientBuilder()
                .endpoint(endpoint)
                .credential(new KeyCredential(apiKey))
                .buildClient();

            // Load audio file
            String audioFilePath = "src/samples/assets/sample-profanity.wav";
            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData));

            // Demonstrate different profanity filter modes
            ProfanityFilterMode[] modes = {
                ProfanityFilterMode.NONE,
                ProfanityFilterMode.MASKED,
                ProfanityFilterMode.REMOVED,
                ProfanityFilterMode.TAGS
            };

            for (ProfanityFilterMode mode : modes) {
                System.out.println("Transcribing with profanity filter mode: " + mode);
                System.out.println("----------------------------------------------");

                // Create transcription options with profanity filter
                TranscriptionOptions options = new TranscriptionOptions(audioFileDetails)
                    .setProfanityFilterMode(mode);

                // Perform transcription
                TranscriptionResult result = client.transcribe(options);

                // Display results
                System.out.println("Combined text: " + result.getCombinedPhrases().get(0).getText());
                System.out.println();
            }


        } catch (Exception e) {
            System.err.println("Error during transcription: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
