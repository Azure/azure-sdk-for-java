// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.AzureKeyCredential;
import java.util.Arrays;

/**
 * Sample demonstrates how to transcribe audio from a URL.
 */
public class TranscribeFromUrlSample {

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

        // Create the transcription client
        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();

        System.out.println("Azure AI Speech Transcription - Transcribe from URL Sample");
        System.out.println("============================================================\n");

        // Audio file URL (must be publicly accessible)
        // Using sample audio from Azure documentation
        String audioUrl = "https://raw.githubusercontent.com/Azure-Samples/cognitive-services-speech-sdk/master/sampledata/audiofiles/aboutSpeechSdk.wav";

        System.out.println("Transcribing audio from URL: " + audioUrl);
        System.out.println();

        // Create transcription options with audio URL
        TranscriptionOptions options = new TranscriptionOptions(audioUrl)
            .setLocales(Arrays.asList("en-US"));

        // Transcribe the audio from URL
        TranscriptionResult result = client.transcribe(options);

        // Display results
        System.out.println("Transcription Results:");
        System.out.println("---------------------");
        System.out.println("Duration: " + result.getDuration() + "\n");

        if (result.getCombinedPhrases() != null && !result.getCombinedPhrases().isEmpty()) {
            System.out.println("Combined text: " + result.getCombinedPhrases().get(0).getText());
        }

        System.out.println();
    }
}
