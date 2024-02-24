package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.CosmosException;
import org.apache.kafka.connect.sink.SinkRecord;

public class SinkOperationFailedResponse {
    private final SinkRecord sinkRecord;
    private final Throwable exception;

    public SinkOperationFailedResponse(SinkRecord sinkRecord, Throwable cosmosException) {
        this.sinkRecord = sinkRecord;
        this.exception = cosmosException;
    }

    public SinkRecord getSinkRecord() {
        return this.sinkRecord;
    }

    public Throwable getException() {
        return this.exception;
    }
}
