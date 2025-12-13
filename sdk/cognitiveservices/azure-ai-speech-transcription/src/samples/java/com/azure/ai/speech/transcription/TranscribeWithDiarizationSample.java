// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.TranscribedPhrase;
import com.azure.ai.speech.transcription.models.TranscriptionDiarizationOptions;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Sample demonstrates using speaker diarization to identify different speakers in audio.
 *
 * Speaker diarization detects and separates different speakers in the audio, labeling
 * each transcribed segment with a speaker ID. This is useful for:
 * - Meeting transcriptions
 * - Interview recordings
 * - Multi-person conversations
 * - Podcast transcriptions
 */
public class TranscribeWithDiarizationSample {
    public static void main(String[] args) {
        String endpoint = System.getenv("SPEECH_ENDPOINT");
        String apiKey = System.getenv("SPEECH_API_KEY");

        if (endpoint == null || apiKey == null) {
            System.err.println("Please set SPEECH_ENDPOINT and SPEECH_API_KEY environment variables");
            return;
        }

        System.out.println("Azure AI Speech Transcription - Speaker Diarization Sample");
        System.out.println("===========================================================\n");

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

            // Configure speaker diarization
            TranscriptionDiarizationOptions diarizationOptions = new TranscriptionDiarizationOptions()
                .setMaxSpeakers(5); // Maximum number of speakers to detect (2-36)

            TranscriptionOptions options = new TranscriptionOptions(audioFileDetails)
                .setDiarizationOptions(diarizationOptions);

            System.out.println("Transcribing with speaker diarization (max 5 speakers)...\n");

            // Transcribe with diarization
            TranscriptionResult result = client.transcribe(options);

            // Display results organized by speaker
            System.out.println("Transcription with Speaker Information:");
            System.out.println("----------------------------------------");

            if (result.getPhrases() != null && !result.getPhrases().isEmpty()) {
                for (TranscribedPhrase phrase : result.getPhrases()) {
                    int speakerId = phrase.getSpeaker() != null ? phrase.getSpeaker() : 0;
                    double startTime = phrase.getOffset() / 1000.0;
                    double endTime = (phrase.getOffset() + phrase.getDuration().toMillis()) / 1000.0;

                    System.out.println(String.format("\n[Speaker %d] (%.2fs - %.2fs)",
                        speakerId, startTime, endTime));
                    System.out.println(phrase.getText());
                }
            }


        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
