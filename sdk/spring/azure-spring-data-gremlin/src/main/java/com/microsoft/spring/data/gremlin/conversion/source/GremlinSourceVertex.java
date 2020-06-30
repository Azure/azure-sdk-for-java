// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.conversion.source;

import com.microsoft.spring.data.gremlin.conversion.result.GremlinResultVertexReader;
import com.microsoft.spring.data.gremlin.conversion.script.GremlinScriptLiteralVertex;

public class GremlinSourceVertex<T> extends AbstractGremlinSource<T> {

    public GremlinSourceVertex() {
        super();
        initializeGremlinStrategy();
    }

    public GremlinSourceVertex(Class<T> domainClass) {
        super(domainClass);
        initializeGremlinStrategy();
    }

    private void initializeGremlinStrategy() {
        this.setGremlinScriptStrategy(new GremlinScriptLiteralVertex());
        this.setGremlinResultReader(new GremlinResultVertexReader());
        this.setGremlinSourceReader(new GremlinSourceVertexReader<>());
        this.setGremlinSourceWriter(new GremlinSourceVertexWriter<>());
    }
}
