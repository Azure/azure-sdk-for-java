package com.azure.ai.openai.realtime;


import com.azure.ai.openai.realtime.models.RealtimeServerEvent;
import com.azure.ai.openai.realtime.models.RealtimeServerEventConversationCreated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventConversationItemCreated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventConversationItemDeleted;
import com.azure.ai.openai.realtime.models.RealtimeServerEventConversationItemInputAudioTranscriptionCompleted;
import com.azure.ai.openai.realtime.models.RealtimeServerEventConversationItemInputAudioTranscriptionFailed;
import com.azure.ai.openai.realtime.models.RealtimeServerEventConversationItemTruncated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventError;
import com.azure.ai.openai.realtime.models.RealtimeServerEventInputAudioBufferCleared;
import com.azure.ai.openai.realtime.models.RealtimeServerEventInputAudioBufferCommitted;
import com.azure.ai.openai.realtime.models.RealtimeServerEventInputAudioBufferSpeechStarted;
import com.azure.ai.openai.realtime.models.RealtimeServerEventInputAudioBufferSpeechStopped;
import com.azure.ai.openai.realtime.models.RealtimeServerEventRateLimitsUpdated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseAudioDelta;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseAudioDone;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseAudioTranscriptDelta;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseAudioTranscriptDone;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseContentPartAdded;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseContentPartDone;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseCreated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseDone;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseFunctionCallArgumentsDelta;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseFunctionCallArgumentsDone;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseOutputItemAdded;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseOutputItemDone;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseTextDelta;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseTextDone;
import com.azure.ai.openai.realtime.models.RealtimeServerEventSessionCreated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventSessionUpdated;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public class LowLevelClient {

    public static void main(String[] args) {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = Configuration.getGlobalConfiguration().get("MODEL_OR_DEPLOYMENT_NAME");

        String openAIKey = Configuration.getGlobalConfiguration().get("OPENAI_KEY");
        String openAIModel = Configuration.getGlobalConfiguration().get("OPENAI_MODEL");

        RealtimeAsyncClient client = new RealtimeClientBuilder()
                // Azure
                .endpoint(endpoint)
                .deploymentOrModelName(deploymentOrModelId)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                // non-Azure
//                .credential(new KeyCredential(openAIKey))
//                .deploymentOrModelName(openAIModel)
                .buildAsyncClient();

        // We create our user input requester as a reactor.Sink
        Sinks.Many<RealtimeEventHandler.UserInputRequest> requestUserInput = Sinks.many().multicast().onBackpressureBuffer();
        Disposable.Composite disposables = Disposables.composite();

        // Add our server message consumer and pass our user input requester
        disposables.add(client.getServerEvents().subscribe(realtimeServerEvent ->
                RealtimeEventHandler.consumeServerEvent(realtimeServerEvent, requestUserInput)));

        // Start the client
        disposables.add(client.start().subscribe());

        // We setup our user input sender
        disposables.add(requestUserInput.asFlux()
                .flatMap(userInputRequest -> {
                    if (userInputRequest instanceof RealtimeEventHandler.SessionUpdateRequest) {
                        return client.sendMessage(RealtimeEventHandler.sessionUpdate());
                    } else if (userInputRequest instanceof RealtimeEventHandler.SendAudioRequest) {
                        return FileUtils.sendAudioFile(client, FileUtils.openResourceFile("audio_weather_alaw.wav"));
                    } else if (userInputRequest instanceof RealtimeEventHandler.EndSession) {
                        return Mono.empty();
                    } else {
                        return Mono.error(new IllegalStateException("Unexpected message type"));
                    }
                }).subscribe());


        // This subscriber will keep our application running until an EndSession request is sent
        requestUserInput.asFlux()
                .ofType(RealtimeEventHandler.EndSession.class)
//                .timeout(Duration.ofSeconds(5))
                .blockFirst();

        try {
            client.stop().block();
            client.close();
            disposables.dispose();
        } catch (Exception e) {
            System.out.println("Error closing client: " + e.getMessage());
        }
    }
}
