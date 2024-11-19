package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.RealtimeEventHandler;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionModel;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionSettings;
import com.azure.ai.openai.realtime.models.RealtimeClientEventSessionUpdate;
import com.azure.ai.openai.realtime.models.RealtimeRequestSession;
import com.azure.ai.openai.realtime.models.RealtimeServerEvent;
import com.azure.ai.openai.realtime.models.RealtimeServerEventErrorError;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseContentPartAdded;
import com.azure.ai.openai.realtime.models.RealtimeServerVadTurnDetection;
import com.azure.ai.openai.realtime.models.ServerErrorReceivedException;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.Configuration;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Sinks;

import java.time.Duration;

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

        // Configure the session
        client.start().block();
        client.sendMessage(new RealtimeClientEventSessionUpdate(
                new RealtimeRequestSession().setTurnDetection(
                        new RealtimeServerVadTurnDetection()
                                .setThreshold(0.5)
                                .setPrefixPaddingMs(300)
                                .setSilenceDurationMs(200)
                ).setInputAudioTranscription(new RealtimeAudioInputTranscriptionSettings(
                        RealtimeAudioInputTranscriptionModel.WHISPER_1))
            )
        ).block();

        try {
            client.stop().block();
            client.close();
            disposables.dispose();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void consumeServerEvent(RealtimeServerEvent serverEvent, Sinks.Many<RealtimeEventHandler.UserInputRequest> requestUserInput) {
        if (serverEvent instanceof RealtimeServerEventResponseContentPartAdded) {
            RealtimeServerEventResponseContentPartAdded contentPartAdded = (RealtimeServerEventResponseContentPartAdded) serverEvent;
            contentPartAdded.getPart();
        }
        System.out.println(serverEvent.getType().toString());
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
