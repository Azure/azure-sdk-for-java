// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosExceptionsHelper;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.apache.kafka.connect.sink.ErrantRecordReporter;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public abstract class CosmosWriterBase implements IWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosWriterBase.class);
    private static final String ID = "id";
    private static final String ETAG = "_etag";
    private final ErrantRecordReporter errantRecordReporter;

    public CosmosWriterBase(ErrantRecordReporter errantRecordReporter) {
        this.errantRecordReporter = errantRecordReporter;
    }

    abstract void writeCore(CosmosAsyncContainer container, List<SinkOperation> sinkOperations);

    @Override
    public void write(CosmosAsyncContainer container, List<SinkRecord> sinkRecords) {
        if (sinkRecords == null || sinkRecords.isEmpty()) {
            LOGGER.debug("No records to be written to container {}", container.getId());
            return;
        }
        LOGGER.debug("Write {} records to container {}", sinkRecords.size(), container.getId());

        // For each sinkRecord, it has a 1:1 mapping SinkOperation which contains sinkRecord and related context: retryCount, succeeded or failure.
        List<SinkOperation> sinkOperations =
            sinkRecords
                .stream()
                .map(sinkRecord -> new SinkOperation(sinkRecord))
                .collect(Collectors.toList());

        try {
            writeCore(container, sinkOperations);
        } catch (Exception e) {
            LOGGER.error("Write failed. ", e);
            throw new CosmosWriteException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    protected String getId(Object recordValue) {
        checkArgument(recordValue instanceof Map, "Argument 'recordValue' is not valid map format.");
        return ((Map<String, Object>) recordValue).get(ID).toString();
    }

    @SuppressWarnings("unchecked")
    protected String getEtag(Object recordValue) {
        checkArgument(recordValue instanceof Map, "Argument 'recordValue' is not valid map format.");
        return ((Map<String, Object>) recordValue).getOrDefault(ETAG, Strings.Emtpy).toString();
    }

    @SuppressWarnings("unchecked")
    protected PartitionKey getPartitionKeyValue(Object recordValue, PartitionKeyDefinition partitionKeyDefinition) {
        checkArgument(recordValue instanceof Map, "Argument 'recordValue' is not valid map format.");

        Map<String, Object> recordMap = (Map<String, Object>) recordValue;
        return PartitionKey.fromItem(recordMap, partitionKeyDefinition);
    }

    protected boolean shouldRetry(Throwable exception, int attemptedCount, int maxRetryCount) {
        if (attemptedCount >= maxRetryCount) {
            return false;
        }

        return KafkaCosmosExceptionsHelper.isTransientFailure(exception);
    }

    protected void sendToDlqIfConfigured(SinkOperation sinkOperationContext) {
        if (this.errantRecordReporter != null) {
            errantRecordReporter.report(sinkOperationContext.getSinkRecord(), sinkOperationContext.getException());
        }
    }
}
