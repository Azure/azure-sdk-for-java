// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.EventHandlerCollection;
import com.azure.ai.openai.realtime.models.RealtimeClientEvent;
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
import com.azure.ai.openai.realtime.models.RealtimeServerEventType;
import com.azure.core.annotation.ServiceClient;
import reactor.core.scheduler.Schedulers;

import java.io.Closeable;
import java.util.function.Consumer;

/**
 * Initializes a new instance of the synchronous OpenAIClient type.
 */
@ServiceClient(builder = RealtimeClientBuilder.class)
public final class RealtimeClient implements Closeable {

    private final RealtimeAsyncClient asyncClient;
    private final EventHandlerCollection eventHandlerCollection = new EventHandlerCollection();

    RealtimeClient(RealtimeAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Starts the client for connecting to the server.
     */
    public synchronized void start() {
        asyncClient.start(() -> {
            this.asyncClient.getServerEvents().publishOn(Schedulers.boundedElastic())
                    .subscribe(event -> eventHandlerCollection.fireEvent(event.getType().toString(), event));
        }).block();
    }

    /**
     * Stops the client and disconnects from the server.
     */
    public synchronized void stop() {
        asyncClient.stop().block();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        this.stop();
    }

    /**
     * Sends a message to the server.
     * 
     * @param event The event to send.
     */
    public void sendMessage(RealtimeClientEvent event) {
        asyncClient.sendMessage(event).block();
    }

    /**
     * Adds an event handler for the conversation created event.
     *
     * @param onConversationCreatedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnConversationCreatedEventHandler(Consumer<RealtimeServerEventConversationCreated> onConversationCreatedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.CONVERSATION_CREATED.toString(), onConversationCreatedEventHandler);
    }

    /**
     * Adds an event handler for the conversation item created event.
     *
     * @param onConversationItemCreatedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnConversationItemCreatedEventHandler(Consumer<RealtimeServerEventConversationItemCreated> onConversationItemCreatedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.CONVERSATION_ITEM_CREATED.toString(), onConversationItemCreatedEventHandler);
    }

    /**
     * Adds an event handler for the conversation item deleted event.
     *
     * @param onConversationItemDeletedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnConversationItemDeletedEventHandler(Consumer<RealtimeServerEventConversationItemDeleted> onConversationItemDeletedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.CONVERSATION_ITEM_DELETED.toString(), onConversationItemDeletedEventHandler);
    }

    /**
     * Adds an event handler for the conversation item input audio transcription completed event.
     *
     * @param onConversationItemInputAudioTranscriptionCompletedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnConversationItemInputAudioTranscriptionCompletedEventHandler(Consumer<RealtimeServerEventConversationItemInputAudioTranscriptionCompleted> onConversationItemInputAudioTranscriptionCompletedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.CONVERSATION_ITEM_INPUT_AUDIO_TRANSCRIPTION_COMPLETED.toString(), onConversationItemInputAudioTranscriptionCompletedEventHandler);
    }

    /**
     * Adds an event handler for the conversation item input audio transcription failed event.
     *
     * @param onConversationItemInputAudioTranscriptionFailedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnConversationItemInputAudioTranscriptionFailedEventHandler(Consumer<RealtimeServerEventConversationItemInputAudioTranscriptionFailed> onConversationItemInputAudioTranscriptionFailedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.CONVERSATION_ITEM_INPUT_AUDIO_TRANSCRIPTION_FAILED.toString(), onConversationItemInputAudioTranscriptionFailedEventHandler);
    }

    /**
     * Adds an event handler for the conversation item truncated event.
     *
     * @param onConversationItemTruncatedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnConversationItemTruncatedEventHandler(Consumer<RealtimeServerEventConversationItemTruncated> onConversationItemTruncatedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.CONVERSATION_ITEM_TRUNCATED.toString(), onConversationItemTruncatedEventHandler);
    }

    /**
     * Adds an event handler for the error event.
     *
     * @param onErrorEventHandler The callback to be notified when this event type is received.
     */
    public void addOnErrorEventHandler(Consumer<RealtimeServerEventError> onErrorEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.ERROR.toString(), onErrorEventHandler);
    }

    /**
     * Adds an event handler for the input audio buffer cleared event.
     *
     * @param onInputAudioBufferClearedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnInputAudioBufferClearedEventHandler(Consumer<RealtimeServerEventInputAudioBufferCleared> onInputAudioBufferClearedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.INPUT_AUDIO_BUFFER_CLEARED.toString(), onInputAudioBufferClearedEventHandler);
    }

    /**
     * Adds an event handler for the input audio buffer committed event.
     *
     * @param onInputAudioBufferCommittedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnInputAudioBufferCommittedEventHandler(Consumer<RealtimeServerEventInputAudioBufferCommitted> onInputAudioBufferCommittedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.INPUT_AUDIO_BUFFER_COMMITTED.toString(), onInputAudioBufferCommittedEventHandler);
    }

    /**
     * Adds an event handler for the input audio buffer speech started event.
     *
     * @param onInputAudioBufferSpeechStartedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnInputAudioBufferSpeechStartedEventHandler(Consumer<RealtimeServerEventInputAudioBufferSpeechStarted> onInputAudioBufferSpeechStartedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED.toString(), onInputAudioBufferSpeechStartedEventHandler);
    }

    /**
     * Adds an event handler for the input audio buffer speech stopped event.
     *
     * @param onInputAudioBufferSpeechStoppedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnInputAudioBufferSpeechStoppedEventHandler(Consumer<RealtimeServerEventInputAudioBufferSpeechStopped> onInputAudioBufferSpeechStoppedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STOPPED.toString(), onInputAudioBufferSpeechStoppedEventHandler);
    }

    /**
     * Adds an event handler for the rate limits updated event.
     *
     * @param onRateLimitsUpdatedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnRateLimitsUpdatedEventHandler(Consumer<RealtimeServerEventRateLimitsUpdated> onRateLimitsUpdatedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RATE_LIMITS_UPDATED.toString(), onRateLimitsUpdatedEventHandler);
    }

    /**
     * Adds an event handler for the response audio delta event.
     *
     * @param onResponseAudioDeltaEventHandler The callback to be notified when this event type is received.
     */
    public void addOnResponseAudioDeltaEventHandler(Consumer<RealtimeServerEventResponseAudioDelta> onResponseAudioDeltaEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RESPONSE_AUDIO_DELTA.toString(), onResponseAudioDeltaEventHandler);
    }

    /**
     * Adds an event handler for the response audio done event.
     *
     * @param onResponseAudioDoneEventHandler The callback to be notified when this event type is received.
     */
    public void addOnResponseAudioDoneEventHandler(Consumer<RealtimeServerEventResponseAudioDone> onResponseAudioDoneEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RESPONSE_AUDIO_DONE.toString(), onResponseAudioDoneEventHandler);
    }

    /**
     * Adds an event handler for the response audio transcript delta event.
     *
     * @param onResponseAudioTranscriptDeltaEventHandler The callback to be notified when this event type is received.
     */
    public void addOnResponseAudioTranscriptDeltaEventHandler(Consumer<RealtimeServerEventResponseAudioTranscriptDelta> onResponseAudioTranscriptDeltaEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RESPONSE_AUDIO_TRANSCRIPT_DELTA.toString(), onResponseAudioTranscriptDeltaEventHandler);
    }

    /**
     * Adds an event handler for the response audio transcript done event.
     *
     * @param onResponseAudioTranscriptDoneEventHandler The callback to be notified when this event type is received.
     */
    public void addOnResponseAudioTranscriptDoneEventHandler(Consumer<RealtimeServerEventResponseAudioTranscriptDone> onResponseAudioTranscriptDoneEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RESPONSE_AUDIO_TRANSCRIPT_DONE.toString(), onResponseAudioTranscriptDoneEventHandler);
    }

    /**
     * Adds an event handler for the response content part added event.
     *
     * @param onResponseContentPartAddedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnResponseContentPartAddedEventHandler(Consumer<RealtimeServerEventResponseContentPartAdded> onResponseContentPartAddedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RESPONSE_CONTENT_PART_ADDED.toString(), onResponseContentPartAddedEventHandler);
    }

    /**
     * Adds an event handler for the response content part done event.
     *
     * @param onResponseContentPartDoneEventHandler The callback to be notified when this event type is received.
     */
    public void addOnResponseContentPartDoneEventHandler(Consumer<RealtimeServerEventResponseContentPartDone> onResponseContentPartDoneEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RESPONSE_CONTENT_PART_DONE.toString(), onResponseContentPartDoneEventHandler);
    }

    /**
     * Adds an event handler for the response created event.
     *
     * @param onResponseCreatedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnResponseCreatedEventHandler(Consumer<RealtimeServerEventResponseCreated> onResponseCreatedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RESPONSE_CREATED.toString(), onResponseCreatedEventHandler);
    }

    /**
     * Adds an event handler for the response done event.
     *
     * @param onResponseDoneEventHandler The callback to be notified when this event type is received.
     */
    public void addOnResponseDoneEventHandler(Consumer<RealtimeServerEventResponseDone> onResponseDoneEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RESPONSE_DONE.toString(), onResponseDoneEventHandler);
    }

    /**
     * Adds an event handler for the response function call arguments delta event.
     *
     * @param onResponseFunctionCallArgumentsDeltaEventHandler The callback to be notified when this event type is received.
     */
    public void addOnResponseFunctionCallArgumentsDeltaEventHandler(Consumer<RealtimeServerEventResponseFunctionCallArgumentsDelta> onResponseFunctionCallArgumentsDeltaEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RESPONSE_FUNCTION_CALL_ARGUMENTS_DELTA.toString(), onResponseFunctionCallArgumentsDeltaEventHandler);
    }

    /**
     * Adds an event handler for the response function call arguments done event.
     *
     * @param onResponseFunctionCallArgumentsDoneEventHandler The callback to be notified when this event type is received.
     */
    public void addOnResponseFunctionCallArgumentsDoneEventHandler(Consumer<RealtimeServerEventResponseFunctionCallArgumentsDone> onResponseFunctionCallArgumentsDoneEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RESPONSE_FUNCTION_CALL_ARGUMENTS_DONE.toString(), onResponseFunctionCallArgumentsDoneEventHandler);
    }

    /**
     * Adds an event handler for the response output item added event.
     *
     * @param onResponseOutputItemAddedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnResponseOutputItemAddedEventHandler(Consumer<RealtimeServerEventResponseOutputItemAdded> onResponseOutputItemAddedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RESPONSE_OUTPUT_ITEM_ADDED.toString(), onResponseOutputItemAddedEventHandler);
    }

    /**
     * Adds an event handler for the response output item done event.
     *
     * @param onResponseOutputItemDoneEventHandler The callback to be notified when this event type is received.
     */
    public void addOnResponseOutputItemDoneEventHandler(Consumer<RealtimeServerEventResponseOutputItemDone> onResponseOutputItemDoneEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RESPONSE_OUTPUT_ITEM_DONE.toString(), onResponseOutputItemDoneEventHandler);
    }

    /**
     * Adds an event handler for the response text delta event.
     *
     * @param onResponseTextDeltaEventHandler The callback to be notified when this event type is received.
     */
    public void addOnResponseTextDeltaEventHandler(Consumer<RealtimeServerEventResponseTextDelta> onResponseTextDeltaEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RESPONSE_TEXT_DELTA.toString(), onResponseTextDeltaEventHandler);
    }

    /**
     * Adds an event handler for the response text done event.
     *
     * @param onResponseTextDoneEventHandler The callback to be notified when this event type is received.
     */
    public void addOnResponseTextDoneEventHandler(Consumer<RealtimeServerEventResponseTextDone> onResponseTextDoneEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.RESPONSE_TEXT_DONE.toString(), onResponseTextDoneEventHandler);
    }

    /**
     * Adds an event handler for the session created event.
     * 
     * @param onSessionCreatedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnSessionCreatedEventHandler (Consumer<RealtimeServerEventSessionCreated> onSessionCreatedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.SESSION_CREATED.toString(), onSessionCreatedEventHandler);
    }

    /** 
     * Adds an event handler for the session updated event.
     * 
     * @param onSessionUpdatedEventHandler The callback to be notified when this event type is received.
     */
    public void addOnSessionUpdatedEventHandler (Consumer<RealtimeServerEventSessionUpdated> onSessionUpdatedEventHandler) {
        eventHandlerCollection.addEventHandler(RealtimeServerEventType.SESSION_UPDATED.toString(), onSessionUpdatedEventHandler);
    }
}
