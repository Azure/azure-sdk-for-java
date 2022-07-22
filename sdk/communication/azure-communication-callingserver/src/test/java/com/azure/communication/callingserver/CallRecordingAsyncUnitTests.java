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
import org.reactivestreams.Publisher;
import reactor.test.StepVerifier;

import java.net.URI;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CallRecordingAsyncUnitTests extends CallRecordingTestBase {
    private CallRecordingAsync callRecording;

    @BeforeEach
    public void setup() {
        CallingServerAsyncClient callingServerClient = CallingServerResponseMocker.getCallingServerAsyncClient(new ArrayList<>());
        callRecording = callingServerClient.getCallRecordingAsync();
    }

    @Test
    public void startRecordingRelativeUriFails() {
        validateError(InvalidParameterException.class, callRecording.startRecording(
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

        validateError(InvalidParameterException.class, callRecording.startRecordingWithResponse(
            new ServerCallLocator(SERVER_CALL_ID),
            URI.create("/not/absolute/uri"),
            startRecordingOptions,
            Context.NONE
        ));
    }

    @Test
    public void recordingOperationsTest() {
        CallingServerAsyncClient callingServerClient = CallingServerResponseMocker.getCallingServerAsyncClient(
            recordingOperationsResponses
        );
        callRecording = callingServerClient.getCallRecordingAsync();

        validateRecordingStatus(
            callRecording.startRecording(new ServerCallLocator(SERVER_CALL_ID), URI.create("https://localhost/")),
            RecordingStatus.ACTIVE
        );

        validateOperationWithRecordingStatus(callRecording.pauseRecording(RECORDING_ID),
            RecordingStatus.INACTIVE
        );

        validateOperationWithRecordingStatus(callRecording.resumeRecording(RECORDING_ID),
            RecordingStatus.ACTIVE);

        validateOperation(callRecording.stopRecording(RECORDING_ID));
        validateError(CallingServerErrorException.class, callRecording.getRecordingState(RECORDING_ID));
    }

    private void validateRecordingStatus(Publisher<RecordingStatusResponse> publisher, RecordingStatus status) {
        StepVerifier.create(publisher)
            .consumeNextWith(recordingStatusResponse -> validateRecording(recordingStatusResponse, status))
            .verifyComplete();
    }

    private void validateOperationWithRecordingStatus(Publisher<Void> operation, RecordingStatus expectedRecordingStatus) {
        validateOperation(operation);
        validateRecordingStatus(
            callRecording.getRecordingState(RECORDING_ID),
            expectedRecordingStatus
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

    private void validateRecording(RecordingStatusResponse recordingStatus, RecordingStatus expectedStatus) {
        assertEquals(RECORDING_ID, recordingStatus.getRecordingId());
        assertEquals(expectedStatus, recordingStatus.getRecordingStatus());
    }
}
