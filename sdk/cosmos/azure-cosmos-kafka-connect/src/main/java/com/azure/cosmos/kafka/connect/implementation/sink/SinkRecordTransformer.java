package com.azure.cosmos.kafka.connect.implementation.sink;

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

    private final IdStrategies idStrategies;

    public SinkRecordTransformer(IdStrategies idStrategies) {
        this.idStrategies = idStrategies;
    }

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
                //  TODO: Do we need to update the value schema to map or keep it struct?
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

    private void maybeInsertId(Object recordValue, SinkRecord sinkRecord) {
        if (!(recordValue instanceof Map)) {
            return;
        }
        Map<String, Object> recordMap = (Map<String, Object>) recordValue;
        IdStrategy idStrategy = config.idStrategy();
        recordMap.put(AbstractIdStrategyConfig.ID, idStrategy.generateId(sinkRecord));
    }
}
