package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.AudioFile;
import com.azure.ai.openai.realtime.implementation.FileUtils;
import com.azure.ai.openai.realtime.implementation.RealtimeEventHandler;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionModel;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionSettings;
import com.azure.ai.openai.realtime.models.RealtimeClientEventSessionUpdate;
import com.azure.ai.openai.realtime.models.RealtimeRequestSession;
import com.azure.ai.openai.realtime.models.RealtimeRequestSessionModality;
import com.azure.ai.openai.realtime.models.RealtimeServerEventErrorError;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseAudioDelta;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseAudioDone;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseAudioTranscriptDelta;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseAudioTranscriptDone;
import com.azure.ai.openai.realtime.models.RealtimeServerVadTurnDetection;
import com.azure.ai.openai.realtime.models.RealtimeVoice;
import com.azure.ai.openai.realtime.models.ServerErrorReceivedException;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.Configuration;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Sinks;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class ClientSample {

    private static final String AUDIO_RESPONSE_DATA_FILE = "audio_response.data";
    private static final String AUDIO_RESPONSE_WAV_FILE = "audio_response.wav";

    public static void main(String[] args) {
        String openaiKey = Configuration.getGlobalConfiguration().get("OPENAI_KEY");
        String modelName = Configuration.getGlobalConfiguration().get("OPENAI_MODEL");

        RealtimeAsyncClient client = new RealtimeClientBuilder()
            .credential(new KeyCredential(openaiKey))
            .deploymentOrModelName(modelName)
            .buildAsyncClient();


//        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
//        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
//        String deploymentOrModelId = Configuration.getGlobalConfiguration().get("MODEL_OR_DEPLOYMENT_NAME");
//
//        RealtimeAsyncClient client = new RealtimeClientBuilder()
//                .endpoint(endpoint)
//                .deploymentOrModelName(deploymentOrModelId)
//                .credential(new AzureKeyCredential(azureOpenaiKey))
//                .buildAsyncClient();

        Sinks.Many<RealtimeEventHandler.UserInputRequest> requestUserInput = Sinks.many().multicast().onBackpressureBuffer();

        Disposable.Composite disposables = Disposables.composite();

        // Setup event consumer for our classes of interest:
        //   - RealtimeServerEventResponseAudioDelta
        //   - RealtimeServerEventResponseAudioTranscriptDelta

        disposables.addAll(Arrays.asList(
            client.getServerEvents()
                .takeUntil(serverEvent -> serverEvent instanceof RealtimeServerEventResponseAudioDone)
                .ofType(RealtimeServerEventResponseAudioDelta.class)
                .subscribe(ClientSample::consumeAudioDelta, ClientSample::consumeError, ClientSample::onAudioResponseCompleted),
            client.getServerEvents()
                .takeUntil(serverEvent -> serverEvent instanceof RealtimeServerEventResponseAudioTranscriptDone)
                .ofType(RealtimeServerEventResponseAudioTranscriptDelta.class)
                .subscribe(ClientSample::consumeAudioTranscriptDelta, ClientSample::consumeError, ClientSample::onAudioTranscriptDeltaCompleted)
        ));

        client.start().block();

        // Configure the session
        client.sendMessage(new RealtimeClientEventSessionUpdate(
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
        )
        ).block();

        // Send audio file
        AudioFile audioFile = new AudioFile(FileUtils.openResourceFile("arc-easy-q237-tts-24khz-16PCM.wav"))
                .setBytesPerSample(16)
                .setSampleRate(24000);
        FileUtils.sendAudioFileAsync(client, audioFile).block();

        try {
            Thread.sleep(10000);
            client.stop().block();
            client.close();
            disposables.dispose();

            Files.deleteIfExists(FileUtils.openResourceFile(AUDIO_RESPONSE_DATA_FILE));
//            Files.deleteIfExists(FileUtils.openResourceFile(AUDIO_RESPONSE_WAV_FILE));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void consumeAudioDelta(RealtimeServerEventResponseAudioDelta audioDelta) {
        try {
            FileUtils.writeToFile(FileUtils.openResourceFile(AUDIO_RESPONSE_DATA_FILE), audioDelta.getDelta());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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

    private static void consumeAudioTranscriptDelta(RealtimeServerEventResponseAudioTranscriptDelta audioTranscriptDelta) {
        System.out.print(audioTranscriptDelta.getDelta());
    }

    private static void onAudioTranscriptDeltaCompleted() {
        System.out.println("\nAudio transcript complete.");
    }

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
