// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Source code snippets from this file are embedded in Transcription SDK JavaDoc (API documentation).

package com.azure.ai.speech.transcription.javadoccodesnippets;

import com.azure.ai.speech.transcription.TranscriptionAsyncClient;
import com.azure.ai.speech.transcription.TranscriptionClientBuilder;
import com.azure.ai.speech.transcription.models.AudioFileDetails;
import com.azure.ai.speech.transcription.models.ProfanityFilterMode;
import com.azure.ai.speech.transcription.models.TranscribeRequestContent;
import com.azure.ai.speech.transcription.models.TranscriptionDiarizationOptions;
import com.azure.ai.speech.transcription.models.TranscriptionOptions;
import com.azure.ai.speech.transcription.models.TranscriptionResult;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Code snippets for {@link TranscriptionAsyncClient} JavaDoc documentation.
 */
public class TranscriptionAsyncClientJavaDocCodeSnippets {

    private static String endpoint = System.getenv("SPEECH_ENDPOINT");
    private static String key = System.getenv("SPEECH_API_KEY");

    /**
     * Sample for creating an asynchronous TranscriptionAsyncClient with API key authentication.
     */
    public void createAsyncClientWithApiKey() {
        // BEGIN: com.azure.ai.speech.transcription.transcriptionasyncclient.instantiation.apikey
        TranscriptionAsyncClient asyncClient = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildAsyncClient();
        // END: com.azure.ai.speech.transcription.transcriptionasyncclient.instantiation.apikey
    }

    /**
     * Sample for transcribing audio asynchronously using subscribe pattern.
     */
    public void transcribeAsyncWithSubscribe() throws Exception {
        TranscriptionAsyncClient asyncClient = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildAsyncClient();

        byte[] audioData = Files.readAllBytes(Paths.get("sample.wav"));
        AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
            .setFilename("sample.wav");
        TranscriptionOptions options = new TranscriptionOptions();
        TranscribeRequestContent requestContent = new TranscribeRequestContent()
            .setAudio(audioFileDetails)
            .setOptions(options);

        // BEGIN: com.azure.ai.speech.transcription.transcriptionasyncclient.transcribe.subscribe
        CountDownLatch latch = new CountDownLatch(1);

        asyncClient.transcribe(requestContent)
            .subscribe(
                // onNext: Process result
                result -> {
                    System.out.println("Duration: " + result.getDurationMilliseconds() + " ms");
                    if (result.getCombinedPhrases() != null) {
                        result.getCombinedPhrases().forEach(phrase ->
                            System.out.println("Text: " + phrase.getText())
                        );
                    }
                    latch.countDown();
                },
                // onError: Handle error
                error -> {
                    System.err.println("Error: " + error.getMessage());
                    latch.countDown();
                },
                // onComplete: Completion handler
                () -> System.out.println("Transcription completed")
            );

        latch.await(60, TimeUnit.SECONDS);
        // END: com.azure.ai.speech.transcription.transcriptionasyncclient.transcribe.subscribe
    }

    /**
     * Sample for transcribing audio asynchronously using block pattern.
     */
    public void transcribeAsyncWithBlock() throws Exception {
        TranscriptionAsyncClient asyncClient = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildAsyncClient();

        byte[] audioData = Files.readAllBytes(Paths.get("sample.wav"));
        AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
            .setFilename("sample.wav");
        TranscriptionOptions options = new TranscriptionOptions();
        TranscribeRequestContent requestContent = new TranscribeRequestContent()
            .setAudio(audioFileDetails)
            .setOptions(options);

        // BEGIN: com.azure.ai.speech.transcription.transcriptionasyncclient.transcribe.block
        // Use block() to convert async call to sync
        TranscriptionResult result = asyncClient.transcribe(requestContent).block();

        if (result != null) {
            System.out.println("Duration: " + result.getDurationMilliseconds() + " ms");
        }
        // END: com.azure.ai.speech.transcription.transcriptionasyncclient.transcribe.block
    }

    /**
     * Sample for transcribing audio asynchronously with advanced options.
     */
    public void transcribeAsyncWithOptions() throws Exception {
        TranscriptionAsyncClient asyncClient = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildAsyncClient();

        // BEGIN: com.azure.ai.speech.transcription.transcriptionasyncclient.transcribe.options
        byte[] audioData = Files.readAllBytes(Paths.get("sample.wav"));

        AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
            .setFilename("sample.wav");

        // Configure advanced transcription options
        TranscriptionOptions options = new TranscriptionOptions()
            .setLocales(Arrays.asList("en-US", "es-ES"))
            .setProfanityFilterMode(ProfanityFilterMode.MASKED)
            .setDiarizationOptions(new TranscriptionDiarizationOptions()
                .setEnabled(true)
                .setMaxSpeakers(5));

        TranscribeRequestContent requestContent = new TranscribeRequestContent()
            .setAudio(audioFileDetails)
            .setOptions(options);

        // Transcribe asynchronously
        Mono<TranscriptionResult> resultMono = asyncClient.transcribe(requestContent);

        // Process result
        resultMono.subscribe(result -> {
            if (result.getPhrases() != null) {
                result.getPhrases().forEach(phrase -> {
                    System.out.printf("Speaker %d: %s%n",
                        phrase.getSpeaker(), phrase.getText());
                });
            }
        });
        // END: com.azure.ai.speech.transcription.transcriptionasyncclient.transcribe.options
    }

    /**
     * Sample for transcribing audio asynchronously with timeout and error handling.
     */
    public void transcribeAsyncWithTimeoutAndErrorHandling() throws Exception {
        TranscriptionAsyncClient asyncClient = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildAsyncClient();

        byte[] audioData = Files.readAllBytes(Paths.get("sample.wav"));
        AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
            .setFilename("sample.wav");
        TranscriptionOptions options = new TranscriptionOptions();
        TranscribeRequestContent requestContent = new TranscribeRequestContent()
            .setAudio(audioFileDetails)
            .setOptions(options);

        // BEGIN: com.azure.ai.speech.transcription.transcriptionasyncclient.transcribe.timeout
        Mono<TranscriptionResult> resultMono = asyncClient.transcribe(requestContent)
            .timeout(Duration.ofMinutes(2))
            .doOnError(error -> System.err.println("Error: " + error.getMessage()))
            .onErrorResume(error -> {
                System.err.println("Fallback: Returning empty result");
                return Mono.empty();
            });

        TranscriptionResult result = resultMono.block();
        // END: com.azure.ai.speech.transcription.transcriptionasyncclient.transcribe.timeout
    }

    /**
     * Sample for processing detailed async transcription results.
     */
    public void processDetailedAsyncResults() throws Exception {
        TranscriptionAsyncClient asyncClient = new TranscriptionClientBuilder()
            .endpoint(endpoint)
            .credential(new KeyCredential(key))
            .buildAsyncClient();

        byte[] audioData = Files.readAllBytes(Paths.get("sample.wav"));
        AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData));
        TranscriptionOptions options = new TranscriptionOptions();
        TranscribeRequestContent requestContent = new TranscribeRequestContent()
            .setAudio(audioFileDetails)
            .setOptions(options);

        // BEGIN: com.azure.ai.speech.transcription.transcriptionasyncclient.results.detailed
        asyncClient.transcribe(requestContent)
            .subscribe(result -> {
                // Access combined phrases
                if (result.getCombinedPhrases() != null) {
                    result.getCombinedPhrases().forEach(channelPhrase ->
                        System.out.printf("[Channel %d] %s%n",
                            channelPhrase.getChannel(), channelPhrase.getText())
                    );
                }

                // Access detailed phrases with word-level timing
                if (result.getPhrases() != null) {
                    result.getPhrases().forEach(phrase -> {
                        System.out.printf("Phrase (%.2f-%.2fs): %s%n",
                            phrase.getOffsetMilliseconds() / 1000.0,
                            (phrase.getOffsetMilliseconds() + phrase.getDurationMilliseconds()) / 1000.0,
                            phrase.getText());

                        if (phrase.getWords() != null) {
                            phrase.getWords().forEach(word ->
                                System.out.printf("  \"%s\" at %.2fs%n",
                                    word.getText(),
                                    word.getOffsetMilliseconds() / 1000.0)
                            );
                        }
                    });
                }
            });
        // END: com.azure.ai.speech.transcription.transcriptionasyncclient.results.detailed
    }
}

