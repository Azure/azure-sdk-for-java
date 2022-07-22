// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.models.RecordingStatusInternal;
import com.azure.communication.callingserver.implementation.models.RecordingStatusResponseInternal;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.RecordingChannel;
import com.azure.communication.callingserver.models.RecordingContent;
import com.azure.communication.callingserver.models.RecordingFormat;
import com.azure.communication.callingserver.models.RecordingStatus;
import com.azure.communication.callingserver.models.RecordingStatusResponse;
import com.azure.communication.callingserver.models.ServerCallLocator;
import com.azure.communication.callingserver.models.StartRecordingOptions;
import com.azure.core.util.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.InvalidParameterException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CallRecordingUnitTests {
    private static final String SERVER_CALL_ID = "aHR0cHM6Ly9jb252LXVzd2UtMDguY29udi5za3lwZS5jb20vY29udi8tby1FWjVpMHJrS3RFTDBNd0FST1J3P2k9ODgmZT02Mzc1Nzc0MTY4MDc4MjQyOTM";

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
        RecordingStatusResponseInternal recordingStatus = new RecordingStatusResponseInternal().setRecordingId("recordingId");
        String recordingActive = serializeObject(recordingStatus.setRecordingStatus(RecordingStatusInternal.ACTIVE));
        String recordingInactive = serializeObject(recordingStatus.setRecordingStatus(RecordingStatusInternal.INACTIVE));

        CallingServerClient callingServerClient = CallingServerResponseMocker.getCallingServerClient(new ArrayList<>(Arrays.asList(
                new AbstractMap.SimpleEntry<>(recordingActive, 200),   //startRecording
                new AbstractMap.SimpleEntry<>(recordingActive, 200),   //getRecordingState
                new AbstractMap.SimpleEntry<>("", 202),                //pauseRecording
                new AbstractMap.SimpleEntry<>(recordingInactive, 200), //getRecordingState
                new AbstractMap.SimpleEntry<>("", 202),                //resumeRecording
                new AbstractMap.SimpleEntry<>(recordingActive, 200),   //getRecordingState
                new AbstractMap.SimpleEntry<>("", 204),                //stopRecording
                new AbstractMap.SimpleEntry<>("", 404)                 //getRecordingState
            )
        ));
        callRecording = callingServerClient.getCallRecording();

        RecordingStatusResponse recordingState = callRecording.startRecording(
            new ServerCallLocator(SERVER_CALL_ID),
            URI.create("https://localhost/")
        );
        validateRecordingActive(recordingState);

        recordingState = callRecording.getRecordingState("recordingId");
        validateRecordingActive(recordingState);

        callRecording.pauseRecording("recordingId");
        recordingState = callRecording.getRecordingState("recordingId");
        validateRecordingInactive(recordingState);

        callRecording.resumeRecording("recordingId");
        recordingState = callRecording.getRecordingState("recordingId");
        validateRecordingActive(recordingState);

        callRecording.stopRecording("recordingId");
        assertThrows(CallingServerErrorException.class, () -> callRecording.getRecordingState("recordingId"));
    }

    private void validateRecordingActive(RecordingStatusResponse recordingState) {
        validateRecording(recordingState, RecordingStatus.ACTIVE);
    }

    private void validateRecordingInactive(RecordingStatusResponse recordingState) {
        validateRecording(recordingState, RecordingStatus.INACTIVE);
    }

    private void validateRecording(RecordingStatusResponse recordingStatus, RecordingStatus expectedStatus) {
        assertEquals("recordingId", recordingStatus.getRecordingId());
        assertEquals(expectedStatus, recordingStatus.getRecordingStatus());
    }
    private String serializeObject(Object o) {
        ObjectMapper mapper = new ObjectMapper();
        String body = null;
        try {
            body = mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return body;
    }
}
