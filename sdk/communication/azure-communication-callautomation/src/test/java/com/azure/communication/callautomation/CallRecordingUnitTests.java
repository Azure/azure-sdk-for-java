// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallAutomationErrorException;
import com.azure.communication.callautomation.models.RecordingChannel;
import com.azure.communication.callautomation.models.RecordingContent;
import com.azure.communication.callautomation.models.RecordingFormat;
import com.azure.communication.callautomation.models.RecordingStatus;
import com.azure.communication.callautomation.models.RecordingStatusResult;
import com.azure.communication.callautomation.models.ServerCallLocator;
import com.azure.communication.callautomation.models.StartRecordingOptions;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.InvalidParameterException;
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
    public void startRecordingRelativeUriFails() {
        assertThrows(
            InvalidParameterException.class,
            () -> callRecording.startRecording(
                new ServerCallLocator(SERVER_CALL_ID),
                URI.create("/not/absolute/uri")
            ));
    }

    @Test
    public void startRecordingWithFullParamsFails() {
        StartRecordingOptions startRecordingOptions = new StartRecordingOptions(
            RecordingContent.AUDIO_VIDEO,
            RecordingFormat.MP4,
            RecordingChannel.MIXED
        );

        assertThrows(
            InvalidParameterException.class,
            () -> callRecording.startRecordingWithResponse(
                new ServerCallLocator(SERVER_CALL_ID),
                URI.create("/not/absolute/uri"),
                startRecordingOptions,
                Context.NONE
            )
        );
    }

    @Test
    public void recordingOperationsTest() {

        CallAutomationClient callAutomationClient = CallAutomationUnitTestBase.getCallAutomationClient(
            recordingOperationsResponses
        );
        callRecording = callAutomationClient.getCallRecording();

        RecordingStatusResult recordingState = callRecording.startRecording(
            new ServerCallLocator(SERVER_CALL_ID),
            URI.create("https://localhost/")
        );
        validateRecording(recordingState, RecordingStatus.ACTIVE);

        verifyOperationWithRecordingStatus(
            () -> callRecording.pauseRecording(RECORDING_ID),
            RecordingStatus.INACTIVE
        );

        verifyOperationWithRecordingStatus(
            () -> callRecording.resumeRecording(RECORDING_ID),
            RecordingStatus.ACTIVE
        );

        callRecording.stopRecording(RECORDING_ID);
        assertThrows(CallAutomationErrorException.class, () -> callRecording.getRecordingState(RECORDING_ID));
    }

    private void verifyOperationWithRecordingStatus(Runnable operation, RecordingStatus expectedStatus) {
        operation.run();
        RecordingStatusResult recordingState = callRecording.getRecordingState(RECORDING_ID);
        validateRecording(recordingState, expectedStatus);
    }

    private void validateRecording(RecordingStatusResult recordingStatus, RecordingStatus expectedStatus) {
        assertEquals(RECORDING_ID, recordingStatus.getRecordingId());
        assertEquals(expectedStatus, recordingStatus.getRecordingStatus());
    }
}
