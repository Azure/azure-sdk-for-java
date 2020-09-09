// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import com.azure.core.util.logging.ClientLogger;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericContainer;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Avro schema functionality.
 */
class AvroSchemaUtils {
    private static final Map<String, Schema> PRIMITIVE_SCHEMAS;

    static {
        Schema.Parser parser = new Schema.Parser();
        PRIMITIVE_SCHEMAS = new HashMap<>();
        PRIMITIVE_SCHEMAS.put("Null", createPrimitiveSchema(parser, "null"));
        PRIMITIVE_SCHEMAS.put("Boolean", createPrimitiveSchema(parser, "boolean"));
        PRIMITIVE_SCHEMAS.put("Integer", createPrimitiveSchema(parser, "int"));
        PRIMITIVE_SCHEMAS.put("Long", createPrimitiveSchema(parser, "long"));
        PRIMITIVE_SCHEMAS.put("Float", createPrimitiveSchema(parser, "float"));
        PRIMITIVE_SCHEMAS.put("Double", createPrimitiveSchema(parser, "double"));
        PRIMITIVE_SCHEMAS.put("String", createPrimitiveSchema(parser, "string"));
        PRIMITIVE_SCHEMAS.put("Bytes", createPrimitiveSchema(parser, "bytes"));
    }

    /**
     * Generates Avro Schema object for the specified primitive type.
     * @param parser Avro schema parser
     * @param type primitive schema type
     * @return Avro Schema object for corresponding primitive type
     */
    private static Schema createPrimitiveSchema(Schema.Parser parser, String type) {
        String schemaString = String.format("{\"type\" : \"%s\"}", type);
        return parser.parse(schemaString);
    }

    /**
     * Maintains map of primitive schemas.
     * @return Map containing string representation of primitive type to corresponding Avro primitive schema
     */
    public static Map<String, Schema> getPrimitiveSchemas() {
        return Collections.unmodifiableMap(PRIMITIVE_SCHEMAS);
    }

    /**
     * Returns Avro schema for specified object, including null values
     *
     * @param object object for which Avro schema is being returned
     * @return Avro schema for object's data structure
     *
     * @throws IllegalArgumentException if object type is unsupported
     */
    public static Schema getSchema(Object object) throws IllegalArgumentException {
        if (object == null) {
            return PRIMITIVE_SCHEMAS.get("Null");
        } else if (object instanceof Boolean) {
            return PRIMITIVE_SCHEMAS.get("Boolean");
        } else if (object instanceof Integer) {
            return PRIMITIVE_SCHEMAS.get("Integer");
        } else if (object instanceof Long) {
            return PRIMITIVE_SCHEMAS.get("Long");
        } else if (object instanceof Float) {
            return PRIMITIVE_SCHEMAS.get("Float");
        } else if (object instanceof Double) {
            return PRIMITIVE_SCHEMAS.get("Double");
        } else if (object instanceof CharSequence) {
            return PRIMITIVE_SCHEMAS.get("String");
        } else if (object instanceof byte[] || object instanceof ByteBuffer) {
            return PRIMITIVE_SCHEMAS.get("Bytes");
        } else if (object instanceof GenericContainer) {
            return ((GenericContainer) object).getSchema();
        } else {
            ClientLogger logger = new ClientLogger(AvroSchemaUtils.class);
            throw logger.logExceptionAsError(
                new IllegalArgumentException(
                "Unsupported Avro type. Supported types are null, Boolean, Integer, Long, "
                    + "Float, Double, String, byte[] and IndexedRecord"));
        }
    }
}
