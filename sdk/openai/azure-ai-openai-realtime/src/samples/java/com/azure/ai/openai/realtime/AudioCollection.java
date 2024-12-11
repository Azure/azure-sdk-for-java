// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.utils.AudioFile;
import com.azure.ai.openai.realtime.utils.FileUtils;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionModel;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionSettings;
import com.azure.ai.openai.realtime.models.SessionUpdateEvent;
import com.azure.ai.openai.realtime.models.RealtimeRequestSession;
import com.azure.ai.openai.realtime.models.RealtimeRequestSessionModality;
import com.azure.ai.openai.realtime.models.RealtimeServerEventErrorError;
import com.azure.ai.openai.realtime.models.ResponseAudioDeltaEvent;
import com.azure.ai.openai.realtime.models.ResponseAudioDoneEvent;
import com.azure.ai.openai.realtime.models.ResponseAudioTranscriptDeltaEvent;
import com.azure.ai.openai.realtime.models.ResponseAudioTranscriptDoneEvent;
import com.azure.ai.openai.realtime.models.RealtimeServerVadTurnDetection;
import com.azure.ai.openai.realtime.models.RealtimeVoice;
import com.azure.ai.openai.realtime.models.ServerErrorReceivedException;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.Configuration;
import reactor.core.Disposable;
import reactor.core.Disposables;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * This sample showcases sending a prompt in audio and what techniques can be utilized to collect the response both
 * in its audio form and text transcript.
 * {@link #consumeAudioDelta(ResponseAudioDeltaEvent)} will collect the chunks of audio sent by the service.
 * This audio defaults to 16PCM 24 kHz samples. The server provides this data as a base64 encoded string which is returned
 * as a byte array. The audio response generation will be signaled by a {@link ResponseAudioDoneEvent} event.
 * We take advantage of this event to signal the completion of the {@link reactor.core.publisher.Flux} we crafted by using
 * {@link reactor.core.publisher.Flux#takeUntil(java.util.function.Predicate)}. and {@link reactor.core.publisher.Flux#ofType(Class)}.
 * {@link #onAudioResponseCompleted()} is passed as the `onComplete` method to the {@link reactor.core.publisher.Flux#subscribe()} method.
 * In this method, we attached the WAV file headers, since the server omits them, and write the file into {@link #AUDIO_RESPONSE_WAV_FILE}.
 * Similarly, for the text transcript, we consume the {@link ResponseAudioTranscriptDeltaEvent} events and print them
 * without interspersing line breaks. This will render the text as it was emitted by the server. We signal the completion of the
 * transcript generation by listening for a {@link ResponseAudioTranscriptDoneEvent} event and print to console
 * "Audio transcript complete."
 */
public class AudioCollection {

    private static final String AUDIO_RESPONSE_DATA_FILE = "audio_response.data";
    private static final String AUDIO_RESPONSE_WAV_FILE = "audio_response.wav";

    /**
     * This sample showcases sending a prompt in audio and what techniques can be utilized to collect the response both
     * in its audio form and text transcript.
     * {@link #consumeAudioDelta(ResponseAudioDeltaEvent)} will collect the chunks of audio sent by the service.
     * This audio defaults to 16PCM 24 kHz samples. The server provides this data as a base64 encoded string which is returned
     * as a byte array. The audio response generation will be signaled by a {@link ResponseAudioDoneEvent} event.
     * We take advantage of this event to signal the completion of the {@link reactor.core.publisher.Flux} we crafted by using
     * {@link reactor.core.publisher.Flux#takeUntil(java.util.function.Predicate)}. and {@link reactor.core.publisher.Flux#ofType(Class)}.
     * {@link #onAudioResponseCompleted()} is passed as the `onComplete` method to the {@link reactor.core.publisher.Flux#subscribe()} method.
     * In this method, we attached the WAV file headers, since the server omits them, and write the file into {@link #AUDIO_RESPONSE_WAV_FILE}.
     * Similarly, for the text transcript, we consume the {@link ResponseAudioTranscriptDeltaEvent} events and print them
     * without interspersing line breaks. This will render the text as it was emitted by the server. We signal the completion of the
     * transcript generation by listening for a {@link ResponseAudioTranscriptDoneEvent} event and print to console
     * "Audio transcript complete."
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        RealtimeAsyncClient client = buildClient(true);

        // We have a composite disposable to collect our subscriptions
        Disposable.Composite disposables = Disposables.composite();

        // Setup event consumers for our server events of interest:
        //   - ResponseAudioDeltaEvent
        //   - ResponseAudioTranscriptDeltaEvent
        disposables.addAll(Arrays.asList(
            client.getServerEvents()
                .takeUntil(serverEvent -> serverEvent instanceof ResponseAudioDoneEvent)
                .ofType(ResponseAudioDeltaEvent.class)
                .subscribe(AudioCollection::consumeAudioDelta, AudioCollection::consumeError, AudioCollection::onAudioResponseCompleted),
            client.getServerEvents()
                .takeUntil(serverEvent -> serverEvent instanceof ResponseAudioTranscriptDoneEvent)
                .ofType(ResponseAudioTranscriptDeltaEvent.class)
                .subscribe(AudioCollection::consumeAudioTranscriptDelta, AudioCollection::consumeError, AudioCollection::onAudioResponseTranscriptCompleted)
        ));

        // Initializing connection to server
        client.start().block();

        // Configure the realtime session
        client.sendMessage(new SessionUpdateEvent(
            new RealtimeRequestSession()
                .setVoice(RealtimeVoice.ALLOY)
                .setTurnDetection(
                    new RealtimeServerVadTurnDetection()
                            .setThreshold(0.5)
                            .setPrefixPaddingMs(300)
                            .setSilenceDurationMs(200)
            ).setInputAudioTranscription(new RealtimeAudioInputTranscriptionSettings(
                    RealtimeAudioInputTranscriptionModel.WHISPER_1)
            ).setModalities(Arrays.asList(RealtimeRequestSessionModality.AUDIO, RealtimeRequestSessionModality.TEXT))
        )).block();

        // Send audio file with our prompt
        AudioFile audioFile = new AudioFile(FileUtils.openResourceFile("arc-easy-q237-tts-24khz-16PCM.wav"))
                .setBytesPerSample(16)
                .setSampleRate(24000);
        FileUtils.sendAudioFileAsync(client, audioFile).block();

        try {
            // We await for subscriptions that we didn't block on
            Thread.sleep(10000);
            client.stop().block();
            client.close();
            disposables.dispose();

            // File cleanup. Comment out these lines if you want to preserve the audio response files.
            Files.deleteIfExists(FileUtils.openResourceFile(AUDIO_RESPONSE_DATA_FILE));
            Files.deleteIfExists(FileUtils.openResourceFile(AUDIO_RESPONSE_WAV_FILE));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Builds a RealtimeAsyncClient based on the configuration settings defined in the environment variables.
     * @param isAzure is set to `true` will build the client assuming an Azure backend, whereas `false` builds the client
     *                for the OpenAI backend.
     * @return an instance of {@link RealtimeAsyncClient}.
     */
    private static RealtimeAsyncClient buildClient(boolean isAzure) {
        if (isAzure) {
            String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
            String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
            String deploymentOrModelId = Configuration.getGlobalConfiguration().get("MODEL_OR_DEPLOYMENT_NAME");

            return new RealtimeClientBuilder()
                .endpoint(endpoint)
                .deploymentOrModelName(deploymentOrModelId)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildAsyncClient();
        } else {
            String openaiKey = Configuration.getGlobalConfiguration().get("OPENAI_KEY");
            String modelName = Configuration.getGlobalConfiguration().get("OPENAI_MODEL");

            return new RealtimeClientBuilder()
                .credential(new KeyCredential(openaiKey))
                .deploymentOrModelName(modelName)
                .buildAsyncClient();
        }
    }

    /**
     * Consumes the audio delta sent by the server containing a chunk of audio data in base64 encoded format.
     * We append each chunk of audio data to a file {@link #AUDIO_RESPONSE_DATA_FILE}. This file does not contain WAV
     * header information, therefore it will not be recognized as an audio file.
     *
     * @param audioDelta The server sent delta containing a new chunk of audio data.
     */
    private static void consumeAudioDelta(ResponseAudioDeltaEvent audioDelta) {
        try {
            FileUtils.writeToFile(FileUtils.openResourceFile(AUDIO_RESPONSE_DATA_FILE), audioDelta.getDelta());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback triggered when the server signals that a full audio response has been sent. We used the audio data chunks
     * collected in {@link #AUDIO_RESPONSE_DATA_FILE} to write a WAV file using the default parameters for its format, and
     * write the file into {@link #AUDIO_RESPONSE_WAV_FILE}.
     */
    private static void onAudioResponseCompleted() {
        try {
            AudioFormat format = new AudioFormat(24000.0f, 16, 1, true, false);
            byte[] audioData = Files.readAllBytes(FileUtils.openResourceFile(AUDIO_RESPONSE_DATA_FILE));

            File audioResponse = FileUtils.openResourceFile(AUDIO_RESPONSE_WAV_FILE).toFile();
            AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(audioData), format, audioData.length / format.getFrameSize());
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioResponse);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Consumes the audio transcript delta sent by the server containing a chunk of text transcript data. The piece of text
     * is directly printed to console, without the addition of line breaks.
     *
     * @param audioTranscriptDelta The server sent delta containing a new chunk of text transcript data corresponding to
     *                             the audio file.
     */
    private static void consumeAudioTranscriptDelta(ResponseAudioTranscriptDeltaEvent audioTranscriptDelta) {
        System.out.print(audioTranscriptDelta.getDelta());
    }

    /**
     * Callback triggered when the server signals that the audio response transcript has been completed.
     */
    private static void onAudioResponseTranscriptCompleted() {
        System.out.println("\nAudio transcript complete.");
    }

    /**
     * Error handler. We are particularly interested in the {@link ServerErrorReceivedException} which describes errors
     * sent by the server as an event, but mapped into an exception for ease of use.
     *
     * @param error The error that occurred while consuming the server sent events.
     */
    private static void consumeError(Throwable error) {
        if (error instanceof ServerErrorReceivedException) {
            ServerErrorReceivedException serverError = (ServerErrorReceivedException) error;
            RealtimeServerEventErrorError errorDetails = serverError.getErrorDetails();
            System.out.println("Error type: " + errorDetails.getType());
            System.out.println("Error code: " + errorDetails.getCode());
            System.out.println("Error parameter: " + errorDetails.getParam());
            System.out.println("Error message: " + errorDetails.getMessage());
        } else {
            System.out.println(error.getMessage());
        }
    }
}
