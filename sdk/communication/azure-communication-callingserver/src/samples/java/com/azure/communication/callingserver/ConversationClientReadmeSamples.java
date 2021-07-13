// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallRecordingState;
import com.azure.communication.callingserver.models.CallRecordingProperties;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.StartCallRecordingResult;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */

public class ConversationClientReadmeSamples {

    /**
     * Sample code for creating a sync calling server client.
     *
     * @return the calling server client.
     */
    public CallingServerClient createCallingServerClient() {
        String endpoint = "https://<RESOURCE_NAME>.communcationservices.azure.com";

        // Your connectionString retrieved from your Azure Communication Service
        String connectionString = "endpoint=https://<resource-name>.communication.azure.com/;accesskey=<access-key>";

        // Initialize the calling server client
        final CallingServerClientBuilder builder = new CallingServerClientBuilder();
        builder.connectionString(connectionString);
        CallingServerClient callingServerClient = builder.buildClient();

        return callingServerClient;
    }

    /**
     * Sample code for starting a recording.
     *
     * @param callingServerClient {@link CallingServerClient} to use for recording.
     * @return recordingId to use with other recording operations.
     */
    public String startRecording(CallingServerClient callingServerClient) {
        String serverCallId = "<serverCallId received from starting call>";
        String recordingStateCallbackUri = "<webhook endpoint to which calling service can report status>";
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);
        StartCallRecordingResult result = serverCall.startRecording(recordingStateCallbackUri);
        String recordingId = result.getRecordingId();
        return recordingId;
    }

    /**
     * Sample code for pausing a recording.
     *
     * @param callingServerClient {@link CallingServerClient} to use for recording.
     * @param serverCallId Identifier of the current server call.
     * @param recordingId Identifier of the recording to pause.
     */
    public void pauseRecording(CallingServerClient callingServerClient,
                               String serverCallId, String recordingId) {
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);
        serverCall.pauseRecording(recordingId);
    }

    /**
     * Sample code for resuming a recording.
     *
     * @param callingServerClient {@link CallingServerClient} to use for recording.
     * @param serverCallId Identifier of the current server call.
     * @param recordingId Identifier of the recording to resume.
     */
    public void resumeRecording(CallingServerClient callingServerClient,
                                String serverCallId, String recordingId) {
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);
        serverCall.resumeRecording(recordingId);
    }

    /**
     * Sample code for stopping a recording.
     *
     * @param callingServerClient {@link CallingServerClient} to use for recording.
     * @param serverCallId Identifier of the current server call.
     * @param recordingId Identifier of the recording to stop.
     */
    public void stopRecording(CallingServerClient callingServerClient,
                              String serverCallId, String recordingId) {
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);
        serverCall.stopRecording(recordingId);
    }

    /**
     * Sample code for requesting the state of a recording.
     *
     * @param callingServerClient {@link CallingServerClient} to use for recording.
     * @param serverCallId Identifier of the current server call.
     * @param recordingId Identifier of the recording from which to request state.
     * @return state of the recording, {@link CallRecordingState}.
     */
    public CallRecordingState getRecordingState(CallingServerClient callingServerClient,
                                                String serverCallId, String recordingId) {
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);
        CallRecordingProperties callRecordingStateResult = serverCall.getRecordingState(recordingId);

        // CallRecordingState: Active, Inactive
        // If the call has ended, CommunicationErrorException will be thrown. Inactive is
        // only returned when the recording is paused.
        CallRecordingState callRecordingState = callRecordingStateResult.getRecordingState();
        return callRecordingState;
    }

    /**
     * Sample code for playing an audio notification in a call.
     *
     * @param callingServerClient {@link CallingServerClient} to use for recording.
     * @param serverCallId Identifier of the current server call.
     * @return information about the play audio request, {@link PlayAudioResult}.
     */
    public PlayAudioResult playAudio(CallingServerClient callingServerClient, String serverCallId) {
        String audioFileUri = "<uri of the file to play>";
        String audioFileId = "<a name to use for caching the audio file>";
        String callbackUri = "<webhook endpoint to which calling service can report status>";
        String context = "<Identifier for correlating responses>";
        ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);
        PlayAudioResult playAudioResult = serverCall.playAudio(audioFileUri, audioFileId, callbackUri, context);
        return playAudioResult;
    }
}
