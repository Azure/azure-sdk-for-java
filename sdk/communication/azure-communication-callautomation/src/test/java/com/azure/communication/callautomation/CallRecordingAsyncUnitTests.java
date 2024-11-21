// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AzureBlobContainerRecordingStorage;
import com.azure.communication.callautomation.models.RecordingState;
import com.azure.communication.callautomation.models.RecordingStateResult;
import com.azure.communication.callautomation.models.ServerCallLocator;
import com.azure.communication.callautomation.models.StartRecordingOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.test.StepVerifier;
import com.azure.core.exception.HttpResponseException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CallRecordingAsyncUnitTests extends CallRecordingUnitTestBase {
    private CallRecordingAsync callRecording;

    @BeforeEach
    public void setup() {
        CallAutomationAsyncClient callingServerClient = CallAutomationUnitTestBase.getCallAutomationAsyncClient(new ArrayList<>());
        callRecording = callingServerClient.getCallRecordingAsync();
    }

    @Test
    public void recordingOperationsTest() {
        CallAutomationAsyncClient callingServerClient = CallAutomationUnitTestBase.getCallAutomationAsyncClient(
            recordingOperationsResponses
        );
        callRecording = callingServerClient.getCallRecordingAsync();

        validateRecordingState(
            callRecording.start(new StartRecordingOptions(new ServerCallLocator(SERVER_CALL_ID))
                    .setRecordingStateCallbackUrl("https://localhost/")),
            RecordingState.ACTIVE
        );

        validateOperationWithRecordingState(callRecording.pause(RECORDING_ID),
            RecordingState.INACTIVE
        );

        validateOperationWithRecordingState(callRecording.resume(RECORDING_ID),
            RecordingState.ACTIVE);

        validateOperation(callRecording.stop(RECORDING_ID));
        assertThrows(HttpResponseException.class, () -> callRecording.getState(RECORDING_ID).block());
    }

    @Test
    public void recordingOperationsTestBYOS() {
        CallAutomationAsyncClient callingServerClient = CallAutomationUnitTestBase.getCallAutomationAsyncClient(
            recordingOperationsResponses
        );
        callRecording = callingServerClient.getCallRecordingAsync();

        validateRecordingState(
            callRecording.start(new StartRecordingOptions(new ServerCallLocator(SERVER_CALL_ID))
                    .setRecordingStateCallbackUrl("https://localhost/")
                    .setRecordingStorage(new AzureBlobContainerRecordingStorage("https://dummyurl/"))
                    ),
            RecordingState.ACTIVE
        );

        validateOperationWithRecordingState(callRecording.pause(RECORDING_ID),
            RecordingState.INACTIVE
        );

        validateOperationWithRecordingState(callRecording.resume(RECORDING_ID),
            RecordingState.ACTIVE);

        validateOperation(callRecording.stop(RECORDING_ID));
        assertThrows(HttpResponseException.class, () -> callRecording.getState(RECORDING_ID).block());
    }

    private void validateRecordingState(Publisher<RecordingStateResult> publisher, RecordingState status) {
        StepVerifier.create(publisher)
            .consumeNextWith(recordingStateResponse -> validateRecording(recordingStateResponse, status))
            .verifyComplete();
    }

    private void validateOperationWithRecordingState(Publisher<Void> operation, RecordingState expectedRecordingState) {
        validateOperation(operation);
        validateRecordingState(
            callRecording.getState(RECORDING_ID),
            expectedRecordingState
        );
    }

    private void validateOperation(Publisher<Void> operation) {
        StepVerifier.create(operation).verifyComplete();
    }

    private void validateRecording(RecordingStateResult recordingState, RecordingState expectedStatus) {
        assertEquals(RECORDING_ID, recordingState.getRecordingId());
        assertEquals(expectedStatus, recordingState.getRecordingState());
    }
}
