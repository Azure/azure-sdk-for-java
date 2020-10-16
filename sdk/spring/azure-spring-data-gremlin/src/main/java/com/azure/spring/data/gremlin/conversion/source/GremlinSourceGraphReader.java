// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.source;

import com.azure.spring.data.gremlin.annotation.EdgeSet;
import com.azure.spring.data.gremlin.annotation.VertexSet;
import com.azure.spring.data.gremlin.common.Constants;
import com.azure.spring.data.gremlin.common.GremlinUtils;
import com.azure.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import com.azure.spring.data.gremlin.mapping.GremlinPersistentEntity;
import com.azure.spring.data.gremlin.mapping.GremlinPersistentProperty;
import com.azure.spring.data.gremlin.repository.support.GremlinEntityInformation;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GremlinSourceGraphReader<T> extends AbstractGremlinSourceReader<T> {

    @Override
    public T read(@NonNull Class<T> type, @NonNull MappingGremlinConverter converter,
                      @NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceGraph)) {
            throw new GremlinUnexpectedSourceTypeException("Should be instance of GremlinSourceGraph");
        }

        final GremlinSourceGraph<T> graphSource = (GremlinSourceGraph<T>) source;
        final T entity = GremlinUtils.createInstance(type);
        final ConvertingPropertyAccessor<T> accessor = converter.getPropertyAccessor(entity);
        final GremlinPersistentEntity<?> persistentEntity = converter.getPersistentEntity(type);

        for (final Field field : FieldUtils.getAllFields(type)) {
            final PersistentProperty<GremlinPersistentProperty> property = persistentEntity.getPersistentProperty(field
                .getName());
            if (property == null) {
                continue;
            }

            if ((field.getName().equals(Constants.PROPERTY_ID) || field.getAnnotation(Id.class) != null)) {
                accessor.setProperty(property, super.getGremlinSourceId(graphSource));
            } else if (field.isAnnotationPresent(VertexSet.class)) {
                accessor.setProperty(property, readEntitySet(graphSource.getVertexSet(), converter));
            } else if (field.isAnnotationPresent(EdgeSet.class)) {
                accessor.setProperty(property, readEntitySet(graphSource.getEdgeSet(), converter));
            }
        }

        return entity;
    }

    private List<Object> readEntitySet(List<GremlinSource<?>> sources, MappingGremlinConverter converter) {
        final List<Object> domainObjects = new ArrayList<>();

        for (final GremlinSource<?> source : sources) {
            domainObjects.add(doGremlinSourceRead(source, converter));
        }

        return domainObjects;
    }

    @SuppressWarnings("unchecked")
    private <T> T doGremlinSourceRead(GremlinSource<T> source, MappingGremlinConverter converter) {
        try {
            Class<T> domainClass = (Class<T>) Class.forName((String) source.getProperties()
                .get(Constants.GREMLIN_PROPERTY_CLASSNAME));
            source.setIdField(new GremlinEntityInformation<>(domainClass).getIdField());
            return source.doGremlinSourceRead(domainClass, converter);
        } catch (ClassNotFoundException e) {
            throw new GremlinUnexpectedSourceTypeException("No Java class found for source property "
                + Constants.GREMLIN_PROPERTY_CLASSNAME, e);
        }
    }
}
