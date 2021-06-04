// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallRecordingState;
import com.azure.communication.callingserver.models.CallRecordingStateResponse;
import com.azure.communication.callingserver.models.PlayAudioResponse;
import com.azure.communication.callingserver.models.StartCallRecordingResponse;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */

public class ConversationClientReadmeSamples {

    /**
     * Sample code for creating a sync convesation client.
     *
     * @return the call client.
     */
    public ConversationClient createConversationClient() {
        String endpoint = "https://<RESOURCE_NAME>.communcationservices.azure.com";

        // Your connectionString retrieved from your Azure Communication Service
        String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

        // Initialize the call client
        final ConversationClientBuilder builder = new ConversationClientBuilder();
        builder.endpoint(endpoint)
            .connectionString(connectionString);
        ConversationClient conversationClient = builder.buildClient();

        return conversationClient;
    }

    /**
     * Sample code for starting a recording.
     * 
     * @param conversationClient {@link ConversationClient} to use for recording.
     * @return recordingId to use with other recording operations.
     */
    public String startRecording(ConversationClient conversationClient) {
        String conversationId = "<conversationId recieved from starting call>";
        String recordingStateCallbackUri = "<webhook endpoint to which calling service can report status>";
        StartCallRecordingResponse response = conversationClient.startRecording(conversationId, recordingStateCallbackUri);
        String recordingId = response.getRecordingId();
        return recordingId;
    }

    /**
     * Sample code for pausing a recording.
     * 
     * @param conversationClient {@link ConversationClient} to use for recording.
     * @param conversationId Identifier of the current conversation (call).
     * @param recordingId Identifier of the recording to pause.
     */
    public void pauseRecording(ConversationClient conversationClient,
            String conversationId, String recordingId) {
        conversationClient.pauseRecording(conversationId, recordingId);
    }

    /**
     * Sample code for resuming a recording.
     * 
     * @param conversationClient {@link ConversationClient} to use for recording.
     * @param conversationId Identifier of the current conversation (call).
     * @param recordingId Identifier of the recording to resume.
     */
    public void resumeRecording(ConversationClient conversationClient,
            String conversationId, String recordingId) {
        conversationClient.resumeRecording(conversationId, recordingId);
    }

    /**
     * Sample code for stoping a recording.
     * 
     * @param conversationClient {@link ConversationClient} to use for recording.
     * @param conversationId Identifier of the current conversation (call).
     * @param recordingId Identifier of the recording to stop.
     */
    public void stopRecording(ConversationClient conversationClient,
            String conversationId, String recordingId) {
        conversationClient.stopRecording(conversationId, recordingId);
    }

    /**
     * Sample code for requesting the state of a recording.
     * 
     * @param conversationClient {@link ConversationClient} to use for recording.
     * @param conversationId Identifier of the current conversation (call).
     * @param recordingId Identifier of the recording from which to request state.
     * @return state of the recording, {@link CallRecordingState}.
     */
    public CallRecordingState getRecordingState(ConversationClient conversationClient,
            String conversationId, String recordingId) {
        CallRecordingStateResponse callRecordingStateResponse =
            conversationClient.getRecordingState(conversationId, recordingId);
        
        /**
         * CallRecordingState: Active, Inactive
         * If the call has ended, CommunicationErrorException will be thrown. Inactive is
         * only returned when the recording is paused.
         */
        CallRecordingState callRecordingState = callRecordingStateResponse.getRecordingState();
        return callRecordingState;
    }

    /**
     * Sample code for playing an audio notification in a call.
     * 
     * @param conversationClient {@link ConversationClient} to use for recording.
     * @param conversationId Identifier of the current conversation (call).
     * @return information about the play audio request, {@link PlayAudioResponse}.
     */
    public PlayAudioResponse playAudio(ConversationClient conversationClient, String conversationId) {
        String audioFileUri = "<uri of the file to play>";
        String audioFileId = "<a name to use for caching the audio file>";
        String callbackUri = "<webhook endpoint to which calling service can report status>";
        String context = "<Identifier for correlating responses>";
        PlayAudioResponse playAudioResponse = conversationClient.playAudio(conversationId, audioFileUri, audioFileId, callbackUri, context);
        return playAudioResponse;
    }
}
