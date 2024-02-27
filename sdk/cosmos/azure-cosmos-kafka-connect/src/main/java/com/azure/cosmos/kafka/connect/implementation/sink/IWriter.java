// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.CosmosAsyncContainer;
import org.apache.kafka.connect.sink.SinkRecord;

import java.util.List;

public interface IWriter {
    void write(CosmosAsyncContainer container, List<SinkRecord> sinkRecords);
}
