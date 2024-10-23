package com.azure.ai.openai.realtime;


import com.azure.ai.openai.realtime.RealtimeAsyncClient;
import com.azure.ai.openai.realtime.RealtimeClientBuilder;
import com.azure.ai.openai.realtime.models.RealtimeAudioFormat;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionModel;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionSettings;
import com.azure.ai.openai.realtime.models.RealtimeClientEventSessionUpdate;
import com.azure.ai.openai.realtime.models.RealtimeRequestSession;
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
import com.azure.ai.openai.realtime.models.RealtimeServerVadTurnDetection;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class LowLevelClient {

    public static void main(String[] args) {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = Configuration.getGlobalConfiguration().get("MODEL_OR_DEPLOYMENT_NAME");

        RealtimeAsyncClient client = new RealtimeClientBuilder()
                .endpoint(endpoint)
                .deploymentOrModelName(deploymentOrModelId)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildAsyncClient();

        // We create our user input requester as a reactor.Sink
        Sinks.Many<UserInputRequest> requestUserInput = Sinks.many().multicast().onBackpressureBuffer();
        Disposable.Composite disposables = Disposables.composite();

        // Add our server message consumer and pass our user input requester
        disposables.add(client.getServerEvents().subscribe(realtimeServerEvent ->
                consumeServerEvent(realtimeServerEvent, requestUserInput)));

        // Start the client
        disposables.add(client.start().subscribe());

        // We setup our user input sender
        disposables.add(requestUserInput.asFlux()
                .flatMap(userInputRequest -> {
                    if (userInputRequest instanceof SessionUpdateRequest) {
                        return client.sendMessage(sessionUpdate());
                    } else if (userInputRequest instanceof SendAudioRequest) {
                        return AudioSender.sendAudio(client, openResourceFile("audio_weather_alaw.wav"));
                    } else if (userInputRequest instanceof EndSession) {
                        return Mono.empty();
                    } else {
                        return Mono.error(new IllegalStateException("Unexpected message type"));
                    }
                }).subscribe());


        // This subscriber will keep our application running until an EndSession request is sent
        requestUserInput.asFlux()
                .ofType(EndSession.class)
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

    private static RealtimeClientEventSessionUpdate sessionUpdate() {
        return new RealtimeClientEventSessionUpdate(
                new RealtimeRequestSession()
                        .setInputAudioTranscription(
                                new RealtimeAudioInputTranscriptionSettings()
                                        .setModel(RealtimeAudioInputTranscriptionModel.WHISPER_1)
                        )
                        .setInputAudioFormat(RealtimeAudioFormat.G711_ALAW)
                        .setTurnDetection(new RealtimeServerVadTurnDetection()));
    }


    private static void consumeServerEvent(RealtimeServerEvent serverEvent, Sinks.Many<UserInputRequest> requestUserInput) {
        switch (serverEvent.getType().getValue()) {
            case "error":
                RealtimeServerEventError errorEvent = (RealtimeServerEventError) serverEvent;
                System.out.println("Error: " + errorEvent.getError().getMessage());
                requestUserInput.emitNext(new EndSession(), Sinks.EmitFailureHandler.FAIL_FAST);
                break;
            case "session.created":
                System.out.println("Session successfully created");
                RealtimeServerEventSessionCreated sessionCreated = (RealtimeServerEventSessionCreated) serverEvent;
                System.out.println("\tEvent ID: " + sessionCreated.getSession().getId());
                System.out.println("\tModel: " + sessionCreated.getSession().getModel());
                requestUserInput.emitNext(new SessionUpdateRequest(), Sinks.EmitFailureHandler.FAIL_FAST);
                break;
            case "session.updated":
                RealtimeServerEventSessionUpdated sessionUpdatedEvent = (RealtimeServerEventSessionUpdated) serverEvent;
                System.out.println("Session updated");
                System.out.println("\tEvent ID: " + sessionUpdatedEvent.getSession().getId());
                System.out.println("\tModel: " + sessionUpdatedEvent.getSession().getModel());
                requestUserInput.emitNext(new SendAudioRequest(), Sinks.EmitFailureHandler.FAIL_FAST);
                break;
            case "conversation.created":
                RealtimeServerEventConversationCreated conversationCreatedEvent = (RealtimeServerEventConversationCreated) serverEvent;

                break;
            case "input_audio_buffer.committed":
                RealtimeServerEventInputAudioBufferCommitted inputAudioBufferCommitted = (RealtimeServerEventInputAudioBufferCommitted) serverEvent;
                // Handle input_audio_buffer.committed event
                break;
            case "input_audio_buffer.cleared":
                RealtimeServerEventInputAudioBufferCleared inputAudioBufferCleared = (RealtimeServerEventInputAudioBufferCleared) serverEvent;
                // Handle input_audio_buffer.cleared event
                break;
            case "input_audio_buffer.speech_started":
                RealtimeServerEventInputAudioBufferSpeechStarted inputAudioBufferSpeechStarted = (RealtimeServerEventInputAudioBufferSpeechStarted) serverEvent;
                System.out.println("Speech started");
                System.out.println("\tEvent ID: " + inputAudioBufferSpeechStarted.getEventId());
                System.out.println("\tStart Time: " + inputAudioBufferSpeechStarted.getAudioStartMs());
                break;
            case "input_audio_buffer.speech_stopped":
                RealtimeServerEventInputAudioBufferSpeechStopped inputAudioBufferSpeechStopped = (RealtimeServerEventInputAudioBufferSpeechStopped) serverEvent;
                // Handle input_audio_buffer.speech_stopped event
                break;
            case "conversation.item.created":
                RealtimeServerEventConversationItemCreated conversationItemCreated = (RealtimeServerEventConversationItemCreated) serverEvent;
                // Handle conversation.item.created event
                break;
            case "conversation.item.input_audio_transcription.completed":
                RealtimeServerEventConversationItemInputAudioTranscriptionCompleted conversationItemInputAudioTranscriptionCompleted = (RealtimeServerEventConversationItemInputAudioTranscriptionCompleted) serverEvent;
                System.out.println("Transcription: " + conversationItemInputAudioTranscriptionCompleted.getTranscript());
                break;
            case "conversation.item.input_audio_transcription.failed":
                RealtimeServerEventConversationItemInputAudioTranscriptionFailed conversationItemInputAudioTranscriptionFailed = (RealtimeServerEventConversationItemInputAudioTranscriptionFailed) serverEvent;
                // Handle conversation.item.input_audio_transcription.failed event
                break;
            case "conversation.item.truncated":
                RealtimeServerEventConversationItemTruncated conversationItemTruncated = (RealtimeServerEventConversationItemTruncated) serverEvent;
                // Handle conversation.item.truncated event
                break;
            case "conversation.item.deleted":
                RealtimeServerEventConversationItemDeleted conversationItemDeleted = (RealtimeServerEventConversationItemDeleted) serverEvent;
                // Handle conversation.item.deleted event
                break;
            case "response.created":
                RealtimeServerEventResponseCreated responseCreated = (RealtimeServerEventResponseCreated) serverEvent;
                // Handle response.created event
                break;
            case "response.done":
                RealtimeServerEventResponseDone responseDone = (RealtimeServerEventResponseDone) serverEvent;
                System.out.println("Response done received");
                requestUserInput.emitNext(new EndSession(), Sinks.EmitFailureHandler.FAIL_FAST);
                // Handle response.done event
                break;
            case "response.output_item.added":
                RealtimeServerEventResponseOutputItemAdded responseOutputItemAdded = (RealtimeServerEventResponseOutputItemAdded) serverEvent;
                // Handle response.output_item.added event
                break;
            case "response.output_item.done":
                RealtimeServerEventResponseOutputItemDone responseOutputItemDone = (RealtimeServerEventResponseOutputItemDone) serverEvent;
                // Handle response.output_item.done event
                break;
            case "response.content_part.added":
                RealtimeServerEventResponseContentPartAdded responseContentPartAdded = (RealtimeServerEventResponseContentPartAdded) serverEvent;
                // Handle response.content_part.added event
                break;
            case "response.content_part.done":
                RealtimeServerEventResponseContentPartDone responseContentPartDone = (RealtimeServerEventResponseContentPartDone) serverEvent;
                // Handle response.content_part.done event
                break;
            case "response.text.delta":
                RealtimeServerEventResponseTextDelta responseTextDelta = (RealtimeServerEventResponseTextDelta) serverEvent;
                // Handle response.text.delta event
                break;
            case "response.text.done":
                RealtimeServerEventResponseTextDone responseTextDone = (RealtimeServerEventResponseTextDone) serverEvent;
                // Handle response.text.done event
                break;
            case "response.audio_transcript.delta":
                RealtimeServerEventResponseAudioTranscriptDelta responseAudioTranscriptDelta = (RealtimeServerEventResponseAudioTranscriptDelta) serverEvent;
                // Handle response.audio_transcript.delta event
                break;
            case "response.audio_transcript.done":
                RealtimeServerEventResponseAudioTranscriptDone responseAudioTranscriptDone = (RealtimeServerEventResponseAudioTranscriptDone) serverEvent;
                // Handle response.audio_transcript.done event
                break;
            case "response.audio.delta":
                RealtimeServerEventResponseAudioDelta responseAudioDelta = (RealtimeServerEventResponseAudioDelta) serverEvent;
                System.out.println("Audio delta received");
                // Handle response.audio.delta event
                break;
            case "response.audio.done":
                RealtimeServerEventResponseAudioDone responseAudioDone = (RealtimeServerEventResponseAudioDone) serverEvent;
                System.out.println("Audio done received");
                // Handle response.audio.done event
                break;
            case "response.function_call_arguments.delta":
                RealtimeServerEventResponseFunctionCallArgumentsDelta responseFunctionCallArgumentsDelta = (RealtimeServerEventResponseFunctionCallArgumentsDelta) serverEvent;
                // Handle response.function_call_arguments.delta event
                break;
            case "response.function_call_arguments.done":
                RealtimeServerEventResponseFunctionCallArgumentsDone responseFunctionCallArgumentsDone = (RealtimeServerEventResponseFunctionCallArgumentsDone) serverEvent;
                // Handle response.function_call_arguments.done event
                break;
            case "rate_limits.updated":
                RealtimeServerEventRateLimitsUpdated rateLimitsUpdated = (RealtimeServerEventRateLimitsUpdated) serverEvent;
                // Handle rate_limits.updated event
                break;
            default:
                break;
        }
    }

    private static class UserInputRequest{}
    private static final class SessionUpdateRequest extends UserInputRequest{}
    private static final class SendAudioRequest extends UserInputRequest{}
    private static final class EndSession extends UserInputRequest{}

    private static Path openResourceFile(String fileName) {
        return Paths.get("src", "test", "resources", fileName);
    }
}
