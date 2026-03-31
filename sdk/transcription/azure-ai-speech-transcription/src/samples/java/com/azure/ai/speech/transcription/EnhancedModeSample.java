// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.speech.transcription;

// BEGIN: com.azure.ai.speech.transcription.enhancedmode.imports
import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.EnhancedModeOptions;
import com.azure.ai.speech.transcription.models.ProfanityFilterMode;
import com.azure.ai.speech.transcription.models.TranscribedPhrase;
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
 * Enhanced Mode for Transcription.
 *
 * <p>Enhanced Mode uses LLM-powered speech recognition to provide improved transcription accuracy,
 * real-time translation, prompt-based customization, and multilingual support with GPU acceleration.</p>
 *
 * <h2>Supported Tasks</h2>
 * <ul>
 *   <li>{@code transcribe} - Transcribe audio in the input language (auto-detected or specified)</li>
 *   <li>{@code translate} - Translate audio to a specified target language</li>
 * </ul>
 *
 * <p>This sample demonstrates:</p>
 * <ul>
 *   <li>Transcribe with Enhanced Mode</li>
 *   <li>Translate an Audio File with Enhanced Mode</li>
 *   <li>Enhanced Mode with Prompt Tuning</li>
 *   <li>Combine Enhanced Mode with Other Options (diarization, profanity filter)</li>
 * </ul>
 *
 * <h2>Limitations</h2>
 * <ul>
 *   <li>{@code confidence} is not available and always returns 0</li>
 *   <li>Word-level timing ({@code offsetMilliseconds}, {@code durationMilliseconds}) is not supported for the {@code translate} task</li>
 *   <li>Diarization is not supported for the {@code translate} task (only speaker1 label is returned)</li>
 *   <li>{@code locales} and {@code phraseLists} options are not required or applicable with Enhanced Mode</li>
 * </ul>
 */
public class EnhancedModeSample {

    private static TranscriptionClient client;
    private static final String AUDIO_FILE_PATH = "src/samples/assets/sample-whatstheweatherlike-en.mp3";

    /**
     * Main method to run the enhanced mode samples.
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

        client = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(apiKey))
            .buildClient();

        System.out.println("Azure AI Speech Transcription - Enhanced Mode Sample");
        System.out.println("=====================================================\n");

        // 1. Transcribe with Enhanced Mode
        demonstrateTranscribeWithEnhancedMode();

        // 2. Translate an Audio File with Enhanced Mode
        demonstrateTranslateWithEnhancedMode();

        // 3. Enhanced Mode with Prompt Tuning
        demonstrateEnhancedModeWithPrompts();

        // 4. Combine Enhanced Mode with Other Options
        demonstrateEnhancedModeWithDiarization();
    }

    /**
     * Demonstrates using Enhanced Mode for improved transcription quality.
     * Enhanced mode is automatically enabled when using EnhancedModeOptions.
     */
    private static void demonstrateTranscribeWithEnhancedMode() {
        System.out.println("1. Transcribe with Enhanced Mode");
        System.out.println("---------------------------------");

        try {
            byte[] audioData = readAudioFile();
            if (audioData == null) {
                return;
            }

            TranscriptionResult result = transcribeWithEnhancedMode(audioData);

            System.out.println("Transcription result:");
            if (result.getCombinedPhrases() != null && !result.getCombinedPhrases().isEmpty()) {
                System.out.println(result.getCombinedPhrases().get(0).getText());
            }
            System.out.println();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage() + "\n");
        }
    }

    // BEGIN: com.azure.ai.speech.transcription.enhancedmode.transcribe
    /**
     * Transcribe audio with Enhanced Mode for improved quality.
     */
    private static TranscriptionResult transcribeWithEnhancedMode(byte[] audioData) {
        AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData));

        // Enhanced mode is automatically enabled
        EnhancedModeOptions enhancedMode = new EnhancedModeOptions()
            .setTask("transcribe");

        TranscriptionOptions options = new TranscriptionOptions(audioFileDetails)
            .setEnhancedModeOptions(enhancedMode);

        return client.transcribe(options);
    }
    // END: com.azure.ai.speech.transcription.enhancedmode.transcribe

    /**
     * Demonstrates translating speech to a target language during transcription.
     * Specify the target language using the language code (e.g., "en" for English,
     * "ko" for Korean, "es" for Spanish, "zh" for Chinese).
     */
    private static void demonstrateTranslateWithEnhancedMode() {
        System.out.println("2. Translate an Audio File with Enhanced Mode");
        System.out.println("----------------------------------------------");

        try {
            byte[] audioData = readAudioFile();
            if (audioData == null) {
                return;
            }

            TranscriptionResult result = translateWithEnhancedMode(audioData, "zh");

            System.out.println("Translated to Chinese:");
            if (result.getCombinedPhrases() != null && !result.getCombinedPhrases().isEmpty()) {
                System.out.println(result.getCombinedPhrases().get(0).getText());
            }
            System.out.println();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage() + "\n");
        }
    }

    // BEGIN: com.azure.ai.speech.transcription.enhancedmode.translate
    /**
     * Translate speech to a target language during transcription.
     *
     * @param audioData the audio data to translate
     * @param targetLanguage the target language code (e.g., "en", "ko", "es", "zh")
     */
    private static TranscriptionResult translateWithEnhancedMode(byte[] audioData, String targetLanguage) {
        AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData));

        // Translate speech to target language
        EnhancedModeOptions enhancedMode = new EnhancedModeOptions()
            .setTask("translate")
            .setTargetLanguage(targetLanguage);

        TranscriptionOptions options = new TranscriptionOptions(audioFileDetails)
            .setEnhancedModeOptions(enhancedMode);

        return client.transcribe(options);
    }
    // END: com.azure.ai.speech.transcription.enhancedmode.translate

    /**
     * Demonstrates using prompts to improve recognition or control output format.
     * Prompts are optional text that guides the output style for transcribe or translate tasks.
     */
    private static void demonstrateEnhancedModeWithPrompts() {
        System.out.println("3. Enhanced Mode with Prompt Tuning");
        System.out.println("------------------------------------");

        try {
            byte[] audioData = readAudioFile();
            if (audioData == null) {
                return;
            }

            TranscriptionResult result = transcribeWithPrompts(audioData);

            System.out.println("Transcription with prompts:");
            if (result.getCombinedPhrases() != null && !result.getCombinedPhrases().isEmpty()) {
                System.out.println(result.getCombinedPhrases().get(0).getText());
            }
            System.out.println();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage() + "\n");
        }
    }

    // BEGIN: com.azure.ai.speech.transcription.enhancedmode.prompts
    /**
     * Transcribe with prompts to guide output formatting.
     */
    private static TranscriptionResult transcribeWithPrompts(byte[] audioData) {
        AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData));

        // Guide output formatting using prompts
        EnhancedModeOptions enhancedMode = new EnhancedModeOptions()
            .setTask("transcribe")
            .setPrompts(Arrays.asList("Output must be in lexical format."));

        TranscriptionOptions options = new TranscriptionOptions(audioFileDetails)
            .setEnhancedModeOptions(enhancedMode);

        return client.transcribe(options);
    }
    // END: com.azure.ai.speech.transcription.enhancedmode.prompts

    /**
     * Demonstrates combining Enhanced Mode with other transcription options like
     * diarization, profanityFilterMode, and channels for comprehensive transcription
     * scenarios such as meeting transcription.
     *
     * <p>Note: Diarization is only supported for the "transcribe" task, not for "translate".</p>
     */
    private static void demonstrateEnhancedModeWithDiarization() {
        System.out.println("4. Combine Enhanced Mode with Other Options");
        System.out.println("--------------------------------------------");

        try {
            byte[] audioData = readAudioFile();
            if (audioData == null) {
                return;
            }

            TranscriptionResult result = transcribeWithDiarization(audioData);

            System.out.println("Transcription with diarization:");
            if (result.getPhrases() != null) {
                for (TranscribedPhrase phrase : result.getPhrases()) {
                    System.out.println("[Speaker " + phrase.getSpeaker() + "] " + phrase.getText());
                }
            }
            System.out.println();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage() + "\n");
        }
    }

    // BEGIN: com.azure.ai.speech.transcription.enhancedmode.diarization
    /**
     * Combine Enhanced Mode with diarization and profanity filtering.
     * Note: Diarization is only supported for the "transcribe" task, not for "translate".
     */
    private static TranscriptionResult transcribeWithDiarization(byte[] audioData) {
        AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData));

        EnhancedModeOptions enhancedMode = new EnhancedModeOptions()
            .setTask("transcribe")
            .setPrompts(Arrays.asList("Output must be in lexical format."));

        TranscriptionOptions options = new TranscriptionOptions(audioFileDetails)
            .setEnhancedModeOptions(enhancedMode)
            .setProfanityFilterMode(ProfanityFilterMode.MASKED)
            .setDiarizationOptions(new TranscriptionDiarizationOptions()
                .setMaxSpeakers(2));

        return client.transcribe(options);
    }
    // END: com.azure.ai.speech.transcription.enhancedmode.diarization

    /**
     * Helper method to read audio file.
     */
    private static byte[] readAudioFile() {
        try {
            if (!Files.exists(Paths.get(AUDIO_FILE_PATH))) {
                System.out.println("Audio file not found: " + AUDIO_FILE_PATH);
                System.out.println("Skipping this example.\n");
                return null;
            }
            return Files.readAllBytes(Paths.get(AUDIO_FILE_PATH));
        } catch (IOException e) {
            System.err.println("Error reading audio file: " + e.getMessage() + "\n");
            return null;
        }
    }
}
