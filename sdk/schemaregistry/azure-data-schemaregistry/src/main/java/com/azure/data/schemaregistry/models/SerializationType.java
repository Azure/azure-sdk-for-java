package com.azure.data.schemaregistry.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Serialization types officially supported by Schema Registry serializers.
 */
public class SerializationType extends ExpandableStringEnum<SerializationType> {

    public static final SerializationType AVRO = fromString("avro");

    /**
     * Creates new SerializationType enum value from String
     *
     * @param name serialization type name
     * @return expanded enum value
     */
    public static SerializationType fromString(String name) {
        return fromString(name, SerializationType.class);
    }
}
