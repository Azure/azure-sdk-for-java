// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.RecordingState;
import com.azure.communication.callingserver.models.RecordingStateResult;
import com.azure.communication.callingserver.models.ServerCallLocator;
import com.azure.communication.callingserver.models.StartRecordingOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

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
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
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
