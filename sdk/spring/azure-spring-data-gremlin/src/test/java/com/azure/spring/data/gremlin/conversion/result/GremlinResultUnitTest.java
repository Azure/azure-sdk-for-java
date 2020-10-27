// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.result;

import com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralEdge;
import com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralGraph;
import com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralVertex;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceEdge;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceVertex;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import org.junit.Test;

public class GremlinResultUnitTest {

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testVertexInsertException() {
        new GremlinScriptLiteralVertex().generateInsertScript(new GremlinSourceEdge<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testVertexUpdateException() {
        new GremlinScriptLiteralVertex().generateUpdateScript(new GremlinSourceEdge<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testVertexFindByIdException() {
        new GremlinScriptLiteralVertex().generateFindByIdScript(new GremlinSourceEdge<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testEdgeInsertException() {
        new GremlinScriptLiteralEdge().generateInsertScript(new GremlinSourceVertex<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testEdgeUpdateException() {
        new GremlinScriptLiteralEdge().generateUpdateScript(new GremlinSourceVertex<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testEdgeFindByIdException() {
        new GremlinScriptLiteralEdge().generateFindByIdScript(new GremlinSourceVertex<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testGraphInsertException() {
        new GremlinScriptLiteralGraph().generateInsertScript(new GremlinSourceVertex<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testGraphUpdateException() {
        new GremlinScriptLiteralGraph().generateUpdateScript(new GremlinSourceVertex<>());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGraphFindByIdException() {
        new GremlinScriptLiteralGraph().generateFindByIdScript(new GremlinSourceVertex<>());
    }
}
