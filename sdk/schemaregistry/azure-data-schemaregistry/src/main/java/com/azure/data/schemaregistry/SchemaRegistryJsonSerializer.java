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
 * Serializer for Schema Registry.
 */
class SchemaRegistryJsonSerializer implements SerializerAdapter {
    private final SerializerAdapter adapter = JacksonAdapter.createDefaultSerializerAdapter();

    /**
     * {@inheritDoc}
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
        //TODO (conniey): Test this.
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
