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
import org.reactivestreams.Publisher;
import reactor.test.StepVerifier;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CallRecordingAsyncUnitTests extends CallRecordingUnitTestBase {
    private CallRecordingAsync callRecording;

    @BeforeEach
    public void setup() {
        CallAutomationAsyncClient callingServerClient = CallAutomationUnitTestBase.getCallAutomationAsyncClient(new ArrayList<>());
        callRecording = callingServerClient.getCallRecordingAsync();
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void recordingOperationsTest() {
        CallAutomationAsyncClient callingServerClient = CallAutomationUnitTestBase.getCallAutomationAsyncClient(
            recordingOperationsResponses
        );
        callRecording = callingServerClient.getCallRecordingAsync();

        validateRecordingState(
            callRecording.startRecording(new StartRecordingOptions(new ServerCallLocator(SERVER_CALL_ID))
                    .setRecordingStateCallbackUrl("https://localhost/")),
            RecordingState.ACTIVE
        );

        validateOperationWithRecordingState(callRecording.pauseRecording(RECORDING_ID),
            RecordingState.INACTIVE
        );

        validateOperationWithRecordingState(callRecording.resumeRecording(RECORDING_ID),
            RecordingState.ACTIVE);

        validateOperation(callRecording.stopRecording(RECORDING_ID));
        validateError(CallingServerErrorException.class, callRecording.getRecordingState(RECORDING_ID));
    }

    private void validateRecordingState(Publisher<RecordingStateResult> publisher, RecordingState status) {
        StepVerifier.create(publisher)
            .consumeNextWith(recordingStateResponse -> validateRecording(recordingStateResponse, status))
            .verifyComplete();
    }

    private void validateOperationWithRecordingState(Publisher<Void> operation, RecordingState expectedRecordingState) {
        validateOperation(operation);
        validateRecordingState(
            callRecording.getRecordingState(RECORDING_ID),
            expectedRecordingState
        );
    }

    private void validateOperation(Publisher<Void> operation) {
        StepVerifier.create(operation).verifyComplete();
    }

    private <T, U> void validateError(Class<T> exception, Publisher<U> publisher) {
        StepVerifier.create(publisher)
            .consumeErrorWith(error -> assertEquals(error.getClass().toString(),
                exception.toString()))
            .verify();
    }

    private void validateRecording(RecordingStateResult recordingState, RecordingState expectedStatus) {
        assertEquals(RECORDING_ID, recordingState.getRecordingId());
        assertEquals(expectedStatus, recordingState.getRecordingState());
    }
}
