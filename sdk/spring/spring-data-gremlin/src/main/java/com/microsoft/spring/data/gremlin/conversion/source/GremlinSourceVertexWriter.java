// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.conversion.source;

import com.microsoft.spring.data.gremlin.common.Constants;
import com.microsoft.spring.data.gremlin.mapping.GremlinPersistentProperty;
import com.microsoft.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.microsoft.spring.data.gremlin.exception.GremlinEntityInformationException;
import com.microsoft.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import com.microsoft.spring.data.gremlin.mapping.GremlinPersistentEntity;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;

public class GremlinSourceVertexWriter<T> implements GremlinSourceWriter<T> {

    @Override
    @SuppressWarnings("unchecked")
    public void write(@NonNull Object domain, @NonNull MappingGremlinConverter converter,
                      @NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceVertex)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceVertex");
        }

        source.setId(converter.getIdFieldValue(domain));

        final GremlinPersistentEntity<?> persistentEntity = converter.getPersistentEntity(domain.getClass());
        final ConvertingPropertyAccessor<T> accessor = (ConvertingPropertyAccessor<T>) converter.getPropertyAccessor(domain);

        for (final Field field : FieldUtils.getAllFields(domain.getClass())) {
            final PersistentProperty<GremlinPersistentProperty> property = persistentEntity.getPersistentProperty(field.getName());
            if (property == null) {
                continue;
            }

            if (field.getName().equals(Constants.PROPERTY_ID) || field.getAnnotation(Id.class) != null) {
                continue;
            } else if (field.getName().equals(Constants.GREMLIN_PROPERTY_CLASSNAME)) {
                throw new GremlinEntityInformationException("Domain Cannot use pre-defined field name: "
                        + Constants.GREMLIN_PROPERTY_CLASSNAME);
            }

            source.setProperty(field.getName(), accessor.getProperty(property));
        }
    }
}

