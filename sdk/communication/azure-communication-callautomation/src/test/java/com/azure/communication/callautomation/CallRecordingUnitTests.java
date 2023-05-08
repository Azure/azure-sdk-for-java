// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.ChannelAffinity;
import com.azure.communication.callautomation.models.RecordingState;
import com.azure.communication.callautomation.models.RecordingStateResult;
import com.azure.communication.callautomation.models.ServerCallLocator;
import com.azure.communication.callautomation.models.StartRecordingOptions;
import com.azure.communication.common.PhoneNumberIdentifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.azure.core.exception.HttpResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public void recordingOperationsTest() {

        CallAutomationClient callAutomationClient = CallAutomationUnitTestBase.getCallAutomationClient(
            recordingOperationsResponses
        );
        callRecording = callAutomationClient.getCallRecording();
        StartRecordingOptions startRecordingOptions = new StartRecordingOptions(new ServerCallLocator(SERVER_CALL_ID))
            .setRecordingStateCallbackUrl("https://localhost/");

        ChannelAffinity channelAffinity = new ChannelAffinity()
            .setParticipant(new PhoneNumberIdentifier("RECORDING_ID"))
            .setChannel(0);

        List<ChannelAffinity> channelAffinities = Arrays.asList(channelAffinity);
        startRecordingOptions.setChannelAffinity(channelAffinities);

        validateRecording(
            callRecording.start(startRecordingOptions),
            RecordingState.ACTIVE
        );

        verifyOperationWithRecordingState(
            () -> callRecording.pause(RECORDING_ID),
            RecordingState.INACTIVE
        );

        verifyOperationWithRecordingState(
            () -> callRecording.resume(RECORDING_ID),
            RecordingState.ACTIVE
        );

        callRecording.stop(RECORDING_ID);
        assertThrows(HttpResponseException.class, () -> callRecording.getState(RECORDING_ID));
    }

    private void verifyOperationWithRecordingState(Runnable operation, RecordingState expectedStatus) {
        operation.run();
        RecordingStateResult recordingState = callRecording.getState(RECORDING_ID);
        validateRecording(recordingState, expectedStatus);
    }

    private void validateRecording(RecordingStateResult recordingState, RecordingState expectedStatus) {
        assertEquals(RECORDING_ID, recordingState.getRecordingId());
        assertEquals(expectedStatus, recordingState.getRecordingState());
    }
}
