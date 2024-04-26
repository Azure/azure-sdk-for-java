// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.idStrategy;

import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.FullKeyStrategy;
import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.IdStrategy;
import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.KafkaMetadataStrategy;
import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.KafkaMetadataStrategyConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.TemplateStrategy;
import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.TemplateStrategyConfig;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TemplateStrategyTest {

    @Test(groups = { "unit" })
    public void simpleKey() {
        IdStrategy strategy = new TemplateStrategy();
        Map<String, String> map = new HashMap<>();
        map.put(TemplateStrategyConfig.TEMPLATE_CONFIG, "${key}");
        strategy.configure(map);
        SinkRecord record = mock(SinkRecord.class);
        when(record.keySchema()).thenReturn(Schema.STRING_SCHEMA);
        when(record.key()).thenReturn("test");

        String id = strategy.generateId(record);
        assertEquals("test", id);
    }

    @Test(groups = { "unit" })
    public void kafkaMetadata() {
        IdStrategy strategy = new TemplateStrategy();
        Map<String, String> map = new HashMap<>();
        map.put(TemplateStrategyConfig.TEMPLATE_CONFIG, "${topic}-${partition}-${offset}");
        strategy.configure(map);
        SinkRecord record = mock(SinkRecord.class);
        when(record.topic()).thenReturn("mytopic");
        when(record.kafkaPartition()).thenReturn(0);
        when(record.kafkaOffset()).thenReturn(1L);

        String id = strategy.generateId(record);
        assertEquals("mytopic-0-1", id);
    }

    @Test(groups = { "unit" })
    public void unknownVariablePreserved() {
        IdStrategy strategy = new TemplateStrategy();
        Map<String, String> map = new HashMap<>();
        map.put(TemplateStrategyConfig.TEMPLATE_CONFIG, "${unknown}");

        strategy.configure(map);
        String id = strategy.generateId(mock(SinkRecord.class));
        assertEquals("${unknown}", id);
    }

    @Test(groups = { "unit" })
    public void nestedStruct() {
        IdStrategy strategy = new TemplateStrategy();
        Map<String, String> map = new HashMap<>();
        map.put(TemplateStrategyConfig.TEMPLATE_CONFIG, "${key}");

        strategy.configure(map);
        SinkRecord record = mock(SinkRecord.class);
        Schema nestedSchema = SchemaBuilder.struct()
            .field("nested_field", Schema.STRING_SCHEMA)
            .build();
        Schema schema = SchemaBuilder.struct()
            .field("string_field", Schema.STRING_SCHEMA)
            .field("struct_field", nestedSchema)
            .build();
        Struct value = new Struct(schema)
            .put("string_field", "value")
            .put("struct_field",
                new Struct(nestedSchema).put("nested_field", "a nest"));
        when(record.keySchema()).thenReturn(schema);
        when(record.key()).thenReturn(value);

        String id = strategy.generateId(record);
        assertEquals(
            "{\"string_field\":\"value\",\"struct_field\":{\"nested_field\":\"a nest\"}}",
            id);
    }

    @Test(groups = { "unit" })
    public void fullKeyStrategyUsesFullKey() {
        IdStrategy strategy = new TemplateStrategy();
        strategy = new FullKeyStrategy();
        strategy.configure(new HashMap<>());
        SinkRecord record = mock(SinkRecord.class);
        Schema schema = SchemaBuilder.struct()
            .field("string_field", Schema.STRING_SCHEMA)
            .field("int64_field", Schema.INT64_SCHEMA)
            .build();
        Struct value = new Struct(schema)
            .put("string_field", "value")
            .put("int64_field", 0L);

        when(record.keySchema()).thenReturn(schema);
        when(record.key()).thenReturn(value);

        String id = strategy.generateId(record);
        assertEquals(
            "{\"string_field\":\"value\",\"int64_field\":0}",
            id);
    }

    @Test(groups = { "unit" })
    public void metadataStrategyUsesMetadataWithDeliminator() {
        IdStrategy strategy = new TemplateStrategy();
        Map<String, String> map = new HashMap<>();
        map.put(KafkaMetadataStrategyConfig.DELIMITER_CONFIG, "_");

        strategy = new KafkaMetadataStrategy();
        strategy.configure(map);
        SinkRecord record = mock(SinkRecord.class);
        when(record.topic()).thenReturn("topic");
        when(record.kafkaPartition()).thenReturn(0);
        when(record.kafkaOffset()).thenReturn(1L);

        String id = strategy.generateId(record);
        assertEquals("topic_0_1", id);
    }

    @Test(groups = { "unit" })
    public void generatedIdSanitized() {
        IdStrategy strategy = new TemplateStrategy();
        Map<String, String> map = new HashMap<>();
        map.put(TemplateStrategyConfig.TEMPLATE_CONFIG, "#my/special\\id?");

        strategy = new TemplateStrategy();
        strategy.configure(map);
        SinkRecord record = mock(SinkRecord.class);

        String id = strategy.generateId(record);
        assertEquals("_my_special_id_", id);
    }
}
