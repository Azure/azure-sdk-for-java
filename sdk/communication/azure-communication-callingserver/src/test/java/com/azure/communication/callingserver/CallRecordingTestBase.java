// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.models.RecordingStatusInternal;
import com.azure.communication.callingserver.implementation.models.RecordingStatusResponseInternal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;

public class CallRecordingTestBase {
    static final String SERVER_CALL_ID = "aHR0cHM6Ly9jb252LXVzd2UtMDguY29udi5za3lwZS5jb20vY29udi8tby1FWjVpMHJrS3RFTDBNd0FST1J3P2k9ODgmZT02Mzc1Nzc0MTY4MDc4MjQyOTM";

    static final String RECORDING_ID = "recordingId";
    private final RecordingStatusResponseInternal recordingStatus = new RecordingStatusResponseInternal().setRecordingId(RECORDING_ID);
    private final String recordingActive = serializeObject(recordingStatus.setRecordingStatus(RecordingStatusInternal.ACTIVE));
    private final String recordingInactive = serializeObject(recordingStatus.setRecordingStatus(RecordingStatusInternal.INACTIVE));

    ArrayList<AbstractMap.SimpleEntry<String, Integer>> recordingOperationsResponses = new ArrayList<>(Arrays.asList(
        new AbstractMap.SimpleEntry<>(recordingActive, 200),   //startRecording
        new AbstractMap.SimpleEntry<>("", 202),                //pauseRecording
        new AbstractMap.SimpleEntry<>(recordingInactive, 200), //getRecordingState
        new AbstractMap.SimpleEntry<>("", 202),                //resumeRecording
        new AbstractMap.SimpleEntry<>(recordingActive, 200),   //getRecordingState
        new AbstractMap.SimpleEntry<>("", 204),                //stopRecording
        new AbstractMap.SimpleEntry<>("", 404)                 //getRecordingState
    ));

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
