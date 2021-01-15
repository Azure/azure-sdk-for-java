// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query;

import com.azure.spring.data.gremlin.common.GremlinEntityType;
import com.azure.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.query.query.GremlinQuery;

import java.util.List;

/**
 * Provider interface for basic Operations with Gremlin
 */
public interface GremlinOperations {

    void deleteAll();

    void deleteAll(GremlinEntityType type);

    <T> void deleteAll(GremlinSource<T> source);

    <T> boolean isEmptyGraph(GremlinSource<T> source);

    <T> boolean existsById(Object id, GremlinSource<T> source);

    <T> void deleteById(Object id, GremlinSource<T> source);

    <T> T insert(T object, GremlinSource<T> source);

    <T> T findById(Object id, GremlinSource<T> source);

    <T> T findVertexById(Object id, GremlinSource<T> source);

    <T> T findEdgeById(Object id, GremlinSource<T> source);

    <T> T update(T object, GremlinSource<T> source);

    <T> T save(T object, GremlinSource<T> source);

    <T> List<T> findAll(GremlinSource<T> source);

    long vertexCount();

    long edgeCount();

    <T> List<T> find(GremlinQuery query, GremlinSource<T> source);

    MappingGremlinConverter getMappingConverter();
}
