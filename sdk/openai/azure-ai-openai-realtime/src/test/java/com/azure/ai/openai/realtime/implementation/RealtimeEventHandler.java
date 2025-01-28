// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.implementation;

import com.azure.ai.openai.realtime.models.RateLimitsUpdatedEvent;
import com.azure.ai.openai.realtime.models.RealtimeAudioFormat;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionModel;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionSettings;
import com.azure.ai.openai.realtime.models.RealtimeRequestSession;
import com.azure.ai.openai.realtime.models.RealtimeRequestSessionModality;
import com.azure.ai.openai.realtime.models.RealtimeServerEvent;
import com.azure.ai.openai.realtime.models.ConversationCreatedEvent;
import com.azure.ai.openai.realtime.models.ConversationItemCreatedEvent;
import com.azure.ai.openai.realtime.models.ConversationItemDeletedEvent;
import com.azure.ai.openai.realtime.models.ConversationItemInputAudioTranscriptionCompletedEvent;
import com.azure.ai.openai.realtime.models.ConversationItemInputAudioTranscriptionFailedEvent;
import com.azure.ai.openai.realtime.models.ConversationItemTruncatedEvent;
import com.azure.ai.openai.realtime.models.RealtimeServerEventError;
import com.azure.ai.openai.realtime.models.InputAudioBufferClearedEvent;
import com.azure.ai.openai.realtime.models.InputAudioBufferCommittedEvent;
import com.azure.ai.openai.realtime.models.InputAudioBufferSpeechStartedEvent;
import com.azure.ai.openai.realtime.models.InputAudioBufferSpeechStoppedEvent;
import com.azure.ai.openai.realtime.models.ResponseAudioDeltaEvent;
import com.azure.ai.openai.realtime.models.ResponseAudioDoneEvent;
import com.azure.ai.openai.realtime.models.ResponseAudioTranscriptDeltaEvent;
import com.azure.ai.openai.realtime.models.ResponseAudioTranscriptDoneEvent;
import com.azure.ai.openai.realtime.models.ResponseContentPartAddedEvent;
import com.azure.ai.openai.realtime.models.ResponseTextDeltaEvent;
import com.azure.ai.openai.realtime.models.ResponseCreatedEvent;
import com.azure.ai.openai.realtime.models.ResponseDoneEvent;
import com.azure.ai.openai.realtime.models.ResponseFunctionCallArgumentsDeltaEvent;
import com.azure.ai.openai.realtime.models.ResponseFunctionCallArgumentsDoneEvent;
import com.azure.ai.openai.realtime.models.ResponseOutputItemAddedEvent;
import com.azure.ai.openai.realtime.models.ResponseOutputItemDoneEvent;
import com.azure.ai.openai.realtime.models.ResponseTextDoneEvent;
import com.azure.ai.openai.realtime.models.SessionCreatedEvent;
import com.azure.ai.openai.realtime.models.SessionUpdatedEvent;
import com.azure.ai.openai.realtime.models.RealtimeServerVadTurnDetection;
import com.azure.ai.openai.realtime.models.SessionUpdateEvent;
import reactor.core.publisher.Sinks;

import java.util.Arrays;

/**
 * Utility class to handle Realtime server events. {@link #consumeServerEvent(RealtimeServerEvent, Sinks.Many)} consumes
 * all the available server events available at the time of writing this.
 * The {@link Sinks.Many} is used to single to the consumer, to issue a new client input. This is used in samples, so it's
 * catering specifically to the use case, but it showcases a potential way to signal the need for more user input in
 * asynchronous scenarios.
 */
public final class RealtimeEventHandler {

    public static class UserInputRequest {
    }

    /**
     * Sent when the service is ready to receive a session update request.
     */
    public static final class SessionUpdateRequest extends UserInputRequest {
    }

    /**
     * Sent when the service is ready to receive audio input.
     */
    public static final class SendAudioRequest extends UserInputRequest {
    }

    /**
     * Sent when the service is done responding to a request, signaling the user to close the session.
     */
    public static final class EndSession extends UserInputRequest {
    }

    /**
     * Consumes the server event and emits a new user input request if necessary.
     *
     * @param serverEvent The server event to consume.
     * @param requestUserInput The sink to emit a new user input request.
     */
    public static void consumeServerEvent(RealtimeServerEvent serverEvent,
        Sinks.Many<RealtimeEventHandler.UserInputRequest> requestUserInput) {
        System.out.println("Server event received: " + serverEvent.getType().getValue());
        switch (serverEvent.getType().getValue()) {
            case "error":
                RealtimeServerEventError errorEvent = (RealtimeServerEventError) serverEvent;
                System.out.println("Error: " + errorEvent.getError().getMessage());
                requestUserInput.emitNext(new EndSession(), Sinks.EmitFailureHandler.FAIL_FAST);
                break;

            case "session.created":
                System.out.println("Session successfully created");
                SessionCreatedEvent sessionCreated = (SessionCreatedEvent) serverEvent;
                System.out.println("\tEvent ID: " + sessionCreated.getSession().getId());
                System.out.println("\tModel: " + sessionCreated.getSession().getModel());
                requestUserInput.emitNext(new SessionUpdateRequest(), Sinks.EmitFailureHandler.FAIL_FAST);
                break;

            case "session.updated":
                SessionUpdatedEvent sessionUpdatedEvent = (SessionUpdatedEvent) serverEvent;
                System.out.println("Session updated");
                System.out.println("\tEvent ID: " + sessionUpdatedEvent.getSession().getId());
                System.out.println("\tModel: " + sessionUpdatedEvent.getSession().getModel());
                requestUserInput.emitNext(new SendAudioRequest(), Sinks.EmitFailureHandler.FAIL_FAST);
                break;

            case "conversation.created":
                ConversationCreatedEvent conversationCreatedEvent = (ConversationCreatedEvent) serverEvent;

                break;

            case "input_audio_buffer.committed":
                InputAudioBufferCommittedEvent inputAudioBufferCommitted = (InputAudioBufferCommittedEvent) serverEvent;
                // Handle input_audio_buffer.committed event
                break;

            case "input_audio_buffer.cleared":
                InputAudioBufferClearedEvent inputAudioBufferCleared = (InputAudioBufferClearedEvent) serverEvent;
                // Handle input_audio_buffer.cleared event
                break;

            case "input_audio_buffer.speech_started":
                InputAudioBufferSpeechStartedEvent inputAudioBufferSpeechStarted
                    = (InputAudioBufferSpeechStartedEvent) serverEvent;
                System.out.println("Speech started");
                System.out.println("\tEvent ID: " + inputAudioBufferSpeechStarted.getEventId());
                System.out.println("\tStart Time: " + inputAudioBufferSpeechStarted.getAudioStartMs());
                break;

            case "input_audio_buffer.speech_stopped":
                InputAudioBufferSpeechStoppedEvent inputAudioBufferSpeechStopped
                    = (InputAudioBufferSpeechStoppedEvent) serverEvent;
                // Handle input_audio_buffer.speech_stopped event
                break;

            case "conversation.item.created":
                ConversationItemCreatedEvent conversationItemCreated = (ConversationItemCreatedEvent) serverEvent;
                // Handle conversation.item.created event
                break;

            case "conversation.item.input_audio_transcription.completed":
                ConversationItemInputAudioTranscriptionCompletedEvent conversationItemInputAudioTranscriptionCompleted
                    = (ConversationItemInputAudioTranscriptionCompletedEvent) serverEvent;
                System.out
                    .println("Transcription: " + conversationItemInputAudioTranscriptionCompleted.getTranscript());
                break;

            case "conversation.item.input_audio_transcription.failed":
                ConversationItemInputAudioTranscriptionFailedEvent conversationItemInputAudioTranscriptionFailed
                    = (ConversationItemInputAudioTranscriptionFailedEvent) serverEvent;
                // Handle conversation.item.input_audio_transcription.failed event
                break;

            case "conversation.item.truncated":
                ConversationItemTruncatedEvent conversationItemTruncated = (ConversationItemTruncatedEvent) serverEvent;
                // Handle conversation.item.truncated event
                break;

            case "conversation.item.deleted":
                ConversationItemDeletedEvent conversationItemDeleted = (ConversationItemDeletedEvent) serverEvent;
                // Handle conversation.item.deleted event
                break;

            case "response.created":
                ResponseCreatedEvent responseCreated = (ResponseCreatedEvent) serverEvent;
                // Handle response.created event
                break;

            case "response.done":
                ResponseDoneEvent responseDone = (ResponseDoneEvent) serverEvent;
                System.out.println("Response done received");
                requestUserInput.emitNext(new EndSession(), Sinks.EmitFailureHandler.FAIL_FAST);
                // Handle response.done event
                break;

            case "response.output_item.added":
                ResponseOutputItemAddedEvent responseOutputItemAdded = (ResponseOutputItemAddedEvent) serverEvent;
                // Handle response.output_item.added event
                break;

            case "response.output_item.done":
                ResponseOutputItemDoneEvent responseOutputItemDone = (ResponseOutputItemDoneEvent) serverEvent;
                // Handle response.output_item.done event
                break;

            case "response.content_part.added":
                ResponseContentPartAddedEvent responseContentPartAdded = (ResponseContentPartAddedEvent) serverEvent;
                // Handle response.content_part.added event
                break;

            case "response.content_part.done":
                ResponseTextDeltaEvent responseContentPartDone = (ResponseTextDeltaEvent) serverEvent;
                // Handle response.content_part.done event
                break;

            case "response.text.delta":
                ResponseTextDeltaEvent responseTextDelta = (ResponseTextDeltaEvent) serverEvent;
                // Handle response.text.delta event
                break;

            case "response.text.done":
                ResponseTextDoneEvent responseTextDone = (ResponseTextDoneEvent) serverEvent;
                // Handle response.text.done event
                break;

            case "response.audio_transcript.delta":
                ResponseAudioTranscriptDeltaEvent responseAudioTranscriptDelta
                    = (ResponseAudioTranscriptDeltaEvent) serverEvent;
                // Handle response.audio_transcript.delta event
                break;

            case "response.audio_transcript.done":
                ResponseAudioTranscriptDoneEvent responseAudioTranscriptDone
                    = (ResponseAudioTranscriptDoneEvent) serverEvent;
                // Handle response.audio_transcript.done event
                break;

            case "response.audio.delta":
                ResponseAudioDeltaEvent responseAudioDelta = (ResponseAudioDeltaEvent) serverEvent;
                System.out.println("Audio delta received");
                // Handle response.audio.delta event
                break;

            case "response.audio.done":
                ResponseAudioDoneEvent responseAudioDone = (ResponseAudioDoneEvent) serverEvent;
                System.out.println("Audio done received");
                // Handle response.audio.done event
                break;

            case "response.function_call_arguments.delta":
                ResponseFunctionCallArgumentsDeltaEvent responseFunctionCallArgumentsDelta
                    = (ResponseFunctionCallArgumentsDeltaEvent) serverEvent;
                // Handle response.function_call_arguments.delta event
                break;

            case "response.function_call_arguments.done":
                ResponseFunctionCallArgumentsDoneEvent responseFunctionCallArgumentsDone
                    = (ResponseFunctionCallArgumentsDoneEvent) serverEvent;
                // Handle response.function_call_arguments.done event
                break;

            case "rate_limits.updated":
                RateLimitsUpdatedEvent rateLimitsUpdated = (RateLimitsUpdatedEvent) serverEvent;
                // Handle rate_limits.updated event
                break;

            default:
                break;
        }
    }

    /**
     * Creates a session update event for the purpose of tests
     * @return a session update request event from the client.
     */
    public static SessionUpdateEvent sessionUpdate() {
        return new SessionUpdateEvent(new RealtimeRequestSession()
            .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT, RealtimeRequestSessionModality.AUDIO))
            .setInputAudioTranscription(
                new RealtimeAudioInputTranscriptionSettings().setModel(RealtimeAudioInputTranscriptionModel.WHISPER_1))
            .setInputAudioFormat(RealtimeAudioFormat.G711_ALAW)
            .setTurnDetection(new RealtimeServerVadTurnDetection()));
    }
}
