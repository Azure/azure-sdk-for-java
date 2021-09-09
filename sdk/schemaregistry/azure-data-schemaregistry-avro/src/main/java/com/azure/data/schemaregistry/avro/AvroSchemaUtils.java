// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericContainer;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Avro schema functionality.
 */
class AvroSchemaUtils {
    private static final Map<Type, Schema> PRIMITIVE_SCHEMAS;

    static {
        final HashMap<Type, Schema> schemas = new HashMap<>();
        schemas.put(Type.NULL, Schema.create(Type.NULL));
        schemas.put(Type.BOOLEAN, Schema.create(Type.BOOLEAN));
        schemas.put(Type.INT, Schema.create(Type.INT));
        schemas.put(Type.LONG, Schema.create(Type.LONG));
        schemas.put(Type.FLOAT, Schema.create(Type.FLOAT));
        schemas.put(Type.DOUBLE, Schema.create(Type.DOUBLE));
        schemas.put(Type.BYTES, Schema.create(Type.BYTES));
        schemas.put(Type.STRING, Schema.create(Type.STRING));

        PRIMITIVE_SCHEMAS = Collections.unmodifiableMap(schemas);
    }

    /**
     * Maintains map of primitive schemas.
     *
     * @return Map containing string representation of primitive type to corresponding Avro primitive schema
     */
    public static Map<Type, Schema> getPrimitiveSchemas() {
        return PRIMITIVE_SCHEMAS;
    }

    /**
     * Returns Avro schema for specified object, including null values
     *
     * @param object object for which Avro schema is being returned
     *
     * @return Avro schema for object's data structure
     *
     * @throws IllegalArgumentException if object type is unsupported
     */
    public static Schema getSchema(Object object) throws IllegalArgumentException {
        if (object == null) {
            return PRIMITIVE_SCHEMAS.get(Type.NULL);
        } else if (object instanceof Boolean) {
            return PRIMITIVE_SCHEMAS.get(Type.BOOLEAN);
        } else if (object instanceof Integer) {
            return PRIMITIVE_SCHEMAS.get(Type.INT);
        } else if (object instanceof Long) {
            return PRIMITIVE_SCHEMAS.get(Type.LONG);
        } else if (object instanceof Float) {
            return PRIMITIVE_SCHEMAS.get(Type.FLOAT);
        } else if (object instanceof Double) {
            return PRIMITIVE_SCHEMAS.get(Type.DOUBLE);
        } else if (object instanceof CharSequence) {
            return PRIMITIVE_SCHEMAS.get(Type.STRING);
        } else if (object instanceof byte[] || object instanceof ByteBuffer) {
            return PRIMITIVE_SCHEMAS.get(Type.BYTES);
        } else if (object instanceof GenericContainer) {
            return ((GenericContainer) object).getSchema();
        } else {
            throw new IllegalArgumentException("Unsupported Avro type. Supported types are null, Boolean, Integer,"
                    + " Long, Float, Double, String, byte[], and GenericContainer");
        }
    }
}
