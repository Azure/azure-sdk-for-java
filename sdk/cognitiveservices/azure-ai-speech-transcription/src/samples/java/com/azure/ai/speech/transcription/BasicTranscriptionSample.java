// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

// BEGIN: com.azure.ai.speech.transcription.basic.imports
import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.TranscribeRequestContent;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
// END: com.azure.ai.speech.transcription.basic.imports

/**
 * Sample demonstrates the most basic usage of Azure AI Speech Transcription client to transcribe
 * an audio file with default settings.
 *
 * This sample shows:
 * - Creating a TranscriptionClient with endpoint and API key (or Azure AD token)
 * - Reading an audio file from disk
 * - Transcribing the audio with default options
 * - Accessing transcription results
 */
public class BasicTranscriptionSample {
    /**
     * Main method to run the basic transcription sample.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        // PREREQUISITE: Set environment variables for authentication
        // 
        // Option 1: API Key Authentication
        // set SPEECH_ENDPOINT=https://your-resource-name.cognitiveservices.azure.com/
        // set SPEECH_API_KEY=your-api-key
        //
        // Option 2: Azure AD Authentication (recommended for production)
        // set SPEECH_ENDPOINT=https://your-resource-name.cognitiveservices.azure.com/
        // Ensure you have configured Azure AD credentials (managed identity, service principal, etc.)

        String endpoint = System.getenv("SPEECH_ENDPOINT");
        String apiKey = System.getenv("SPEECH_API_KEY");

        if (endpoint == null) {
            System.err.println("Please set SPEECH_ENDPOINT environment variable");
            System.err.println("Example:");
            System.err.println("  set SPEECH_ENDPOINT=https://your-resource-name.cognitiveservices.azure.com/");
            System.err.println("\nFor authentication, choose one of:");
            System.err.println("  Option 1: set SPEECH_API_KEY=your-api-key");
            System.err.println("  Option 2: Configure Azure AD credentials (DefaultAzureCredential)");
            return;
        }

        // Create the transcription client with appropriate authentication
        TranscriptionClient client;
        
        if (apiKey != null && !apiKey.isEmpty()) {
            // BEGIN: com.azure.ai.speech.transcription.basic.create-client.apikey
            // Option 1: Use API Key authentication
            client = new TranscriptionClientBuilder()
                .endpoint(endpoint)
                .credential(new KeyCredential(apiKey))
                .buildClient();
            // END: com.azure.ai.speech.transcription.basic.create-client.apikey
            System.out.println("Using API Key authentication");
        } else {
            // BEGIN: com.azure.ai.speech.transcription.basic.create-client.azuread
            // Option 2: Use Azure AD authentication (recommended for production)
            TokenCredential credential = new DefaultAzureCredentialBuilder().build();
            client = new TranscriptionClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();
            // END: com.azure.ai.speech.transcription.basic.create-client.azuread
            System.out.println("Using Azure AD authentication");
        }

        System.out.println("Azure AI Speech Transcription - Basic Sample");
        System.out.println("==============================================\n");

        try {
            // For this sample, you need an audio file. Common formats include:
            // - WAV (16 kHz, 16-bit, mono PCM recommended)
            // - MP3, OGG, FLAC, etc.
            // Maximum file size: 250 MB
            // Maximum duration: 2 hours

            String audioFilePath = "sample-audio.wav";
            if (!Files.exists(Paths.get(audioFilePath))) {
                System.err.println("Audio file not found: " + audioFilePath);
                System.err.println("Please provide a valid audio file path.");
                return;
            }

            // BEGIN: com.azure.ai.speech.transcription.basic.read-audio-file
            // Read the audio file
            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));
            System.out.println("Audio file loaded: " + audioFilePath);
            System.out.println("File size: " + audioData.length + " bytes\n");
            // END: com.azure.ai.speech.transcription.basic.read-audio-file

            // BEGIN: com.azure.ai.speech.transcription.basic.transcribe
            // Create audio file details
            AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
                .setFilename(audioFilePath);

            // Create transcription options (using defaults)
            TranscriptionOptions options = new TranscriptionOptions();

            // Create the transcribe request
            TranscribeRequestContent requestContent = new TranscribeRequestContent()
                .setAudio(audioFileDetails)
                .setOptions(options);

            System.out.println("Starting transcription...");

            // Transcribe the audio
            TranscriptionResult result = client.transcribe(requestContent);
            // END: com.azure.ai.speech.transcription.basic.transcribe

            // BEGIN: com.azure.ai.speech.transcription.basic.process-results
            // Display results
            System.out.println("\nTranscription Results:");
            System.out.println("----------------------");
            System.out.println("Duration: " + result.getDurationMilliseconds() + " ms");

            if (result.getCombinedPhrases() != null && !result.getCombinedPhrases().isEmpty()) {
                System.out.println("\nTranscribed Text:");
                result.getCombinedPhrases().forEach(phrase -> {
                    System.out.println("  Channel " + phrase.getChannel() + ": " + phrase.getText());
                });
            } else {
                System.out.println("No transcription results found.");
            }
            // END: com.azure.ai.speech.transcription.basic.process-results

            System.out.println("\nTranscription completed successfully!");

        } catch (IOException e) {
            System.err.println("Error reading audio file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error during transcription: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
