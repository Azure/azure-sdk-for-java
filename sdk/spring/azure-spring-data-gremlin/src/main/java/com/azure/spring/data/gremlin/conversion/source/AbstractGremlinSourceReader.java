// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.source;

import com.azure.spring.data.gremlin.common.GremlinUtils;
import com.azure.spring.data.gremlin.exception.GremlinEntityInformationException;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedEntityTypeException;
import com.azure.spring.data.gremlin.mapping.GremlinPersistentProperty;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import org.apache.tinkerpop.shaded.jackson.databind.JavaType;
import org.apache.tinkerpop.shaded.jackson.databind.type.TypeFactory;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public abstract class AbstractGremlinSourceReader<T> implements GremlinSourceReader<T> {

    /**
     * Convert the value to the equivalent Java object.
     *
     * @param persistentProperty The gremlin persistent property.
     * @param value The property value.
     * @return The Java object representation of the value.
     * @throws GremlinUnexpectedEntityTypeException If the value could not be parsed to its type.
     */
    protected Object readProperty(@NonNull PersistentProperty<GremlinPersistentProperty> persistentProperty,
                                  @Nullable Object value) {
        Assert.notNull(persistentProperty, "property should not be null");
        final Class<?> type = persistentProperty.getTypeInformation().getType();
        final JavaType javaType = TypeFactory.defaultInstance().constructType(persistentProperty.getType());

        if (value == null) {
            return null;
        } else if (type == int.class || type == Integer.class
            || type == Boolean.class || type == boolean.class
            || type == String.class) {
            return value;
        } else if (type == Date.class) {
            Assert.isTrue(value instanceof Long, "Date store value must be instance of long");
            return new Date((Long) value);
        } else {
            final Object object;

            try {
                object = GremlinUtils.getObjectMapper().readValue(value.toString(), javaType);
            } catch (IOException e) {
                throw new GremlinUnexpectedEntityTypeException("Failed to read String to Object", e);
            }

            return object;
        }
    }

    /**
     * Get id from the {@link GremlinSource}.
     *
     * @param source The gremlin source object.
     * @return The id object.
     * @throws GremlinEntityInformationException If the id type is not supported.
     */
    protected Object getGremlinSourceId(@NonNull GremlinSource<T> source) {
        Assert.notNull(source, "source should not be null");
        if (!source.getId().isPresent()) {
            return null;
        }

        final Object id = source.getId().get();
        final Field idField = source.getIdField();

        if (idField.getType() == String.class) {
            return id.toString();
        } else if (idField.getType() == Integer.class) {
            Assert.isTrue(id instanceof Integer, "source Id should be Integer.");
            return id;
        } else if (idField.getType() == Long.class && id instanceof Integer) {
            return Long.valueOf((Integer) id);
        } else if (idField.getType() == Long.class && id instanceof Long) {
            return id;
        }

        throw new GremlinEntityInformationException("unsupported id field type: " + id.getClass().getSimpleName());
    }
}

