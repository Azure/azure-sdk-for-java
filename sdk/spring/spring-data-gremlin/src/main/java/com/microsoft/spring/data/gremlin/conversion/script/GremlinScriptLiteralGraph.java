// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.conversion.script;

import com.microsoft.spring.data.gremlin.common.Constants;
import com.microsoft.spring.data.gremlin.conversion.source.GremlinSource;
import com.microsoft.spring.data.gremlin.conversion.source.GremlinSourceGraph;
import com.microsoft.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class GremlinScriptLiteralGraph implements GremlinScriptLiteral {

    private final GremlinScriptLiteralVertex scriptVertex = new GremlinScriptLiteralVertex();

    private final GremlinScriptLiteralEdge scriptEdge = new GremlinScriptLiteralEdge();

    @Override
    public <T> List<String> generateInsertScript(@NonNull GremlinSource<T> source) {
        return generateInsertUpdateScript(source,
                scriptVertex::generateInsertScript,
                scriptEdge::generateInsertScript);
    }

    @Override
    public List<String> generateDeleteAllScript() {
        return Arrays.asList(Constants.GREMLIN_SCRIPT_EDGE_DROP_ALL, Constants.GREMLIN_QUERY_BARRIER, Constants.GREMLIN_SCRIPT_VERTEX_DROP_ALL);
    }

    @Override
    public <T> List<String> generateDeleteAllByClassScript(@NonNull GremlinSource<T> source) {
        return generateDeleteAllScript();
    }

    @Override
    public <T> List<String> generateFindByIdScript(@Nullable GremlinSource<T> source) {
        throw new UnsupportedOperationException("Gremlin graph cannot findById by single query.");
    }

    @Override
    public <T> List<String> generateUpdateScript(@NonNull GremlinSource<T> source) {
        return generateInsertUpdateScript(source,
                scriptVertex::generateUpdateScript,
                scriptEdge::generateUpdateScript);
    }

    private <T> List<String> generateInsertUpdateScript(@NonNull GremlinSource<T> source,
                                                        @NonNull Function<GremlinSource<?>, List<String>> vertexHandler,
                                                        @NonNull Function<GremlinSource<?>, List<String>> edgeHandler) {
        if (!(source instanceof GremlinSourceGraph)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceGraph");
        }

        final List<String> scriptList = new ArrayList<>();
        final GremlinSourceGraph<T> sourceGraph = (GremlinSourceGraph<T>) source;
        final List<GremlinSource<?>> vertexes = sourceGraph.getVertexSet();
        final List<GremlinSource<?>> edges = sourceGraph.getEdgeSet();

        vertexes.forEach(vertex -> scriptList.addAll(vertexHandler.apply(vertex)));
        scriptList.add(Constants.GREMLIN_QUERY_BARRIER);
        edges.forEach(edge -> scriptList.addAll(edgeHandler.apply(edge)));

        return scriptList;
    }

    @Override
    public <T> List<String> generateDeleteByIdScript(@NonNull GremlinSource<T> source) {
        if (!(source instanceof GremlinSourceGraph)) {
            throw new GremlinUnexpectedSourceTypeException("should be the instance of GremlinSourceGraph");
        }

        return this.generateDeleteAllScript();
    }

    @Override
    public <T> List<String> generateFindAllScript(@NonNull GremlinSource<T> source) {
        throw new UnsupportedOperationException("Gremlin graph cannot be findAll.");
    }

    public List<String> generateIsEmptyScript() {
        final List<String> scriptList = Arrays.asList(Constants.GREMLIN_PRIMITIVE_GRAPH, Constants.GREMLIN_PRIMITIVE_VERTEX_ALL);
        final String query = String.join(Constants.GREMLIN_PRIMITIVE_INVOKE, scriptList);

        return Collections.singletonList(query);
    }

    @Override
    public <T> List<String> generateCountScript(@NonNull GremlinSource<T> source) {
        throw new UnsupportedOperationException("Gremlin graph counting is not available.");
    }
}
