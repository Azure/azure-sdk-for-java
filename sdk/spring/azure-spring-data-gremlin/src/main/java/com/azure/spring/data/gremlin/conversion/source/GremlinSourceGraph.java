// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.source;

import com.azure.spring.data.gremlin.conversion.result.GremlinResultsGraphReader;
import com.azure.spring.data.gremlin.conversion.result.GremlinResultsReader;
import com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralGraph;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;

import java.util.ArrayList;
import java.util.List;

public class GremlinSourceGraph<T> extends AbstractGremlinSource<T> {

    private final List<GremlinSource<?>> vertexSet = new ArrayList<>();

    private final List<GremlinSource<?>> edgeSet = new ArrayList<>();

    private final GremlinResultsReader resultsReader;

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

    /**
     * Add gremlin source to the graph.
     * @param source A gremlin source, could be vertex or edge.
     * @throws GremlinUnexpectedSourceTypeException If source type is not {@link GremlinSourceVertex} or
     *         {@link GremlinSourceEdge}.
     */
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

