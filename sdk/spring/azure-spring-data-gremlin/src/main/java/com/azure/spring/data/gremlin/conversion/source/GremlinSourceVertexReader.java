// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.source;

import com.azure.spring.data.gremlin.common.Constants;
import com.azure.spring.data.gremlin.common.GremlinUtils;
import com.azure.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import com.azure.spring.data.gremlin.mapping.GremlinPersistentEntity;
import com.azure.spring.data.gremlin.mapping.GremlinPersistentProperty;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;

public class GremlinSourceVertexReader<T> extends AbstractGremlinSourceReader<T> {

    @Override
    public T read(@NonNull Class<T> domainClass, @NonNull MappingGremlinConverter converter,
                      @NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be instance of GremlinSourceVertex");
        }

        final T domain = GremlinUtils.createInstance(domainClass);
        final ConvertingPropertyAccessor<T> accessor = converter.getPropertyAccessor(domain);
        final GremlinPersistentEntity<?> persistentEntity = converter.getPersistentEntity(domainClass);

        for (final Field field : FieldUtils.getAllFields(domainClass)) {
            final PersistentProperty<GremlinPersistentProperty> property = persistentEntity.getPersistentProperty(field
                .getName());
            if (property == null) {
                continue;
            }

            if (field.getName().equals(Constants.PROPERTY_ID) || field.getAnnotation(Id.class) != null) {
                accessor.setProperty(property, super.getGremlinSourceId(source));
            } else {
                final Object value = super.readProperty(property, source.getProperties().get(field.getName()));
                accessor.setProperty(property, value);
            }
        }

        return domain;
    }
}

