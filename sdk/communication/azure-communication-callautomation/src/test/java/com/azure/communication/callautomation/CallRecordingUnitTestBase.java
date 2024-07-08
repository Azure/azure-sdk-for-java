// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.models.RecordingStateInternal;
import com.azure.communication.callautomation.implementation.models.RecordingKind;
import com.azure.communication.callautomation.implementation.models.RecordingStateResponseInternal;

import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;

public class CallRecordingUnitTestBase {
    static final String SERVER_CALL_ID = "aHR0cHM6Ly9jb252LXVzd2UtMDguY29udi5za3lwZS5jb20vY29udi8tby1FWjVpMHJrS3RFTDBNd0FST1J3P2k9ODgmZT02Mzc1Nzc0MTY4MDc4MjQyOTM";

    static final String RECORDING_ID = "recordingId";

    private final RecordingStateResponseInternal recordingState = new RecordingStateResponseInternal().setRecordingId(RECORDING_ID);

    private final String recordingActive = generateGetParticipantResponse(RecordingStateInternal.ACTIVE, RecordingKind.TEAMS);
    private final String recordingInactive = generateGetParticipantResponse(RecordingStateInternal.INACTIVE, RecordingKind.TEAMS);

    ArrayList<AbstractMap.SimpleEntry<String, Integer>> recordingOperationsResponses = new ArrayList<>(Arrays.asList(
        new AbstractMap.SimpleEntry<>(recordingActive, 200),   //startRecording
        new AbstractMap.SimpleEntry<>("", 202),                //pauseRecording
        new AbstractMap.SimpleEntry<>(recordingInactive, 200), //getRecordingState
        new AbstractMap.SimpleEntry<>("", 202),                //resumeRecording
        new AbstractMap.SimpleEntry<>(recordingActive, 200),   //getRecordingState
        new AbstractMap.SimpleEntry<>("", 204),                //stopRecording
        new AbstractMap.SimpleEntry<>("", 404)                 //getRecordingState
    ));

    private String serializeObject(RecordingStateResponseInternal o) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            o.toJson(writer);
            writer.flush();
            return outputStream.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateGetParticipantResponse(RecordingStateInternal recordingState, RecordingKind recordingKind) {

        RecordingStateResponseInternal response = new RecordingStateResponseInternal();
        response.setRecordingState(recordingState);
        response.setRecordingKind(recordingKind);
        response.setRecordingId(RECORDING_ID);

        return serializeObject(response);
    }
}
