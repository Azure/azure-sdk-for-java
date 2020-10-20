// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.source;

import com.azure.spring.data.gremlin.conversion.result.GremlinResultEdgeReader;
import com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralEdge;

public class GremlinSourceEdge<T> extends AbstractGremlinSource<T> {

    private Object vertexIdFrom;

    private Object vertexIdTo;

    public GremlinSourceEdge() {
        super();
        initializeGremlinStrategy();
    }

    public GremlinSourceEdge(Class<T> domainClass) {
        super(domainClass);
        initializeGremlinStrategy();
    }

    private void initializeGremlinStrategy() {
        this.setGremlinScriptStrategy(new GremlinScriptLiteralEdge());
        this.setGremlinResultReader(new GremlinResultEdgeReader());
        this.setGremlinSourceReader(new GremlinSourceEdgeReader<>());
        this.setGremlinSourceWriter(new GremlinSourceEdgeWriter<>());
    }

    public Object getVertexIdFrom() {
        return vertexIdFrom;
    }

    public void setVertexIdFrom(Object vertexIdFrom) {
        this.vertexIdFrom = vertexIdFrom;
    }

    public Object getVertexIdTo() {
        return vertexIdTo;
    }

    public void setVertexIdTo(Object vertexIdTo) {
        this.vertexIdTo = vertexIdTo;
    }
}
