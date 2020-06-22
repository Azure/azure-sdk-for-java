// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.conversion.source;

import com.microsoft.spring.data.gremlin.conversion.result.GremlinResultsGraphReader;
import com.microsoft.spring.data.gremlin.conversion.result.GremlinResultsReader;
import com.microsoft.spring.data.gremlin.conversion.script.GremlinScriptLiteralGraph;
import com.microsoft.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;

import java.util.ArrayList;
import java.util.List;

public class GremlinSourceGraph<T> extends AbstractGremlinSource<T> {

    private List<GremlinSource<?>> vertexSet = new ArrayList<>();

    private List<GremlinSource<?>> edgeSet = new ArrayList<>();

    private GremlinResultsReader resultsReader;

    public GremlinSourceGraph() {
        super();
        initializeGremlinStrategy();
        this.setGremlinSourceReader(new GremlinSourceGraphReader<>());
        this.resultsReader = new GremlinResultsGraphReader();
    }

    public GremlinSourceGraph(Class<T> domainClass) {
        super(domainClass);
        initializeGremlinStrategy();
        this.setGremlinSourceReader(new GremlinSourceGraphReader<T>());
        this.resultsReader = new GremlinResultsGraphReader();
    }

    public void addGremlinSource(GremlinSource<?> source) {
        if (source instanceof GremlinSourceVertex) {
            this.vertexSet.add(source);
        } else if (source instanceof GremlinSourceEdge) {
            this.edgeSet.add(source);
        } else {
            throw new GremlinUnexpectedSourceTypeException("source type can only be Vertex or Edge");
        }
    }

    private void initializeGremlinStrategy() {
        this.setGremlinScriptStrategy(new GremlinScriptLiteralGraph());
        this.setGremlinSourceWriter(new GremlinSourceGraphWriter<>());
    }

    public List<GremlinSource<?>> getVertexSet() {
        return vertexSet;
    }

    public List<GremlinSource<?>> getEdgeSet() {
        return edgeSet;
    }

    public GremlinResultsReader getResultsReader() {
        return resultsReader;
    }
}

