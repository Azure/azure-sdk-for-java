// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.result;

import com.azure.spring.data.gremlin.common.Constants;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceEdge;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceGraph;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceVertex;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedEntityTypeException;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public class GremlinResultsGraphReader extends AbstractGremlinResultReader {

    private final GremlinResultVertexReader vertexResultReader;
    private final GremlinResultEdgeReader edgeResultReader;

    public GremlinResultsGraphReader() {
        vertexResultReader = new GremlinResultVertexReader();
        edgeResultReader = new GremlinResultEdgeReader();
    }

    @Override
    public <T> void read(@NonNull List<Result> results, @NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceGraph)) {
            throw new GremlinUnexpectedSourceTypeException("Should be instance of GremlinSourceGraph");
        }

        final GremlinSourceGraph<T> graphSource = (GremlinSourceGraph<T>) source;

        graphSource.getVertexSet().clear();
        graphSource.getEdgeSet().clear();

        results.stream().map(this::processResult).forEach(graphSource::addGremlinSource);
    }

    private GremlinSource<?> processResult(Result result) {
        final GremlinSource<?> source;
        final Object obj = result.getObject();

        Assert.isInstanceOf(Map.class, obj, "should be an instance of Map");
        @SuppressWarnings("unchecked") final Map<String, Object> map = (Map<String, Object>) result.getObject();

        Assert.isTrue(map.containsKey(Constants.PROPERTY_TYPE), "should contain a type property");
        final String type = (String) map.get(Constants.PROPERTY_TYPE);

        switch (type) {
            case Constants.RESULT_TYPE_VERTEX:
                source = new GremlinSourceVertex<>();
                vertexResultReader.read(singletonList(result), source);
                break;
            case Constants.RESULT_TYPE_EDGE:
                source = new GremlinSourceEdge<>();
                edgeResultReader.read(singletonList(result), source);
                break;
            default:
                throw new GremlinUnexpectedEntityTypeException("Unexpected result type: " + type);
        }

        return source;
    }
}
