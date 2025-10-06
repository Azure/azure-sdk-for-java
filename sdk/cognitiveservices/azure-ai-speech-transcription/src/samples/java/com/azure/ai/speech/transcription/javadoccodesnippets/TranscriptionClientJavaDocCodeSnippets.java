// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Source code snippets from this file are embedded in Transcription SDK JavaDoc (API documentation).

package com.azure.ai.speech.transcription.javadoccodesnippets;

import com.azure.ai.speech.transcription.TranscriptionClient;
import com.azure.ai.speech.transcription.TranscriptionClientBuilder;
import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.ProfanityFilterMode;
import com.azure.ai.speech.transcription.models.TranscribeRequestContent;
import com.azure.ai.speech.transcription.models.TranscriptionDiarizationOptions;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Code snippets for {@link TranscriptionClient} JavaDoc documentation.
 */
public class TranscriptionClientJavaDocCodeSnippets {

    private static String endpoint = System.getenv("SPEECH_ENDPOINT");
    private static String key = System.getenv("SPEECH_API_KEY");

    /**
     * Sample for creating a synchronous TranscriptionClient with API key authentication.
     */
    public void createClientWithApiKey() {
        // BEGIN: com.azure.ai.speech.transcription.transcriptionclient.instantiation.apikey
        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildClient();
        // END: com.azure.ai.speech.transcription.transcriptionclient.instantiation.apikey
    }

    /**
     * Sample for transcribing audio from a file with default options.
     */
    public void transcribeFromFile() throws Exception {
        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildClient();

        // BEGIN: com.azure.ai.speech.transcription.transcriptionclient.transcribe.file
        // Read audio file
        byte[] audioData = Files.readAllBytes(Paths.get("sample.wav"));

        // Create audio file details
        AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
            .setFilename("sample.wav");

        // Create transcription options with default settings
        TranscriptionOptions options = new TranscriptionOptions();

        // Create transcribe request
        TranscribeRequestContent requestContent = new TranscribeRequestContent()
            .setAudio(audioFileDetails)
            .setOptions(options);

        // Transcribe audio
        TranscriptionResult result = client.transcribe(requestContent);

        // Process results
        System.out.println("Duration: " + result.getDurationMilliseconds() + " ms");
        result.getCombinedPhrases().forEach(phrase -> {
            System.out.println("Channel " + phrase.getChannel() + ": " + phrase.getText());
        });
        // END: com.azure.ai.speech.transcription.transcriptionclient.transcribe.file
    }

    /**
     * Sample for transcribing audio with advanced options.
     */
    public void transcribeWithOptions() throws Exception {
        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildClient();

        // BEGIN: com.azure.ai.speech.transcription.transcriptionclient.transcribe.options
        byte[] audioData = Files.readAllBytes(Paths.get("sample.wav"));

        AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
            .setFilename("sample.wav");

        // Configure advanced transcription options
        TranscriptionOptions options = new TranscriptionOptions()
            .setLocales(Arrays.asList("en-US", "es-ES"))
            .setProfanityFilterMode(ProfanityFilterMode.MASKED)
            .setDiarization(new TranscriptionDiarizationOptions()
                .setEnabled(true)
                .setMaxSpeakers(5));

        TranscribeRequestContent requestContent = new TranscribeRequestContent()
            .setAudio(audioFileDetails)
            .setOptions(options);

        TranscriptionResult result = client.transcribe(requestContent);

        // Access detailed results
        if (result.getPhrases() != null) {
            result.getPhrases().forEach(phrase -> {
                System.out.printf("Speaker %d: %s%n",
                    phrase.getSpeaker(), phrase.getText());
            });
        }
        // END: com.azure.ai.speech.transcription.transcriptionclient.transcribe.options
    }

    /**
     * Sample for processing detailed transcription results with word-level timing.
     */
    public void processDetailedResults() throws Exception {
        TranscriptionClient client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildClient();

        byte[] audioData = Files.readAllBytes(Paths.get("sample.wav"));
        AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData));
        TranscriptionOptions options = new TranscriptionOptions();
        TranscribeRequestContent requestContent = new TranscribeRequestContent()
            .setAudio(audioFileDetails)
            .setOptions(options);
        TranscriptionResult result = client.transcribe(requestContent);

        // BEGIN: com.azure.ai.speech.transcription.transcriptionclient.results.detailed
        // Access sentence-level combined phrases
        if (result.getCombinedPhrases() != null) {
            result.getCombinedPhrases().forEach(channelPhrase -> {
                System.out.printf("[Channel %d] %s%n",
                    channelPhrase.getChannel(), channelPhrase.getText());
            });
        }

        // Access word-level details with timing
        if (result.getPhrases() != null) {
            result.getPhrases().forEach(phrase -> {
                System.out.printf("Phrase (%.2f-%.2fs): %s%n",
                    phrase.getOffsetMilliseconds() / 1000.0,
                    (phrase.getOffsetMilliseconds() + phrase.getDurationMilliseconds()) / 1000.0,
                    phrase.getText());

                // Get word-level timing information
                if (phrase.getWords() != null) {
                    phrase.getWords().forEach(word -> {
                        System.out.printf("  Word: \"%s\" at %.2fs%n",
                            word.getText(),
                            word.getOffsetMilliseconds() / 1000.0);
                    });
                }
            });
        }
        // END: com.azure.ai.speech.transcription.transcriptionclient.results.detailed
    }
}
