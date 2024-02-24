package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.CosmosAsyncContainer;
import org.apache.kafka.connect.sink.SinkRecord;

import java.util.List;

public interface IWriter {
    SinkWriteResponse write(CosmosAsyncContainer container, List<SinkRecord> sinkRecords);
}
