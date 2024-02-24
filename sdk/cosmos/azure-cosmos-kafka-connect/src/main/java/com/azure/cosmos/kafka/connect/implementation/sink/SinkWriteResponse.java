package com.azure.cosmos.kafka.connect.implementation.sink;

import org.apache.kafka.connect.sink.SinkRecord;

import java.util.ArrayList;
import java.util.List;

public class SinkWriteResponse {
    private final List<SinkRecord> succeededRecords;
    private List<SinkOperationFailedResponse> failedRecordResponses;

    public SinkWriteResponse() {
        succeededRecords = new ArrayList<>();
        failedRecordResponses = new ArrayList<>();
    }

    public List<SinkRecord> getSucceededRecords() {
        return succeededRecords;
    }

    public List<SinkOperationFailedResponse> getFailedRecordResponses() {
        return failedRecordResponses;
    }

    public void setFailedRecordResponses(List<SinkOperationFailedResponse> failedRecordResponses) {
        this.failedRecordResponses = failedRecordResponses;
    }
}
