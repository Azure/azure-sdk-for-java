// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.source;

import com.azure.spring.data.gremlin.annotation.EdgeFrom;
import com.azure.spring.data.gremlin.annotation.EdgeTo;
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

public class GremlinSourceEdgeReader<T> extends AbstractGremlinSourceReader<T> {

    @Override
    public T read(@NonNull Class<T> domainClass, @NonNull MappingGremlinConverter converter,
                      @NonNull GremlinSource<T> source) {
        Assert.notNull(domainClass, "domainClass should not be null");
        Assert.notNull(converter, "converter should not be null");
        Assert.notNull(source, "source should not be null");
        if (!(source instanceof GremlinSourceEdge)) {
            throw new GremlinUnexpectedSourceTypeException("should be instance of GremlinSourceEdge");
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
                continue;
            } else if (field.getAnnotation(EdgeFrom.class) != null || field.getAnnotation(EdgeTo.class) != null) {
                // We cannot do that here as the gremlin will not tell more information about vertex except Id. After
                // the query of Edge end, we can get the Id of vertex from/to. And then we will do extra 2 query to
                // obtain the 2 vertex and complete the edge.
                //
                // That work will be wrapped in GremlinTemplate insert, and skip the property here.
                continue;
            }

            final Object value = super.readProperty(property, source.getProperties().get(field.getName()));
            accessor.setProperty(property, value);
        }

        return domain;
    }
}
