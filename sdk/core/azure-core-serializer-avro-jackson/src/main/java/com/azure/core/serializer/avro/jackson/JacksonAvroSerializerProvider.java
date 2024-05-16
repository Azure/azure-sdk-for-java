// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

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
public class JacksonAvroSerializerProvider implements AvroSerializerProvider {
    private static final Schema NULL_SCHEMA;
    private static final Schema BYTE_BUFFER_SCHEMA;
    private static final Map<Class<?>, Schema> CLASS_SCHEMA_CACHE;

    private static final ClientLogger LOGGER = new ClientLogger(JacksonAvroSerializerProvider.class);
    private static final String UNSUPPORTED_TYPE_EXCEPTION = "Unsupported Avro type. Supported types are null, "
        + "Boolean, Integer, Long, Float, Double, String, byte[] and IndexedRecord";

    static {
        NULL_SCHEMA = Schema.create(Schema.Type.NULL);
        BYTE_BUFFER_SCHEMA = Schema.create(Schema.Type.BYTES);
        CLASS_SCHEMA_CACHE = new ConcurrentHashMap<>();

        addPrimitiveSchema(Schema.create(Schema.Type.BOOLEAN), boolean.class, Boolean.class);
        addPrimitiveSchema(Schema.create(Schema.Type.INT), int.class, Integer.class);
        addPrimitiveSchema(Schema.create(Schema.Type.LONG), long.class, Long.class);
        addPrimitiveSchema(Schema.create(Schema.Type.FLOAT), float.class, Float.class);
        addPrimitiveSchema(Schema.create(Schema.Type.DOUBLE), double.class, Double.class);
        addPrimitiveSchema(Schema.create(Schema.Type.STRING), CharSequence.class, String.class);
        addPrimitiveSchema(Schema.create(Schema.Type.BYTES), byte[].class);
    }

    @Override
    public AvroSerializer createInstance(String schema) {
        return new JacksonAvroSerializerBuilder()
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
        if (object == null) {
            return NULL_SCHEMA;
        }

        if (ByteBuffer.class.isAssignableFrom(object.getClass())) {
            return BYTE_BUFFER_SCHEMA;
        }

        return CLASS_SCHEMA_CACHE.computeIfAbsent(object.getClass(), k -> {
            if (GenericContainer.class.isAssignableFrom(k)) {
                return ((GenericContainer) object).getSchema();
            }
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(UNSUPPORTED_TYPE_EXCEPTION));
        });
    }

    private static void addPrimitiveSchema(Schema primitiveSchema, Class<?>... associatedClasses) {
        for (Class<?> associatedClass : associatedClasses) {
            CLASS_SCHEMA_CACHE.put(associatedClass, primitiveSchema);
        }
    }
}
