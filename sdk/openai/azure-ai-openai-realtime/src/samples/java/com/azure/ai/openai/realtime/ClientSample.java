package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.AudioFile;
import com.azure.ai.openai.realtime.implementation.FileUtils;
import com.azure.ai.openai.realtime.implementation.RealtimeEventHandler;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionModel;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionSettings;
import com.azure.ai.openai.realtime.models.RealtimeClientEventSessionUpdate;
import com.azure.ai.openai.realtime.models.RealtimeRequestSession;
import com.azure.ai.openai.realtime.models.RealtimeRequestSessionModality;
import com.azure.ai.openai.realtime.models.RealtimeServerEvent;
import com.azure.ai.openai.realtime.models.RealtimeServerEventConversationItemInputAudioTranscriptionCompleted;
import com.azure.ai.openai.realtime.models.RealtimeServerEventErrorError;
import com.azure.ai.openai.realtime.models.RealtimeServerEventInputAudioBufferSpeechStarted;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseContentPartAdded;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseDone;
import com.azure.ai.openai.realtime.models.RealtimeServerVadTurnDetection;
import com.azure.ai.openai.realtime.models.ServerErrorReceivedException;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Arrays;

public class ClientSample {
    public static void main(String[] args) {
        String openaiKey = Configuration.getGlobalConfiguration().get("OPENAI_KEY");
        String modelName = Configuration.getGlobalConfiguration().get("OPENAI_MODEL");

        RealtimeAsyncClient client = new RealtimeClientBuilder()
            .credential(new KeyCredential(openaiKey))
            .deploymentOrModelName(modelName)
            .buildAsyncClient();

        Sinks.Many<RealtimeEventHandler.UserInputRequest> requestUserInput = Sinks.many().multicast().onBackpressureBuffer();

        Disposable.Composite disposables = Disposables.composite();

        // Setup event consumer
        disposables.add(client.getServerEvents().subscribe(realtimeServerEvent ->
            consumeServerEvent(realtimeServerEvent, requestUserInput), ClientSample::consumeError));

        client.start().block();

        // Configure the session
        client.sendMessage(new RealtimeClientEventSessionUpdate(
                    new RealtimeRequestSession().setTurnDetection(
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
        AudioFile audioFile = new AudioFile(FileUtils.openResourceFile("arc-easy-q237-tts.wav"))
                .setBytesPerSample(16)
                .setSampleRate(22500);
        FileUtils.sendAudioFileAsync(client, audioFile).block();

        try {
            Thread.sleep(10000);
            client.stop().block();
            client.close();
            disposables.dispose();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void consumeServerEvent(RealtimeServerEvent serverEvent, Sinks.Many<RealtimeEventHandler.UserInputRequest> requestUserInput) {
        if (serverEvent.getType().toString().startsWith("input_audio")) {
            System.out.println("Input audio event of type:" + "\"" + serverEvent.getType() + "\" and its JSON:");
            System.out.println(RealtimeClientTestBase.toJson(serverEvent));
        } else if (serverEvent instanceof RealtimeServerEventResponseDone) {
            RealtimeServerEventResponseDone responseDone = (RealtimeServerEventResponseDone) serverEvent;
            System.out.println("Response done JSON: " + RealtimeClientTestBase.toJson(responseDone));
        } else {
            System.out.println(serverEvent.getType().toString() + " and its JSON:");
            System.out.println(RealtimeClientTestBase.toJson(serverEvent));
        }
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
