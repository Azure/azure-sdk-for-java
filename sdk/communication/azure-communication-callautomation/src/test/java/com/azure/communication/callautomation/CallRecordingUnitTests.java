// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.communication.callautomation.models.RecordingChannel;
import com.azure.communication.callautomation.models.RecordingContent;
import com.azure.communication.callautomation.models.RecordingFormat;
import com.azure.communication.callautomation.models.RecordingState;
import com.azure.communication.callautomation.models.RecordingStateResult;
import com.azure.communication.callautomation.models.ServerCallLocator;
import com.azure.communication.callautomation.models.StartRecordingOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CallRecordingUnitTests extends CallRecordingUnitTestBase {
    private CallRecording callRecording;

    @BeforeEach
    public void setup() {
        CallAutomationClient callAutomationClient = CallAutomationUnitTestBase.getCallAutomationClient(new ArrayList<>());
        callRecording = callAutomationClient.getCallRecording();
    }
    @Test
    public void startRecordingRelativeUriFails() {
        assertThrows(
            InvalidParameterException.class,
            () -> callRecording.startRecording(new StartRecordingOptions(new ServerCallLocator(SERVER_CALL_ID))
                    .setRecordingStateCallbackUrl("/not/absolute/uri")
            ));
    }

    @Test
    public void startRecordingWithFullParamsFails() {
        StartRecordingOptions startRecordingOptions = new StartRecordingOptions(new ServerCallLocator(SERVER_CALL_ID))
            .setRecordingContent(RecordingContent.AUDIO_VIDEO)
            .setRecordingChannel(RecordingChannel.MIXED)
            .setRecordingFormat(RecordingFormat.MP4)
            .setRecordingStateCallbackUrl("/not/absolute/uri")
            .setAudioChannelParticipantOrdering(new ArrayList<CommunicationIdentifier>(Arrays.asList(
                new CommunicationUserIdentifier("rawId1"),
                new CommunicationUserIdentifier("rawId2"))));

        assertThrows(
            InvalidParameterException.class,
            () -> callRecording.startRecordingWithResponse(startRecordingOptions, Context.NONE)
        );
    }

    @Test
    public void recordingOperationsTest() {

        CallAutomationClient callAutomationClient = CallAutomationUnitTestBase.getCallAutomationClient(
            recordingOperationsResponses
        );
        callRecording = callAutomationClient.getCallRecording();

        validateRecording(
            callRecording.startRecording(new StartRecordingOptions(new ServerCallLocator(SERVER_CALL_ID))
                .setRecordingStateCallbackUrl("https://localhost/")),
            RecordingState.ACTIVE
        );

        verifyOperationWithRecordingState(
            () -> callRecording.pauseRecording(RECORDING_ID),
            RecordingState.INACTIVE
        );

        verifyOperationWithRecordingState(
            () -> callRecording.resumeRecording(RECORDING_ID),
            RecordingState.ACTIVE
        );

        callRecording.stopRecording(RECORDING_ID);
        assertThrows(CallingServerErrorException.class, () -> callRecording.getRecordingState(RECORDING_ID));
    }

    private void verifyOperationWithRecordingState(Runnable operation, RecordingState expectedStatus) {
        operation.run();
        RecordingStateResult recordingState = callRecording.getRecordingState(RECORDING_ID);
        validateRecording(recordingState, expectedStatus);
    }

    private void validateRecording(RecordingStateResult recordingState, RecordingState expectedStatus) {
        assertEquals(RECORDING_ID, recordingState.getRecordingId());
        assertEquals(expectedStatus, recordingState.getRecordingState());
    }
}
