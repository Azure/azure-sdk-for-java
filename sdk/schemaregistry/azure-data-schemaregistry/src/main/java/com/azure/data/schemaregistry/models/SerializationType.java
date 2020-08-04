package com.azure.data.schemaregistry.models;

import com.azure.core.util.ExpandableStringEnum;

public class SerializationType extends ExpandableStringEnum<SerializationType> {

    public static final SerializationType AVRO = fromString("avro");
    public static final SerializationType JSON = fromString("json");

    public static SerializationType fromString(String name) {
        return fromString(name, SerializationType.class);
    }
}
