// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.RecordingChannel;
import com.azure.communication.callingserver.models.RecordingContent;
import com.azure.communication.callingserver.models.RecordingFormat;
import com.azure.communication.callingserver.models.RecordingStatus;
import com.azure.communication.callingserver.models.RecordingStatusResponse;
import com.azure.communication.callingserver.models.ServerCallLocator;
import com.azure.communication.callingserver.models.StartRecordingOptions;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CallRecordingUnitTests extends CallRecordingTestBase {
    private CallRecording callRecording;

    @BeforeEach
    public void setup() {
        CallingServerClient callingServerClient = CallingServerResponseMocker.getCallingServerClient(new ArrayList<>());
        callRecording = callingServerClient.getCallRecording();
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

        CallingServerClient callingServerClient = CallingServerResponseMocker.getCallingServerClient(
            recordingOperationsResponses
        );
        callRecording = callingServerClient.getCallRecording();

        RecordingStatusResponse recordingState = callRecording.startRecording(
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
        assertThrows(CallingServerErrorException.class, () -> callRecording.getRecordingState(RECORDING_ID));
    }

    private void verifyOperationWithRecordingStatus(Runnable operation, RecordingStatus expectedStatus) {
        operation.run();
        RecordingStatusResponse recordingState = callRecording.getRecordingState(RECORDING_ID);
        validateRecording(recordingState, expectedStatus);
    }

    private void validateRecording(RecordingStatusResponse recordingStatus, RecordingStatus expectedStatus) {
        assertEquals(RECORDING_ID, recordingStatus.getRecordingId());
        assertEquals(expectedStatus, recordingStatus.getRecordingStatus());
    }
}
