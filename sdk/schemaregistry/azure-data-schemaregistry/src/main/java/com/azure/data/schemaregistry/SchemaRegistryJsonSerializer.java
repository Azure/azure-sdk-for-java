// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.serializer.CollectionFormat;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Package-private serializer for Schema Registry.
 * Overrides {@link #serialize(Object, SerializerEncoding)} behaviour because the default serializer tries to serialise
 * an already serialised JSON object.
 */
class SchemaRegistryJsonSerializer implements SerializerAdapter {
    private final SerializerAdapter adapter = JacksonAdapter.createDefaultSerializerAdapter();

    /**
     * Serializes an object. If {@code encoding} is JSON and the {@code object} is a string, it passed through as-is.
     * Otherwise, the object is serialized through the default serializer.  The reason is that Schema Registry schemas
     * are JSON strings, we don't want to double serialize them, resulting in an incorrect schema.
     */
    @Override
    public String serialize(Object object, SerializerEncoding encoding) throws IOException {
        if (encoding != SerializerEncoding.JSON) {
            return adapter.serialize(object, encoding);
        }

        if (object instanceof String) {
            return (String) object;
        } else {
            return adapter.serialize(object, encoding);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String serializeRaw(Object object) {
        return adapter.serializeRaw(object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String serializeList(List<?> list, CollectionFormat format) {
        return adapter.serializeList(list, format);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T deserialize(String value, Type type, SerializerEncoding encoding) throws IOException {
        return adapter.deserialize(value, type, encoding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T deserialize(HttpHeaders headers, Type type) throws IOException {
        return adapter.deserialize(headers, type);
    }
}
