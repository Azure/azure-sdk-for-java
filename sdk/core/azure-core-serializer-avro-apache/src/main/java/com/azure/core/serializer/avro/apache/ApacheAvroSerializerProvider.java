// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.apache;

import com.azure.core.experimental.serializer.AvroSerializer;
import com.azure.core.experimental.serializer.AvroSerializerProvider;
import com.azure.core.util.logging.ClientLogger;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericContainer;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link AvroSerializerProvider}.
 */
public class ApacheAvroSerializerProvider implements AvroSerializerProvider {
    private static final Map<Class<?>, Schema> CLASS_SCHEMA_CACHE;

    private static final ClientLogger LOGGER = new ClientLogger(ApacheAvroSerializerProvider.class);
    private static final String UNSUPPORTED_TYPE_EXCEPTION = "Unsupported Avro type. Supported types are null, "
        + "Boolean, Integer, Long, Float, Double, String, byte[] and IndexedRecord";

    static {
        CLASS_SCHEMA_CACHE = new ConcurrentHashMap<>();

        Schema.Parser parser = new Schema.Parser();
        addPrimitiveSchema("null", parser, (Class<?>) null);
        addPrimitiveSchema("boolean", parser, boolean.class, Boolean.class);
        addPrimitiveSchema("int", parser, int.class, Integer.class);
        addPrimitiveSchema("long", parser, long.class, Long.class);
        addPrimitiveSchema("float", parser, float.class, Float.class);
        addPrimitiveSchema("double", parser, double.class, Double.class);
        addPrimitiveSchema("string", parser, CharSequence.class, String.class);
        addPrimitiveSchema("bytes", parser, byte[].class, ByteBuffer.class);
    }

    @Override
    public AvroSerializer createInstance(String schema) {
        return new ApacheAvroSerializerBuilder()
            .schema(schema)
            .build();
    }

    @Override
    public String getSchema(Object object) {
        return computeOrGet(object).toString();
    }

    @Override
    public String getSchemaName(Object object) {
        return computeOrGet(object).getFullName();
    }

    private static Schema computeOrGet(Object object) {
        Class<?> key = (object == null) ? null : object.getClass();

        return CLASS_SCHEMA_CACHE.computeIfAbsent(key, k -> {
            if (GenericContainer.class.isAssignableFrom(k)) {
                return ((GenericContainer) object).getSchema();
            }
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(UNSUPPORTED_TYPE_EXCEPTION));
        });
    }

    private static void addPrimitiveSchema(String type, Schema.Parser parser, Class<?>... associatedClasses) {
        Schema schema = parser.parse(String.format("{\"type\" : \"%s\"}", type));

        for (Class<?> associatedClass : associatedClasses) {
            CLASS_SCHEMA_CACHE.put(associatedClass, schema);
        }
    }
}
