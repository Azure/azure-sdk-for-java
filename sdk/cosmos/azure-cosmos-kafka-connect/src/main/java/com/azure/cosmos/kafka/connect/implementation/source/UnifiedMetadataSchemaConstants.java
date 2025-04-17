package com.azure.cosmos.kafka.connect.implementation.source;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

final class UnifiedMetadataSchemaConstants {
    public static final String SCHEMA_NAME = "UnifiedMetadataSchema";
    public static final String ENTITY_TYPE_NAME = "entityType";
    public static final String JSON_VALUE_NAME = "jsonValue";

    public static final Schema SCHEMA = SchemaBuilder
        .struct()
        .name(SCHEMA_NAME)
        .field(ENTITY_TYPE_NAME, Schema.STRING_SCHEMA)
        .field(JSON_VALUE_NAME, Schema.STRING_SCHEMA)
        .build();
}
