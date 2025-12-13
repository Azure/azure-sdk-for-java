// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Simplest possible example of transcribing an audio file.
 *
 * This sample demonstrates the absolute minimum code needed to:
 * 1. Create a client
 * 2. Load an audio file
 * 3. Transcribe it
 * 4. Get the text result
 */
public class TranscribeAudioFileSample {
    public static void main(String[] args) {
        try {
            // Get credentials from environment variables
            String endpoint = System.getenv("SPEECH_ENDPOINT");
            String apiKey = System.getenv("SPEECH_API_KEY");

            // Create client
            TranscriptionClient client = new TranscriptionClientBuilder()
                .endpoint(endpoint)
                .credential(new KeyCredential(apiKey))
                .buildClient();

            // Load audio file
            String audioFilePath = "src/samples/assets/sample-audio.wav";
            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));

            // Create audio file details
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
                .setFilename(audioFilePath);

            // Transcribe
            TranscriptionOptions options = new TranscriptionOptions(audioFileDetails);
            TranscriptionResult result = client.transcribe(options);

            // Print result
            System.out.println("Transcription:");
            result.getCombinedPhrases().forEach(phrase ->
                System.out.println(phrase.getText())
            );

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
