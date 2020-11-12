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
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.List;

public class GremlinSourceGraphWriter<T> implements GremlinSourceWriter<T> {

    private void writeGraphSet(@NonNull List<Object> objectList, @NonNull MappingGremlinConverter mappingConverter,
                               @NonNull GremlinSourceGraph<T> sourceGraph) {
        Assert.notNull(objectList, "objectList should not be null");
        Assert.notNull(mappingConverter, "mappingConverter should not be null");
        Assert.notNull(sourceGraph, "sourceGraph should not be null");
        Assert.isInstanceOf(GremlinSourceGraph.class, sourceGraph, "should be instance of GremlinSourceGraph ");

        for (final Object object : objectList) {
            final GremlinSource<?> source = GremlinUtils.toGremlinSource(object.getClass());
            source.doGremlinSourceWrite(object, mappingConverter);
            sourceGraph.addGremlinSource(source);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(@NonNull Object domain, @NonNull MappingGremlinConverter converter,
                      @NonNull GremlinSource<T> source) {
        Assert.notNull(domain, "domain should not be null");
        Assert.notNull(converter, "converter should not be null");
        Assert.notNull(source, "source should not be null");
        if (!(source instanceof GremlinSourceGraph)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceEdge");
        }

        final GremlinSourceGraph<T> sourceGraph = (GremlinSourceGraph<T>) source;
        final GremlinPersistentEntity<?> persistentEntity = converter.getPersistentEntity(domain.getClass());
        final ConvertingPropertyAccessor<T> accessor = converter.getPropertyAccessor((T) domain);

        for (final Field field : FieldUtils.getAllFields(domain.getClass())) {
            final PersistentProperty<GremlinPersistentProperty> property = persistentEntity.getPersistentProperty(field
                .getName());
            if (property == null) {
                continue;
            }

            if (field.getName().equals(Constants.PROPERTY_ID) || field.getAnnotation(Id.class) != null) {
                continue;
            }

            @SuppressWarnings("unchecked") final List<Object> objects = (List<Object>) accessor.getProperty(property);

            if (field.getAnnotation(VertexSet.class) != null || field.getAnnotation(EdgeSet.class) != null) {
                this.writeGraphSet(objects, converter, sourceGraph);
            }
        }
    }
}

