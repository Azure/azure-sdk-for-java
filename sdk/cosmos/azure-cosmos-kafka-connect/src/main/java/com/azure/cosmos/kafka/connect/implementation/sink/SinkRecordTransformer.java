// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.AbstractIdStrategyConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.FullKeyStrategy;
import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.IdStrategy;
import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.KafkaMetadataStrategy;
import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.ProvidedInKeyStrategy;
import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.ProvidedInValueStrategy;
import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.TemplateStrategy;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SinkRecordTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SinkRecordTransformer.class);

    private final IdStrategy idStrategy;

    public SinkRecordTransformer(CosmosSinkTaskConfig sinkTaskConfig) {
        this.idStrategy = this.createIdStrategy(sinkTaskConfig);
    }

    @SuppressWarnings("unchecked")
    public List<SinkRecord> transform(String containerName, List<SinkRecord> sinkRecords) {
        List<SinkRecord> toBeWrittenRecordList = new ArrayList<>();
        for (SinkRecord record : sinkRecords) {
            if (record.key() != null) {
                MDC.put(String.format("CosmosDbSink-%s", containerName), record.key().toString());
            }

            LOGGER.trace(
                "Key Schema [{}], Key [{}], Value type [{}], Value schema [{}]",
                record.keySchema(),
                record.key(),
                record.value() == null ? null : record.value().getClass().getName(),
                record.value() == null ? null : record.valueSchema());

            Object recordValue;
            if (record.value() instanceof Struct) {
                recordValue = StructToJsonMap.toJsonMap((Struct) record.value());
            } else if (record.value() instanceof Map) {
                recordValue = StructToJsonMap.handleMap((Map<String, Object>) record.value());
            } else {
                recordValue = record.value();
            }

            maybeInsertId(recordValue, record);

            //  Create an updated record with from the current record and the updated record value
            final SinkRecord updatedRecord = new SinkRecord(record.topic(),
                record.kafkaPartition(),
                record.keySchema(),
                record.key(),
                record.valueSchema(),
                recordValue,
                record.kafkaOffset(),
                record.timestamp(),
                record.timestampType(),
                record.headers());

            toBeWrittenRecordList.add(updatedRecord);
        }

        return toBeWrittenRecordList;
    }

    @SuppressWarnings("unchecked")
    private void maybeInsertId(Object recordValue, SinkRecord sinkRecord) {
        if (!(recordValue instanceof Map)) {
            return;
        }
        Map<String, Object> recordMap = (Map<String, Object>) recordValue;
        recordMap.put("id", this.idStrategy.generateId(sinkRecord));
    }

    private IdStrategy createIdStrategy(CosmosSinkTaskConfig sinkTaskConfig) {
        IdStrategy idStrategyClass;
        switch (sinkTaskConfig.getIdStrategy()) {
            case FULL_KEY_STRATEGY:
                idStrategyClass = new FullKeyStrategy();
                break;
            case TEMPLATE_STRATEGY:
                idStrategyClass = new TemplateStrategy();
                break;
            case KAFKA_METADATA_STRATEGY:
                idStrategyClass = new KafkaMetadataStrategy();
                break;
            case PROVIDED_IN_VALUE_STRATEGY:
                idStrategyClass = new ProvidedInValueStrategy();
                break;
            case PROVIDED_IN_KEY_STRATEGY:
                idStrategyClass = new ProvidedInKeyStrategy();
                break;
            default:
                throw new IllegalArgumentException(sinkTaskConfig.getIdStrategy() + " is not supported");
        }

        idStrategyClass.configure(sinkTaskConfig.originalsWithPrefix(AbstractIdStrategyConfig.PREFIX));
        return idStrategyClass;
    }
}
