// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository.support;

import com.azure.spring.data.gremlin.annotation.Edge;
import com.azure.spring.data.gremlin.annotation.GeneratedValue;
import com.azure.spring.data.gremlin.annotation.Graph;
import com.azure.spring.data.gremlin.annotation.Vertex;
import com.azure.spring.data.gremlin.common.GremlinUtils;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceEdge;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceGraph;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceVertex;
import com.azure.spring.data.gremlin.exception.GremlinInvalidEntityIdFieldException;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedEntityTypeException;
import org.springframework.data.repository.core.support.AbstractEntityInformation;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;


public class GremlinEntityInformation<T, ID> extends AbstractEntityInformation<T, ID> {

    private final Field idField;

    public GremlinEntityInformation(@NonNull Class<T> domainClass) {
        super(domainClass);

        this.idField = this.getIdField(domainClass);
    }

    private GremlinSource<T> createGremlinSource(@NonNull Class<T> domainClass, @NonNull Field idField) {
        final String label;
        final String domainClassName = domainClass.getSimpleName();
        final Vertex vertex = domainClass.getAnnotation(Vertex.class);
        final Edge edge = domainClass.getAnnotation(Edge.class);
        final Graph graph = domainClass.getAnnotation(Graph.class);
        final GremlinSource<T> source;

        if (vertex != null && edge == null && graph == null) {
            source = new GremlinSourceVertex<>(domainClass);
            label = vertex.label().isEmpty() ? domainClassName : vertex.label();
        } else if (edge != null && vertex == null && graph == null) {
            source = new GremlinSourceEdge<>(domainClass);
            label = edge.label().isEmpty() ? domainClassName : edge.label();
        } else if (graph != null && vertex == null && edge == null) {
            source = new GremlinSourceGraph<>(domainClass);
            label = "";
        } else {
            throw new GremlinUnexpectedEntityTypeException("Unexpected gremlin entity type");
        }

        source.setLabel(label);
        source.setIdField(idField);

        return source;
    }

    public GremlinSource<T> createGremlinSource() {
        return createGremlinSource(super.getJavaType(), idField);
    }

    @Override
    @Nullable
    public ID getId(T entity) {
        final Field idField = this.idField;
        @SuppressWarnings("unchecked") final ID id = (ID) ReflectionUtils.getField(idField, entity);

        if (id == null && !(super.getJavaType().isAnnotationPresent(Graph.class))
                && !idField.isAnnotationPresent(GeneratedValue.class)) {
            throw new GremlinInvalidEntityIdFieldException("A non-generated id field cannot be null!");
        }
        return id;
    }

    @Override
    public Class<ID> getIdType() {
        @SuppressWarnings("unchecked") final Class<ID> idClass = (Class<ID>) this.idField.getType();

        return idClass;
    }

    @NonNull
    private Field getIdField(@NonNull Class<T> domainClass) {
        final Field idField = GremlinUtils.getIdField(domainClass);

        ReflectionUtils.makeAccessible(idField);

        return idField;
    }

    public Field getIdField() {
        return idField;
    }
}

