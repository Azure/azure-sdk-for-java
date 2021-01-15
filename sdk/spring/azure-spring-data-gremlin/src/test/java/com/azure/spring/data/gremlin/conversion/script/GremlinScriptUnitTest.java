// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.script;

import com.azure.spring.data.gremlin.common.domain.Person;
import com.azure.spring.data.gremlin.conversion.result.GremlinResultEdgeReader;
import com.azure.spring.data.gremlin.conversion.result.GremlinResultVertexReader;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceEdge;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceVertex;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedEntityTypeException;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;

public class GremlinScriptUnitTest {

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testVertexWriteException() {
        new GremlinResultVertexReader().read(singletonList(new Result(new Object())), new GremlinSourceEdge<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testEdgeReadException() {
        new GremlinResultEdgeReader().read(singletonList(new Result(new Object())), new GremlinSourceVertex<>());
    }

    @Test(expected = GremlinUnexpectedEntityTypeException.class)
    public void testGeneratePropertyException() {
        final Map<String, Object> properties = new HashMap<>();
        final GremlinSource<?> source = new GremlinSourceVertex<>();
        properties.put("person", source);
        GremlinScriptLiteralHelper.generateProperties(properties);
    }

    @Test(expected = GremlinUnexpectedEntityTypeException.class)
    public void testGenerateHasException() {
        final GremlinSource<?> source = new GremlinSourceVertex<>();
        GremlinScriptLiteralHelper.generateHas("fake-name", source);
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testEdgeDeleteByIdScriptException() {
        new GremlinScriptLiteralEdge().generateDeleteByIdScript(new GremlinSourceVertex<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testGraphDeleteByIdScriptException() {
        new GremlinScriptLiteralGraph().generateDeleteByIdScript(new GremlinSourceVertex<>());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGraphFindAllScriptException() {
        new GremlinScriptLiteralGraph().generateFindAllScript(new GremlinSourceVertex<>());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGraphCountException() {
        new GremlinScriptLiteralGraph().generateCountScript(new GremlinSourceVertex<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testEdgeCountScriptException() {
        new GremlinScriptLiteralEdge().generateCountScript(new GremlinSourceVertex<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testVertexDeleteByIdScriptException() {
        new GremlinScriptLiteralGraph().generateDeleteByIdScript(new GremlinSourceEdge<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testVertexCountScriptException() {
        new GremlinScriptLiteralVertex().generateCountScript(new GremlinSourceEdge<>());
    }

    @Test
    public void testVertexSourceSetProperty() {
        final GremlinSource<Person> source = new GremlinSourceVertex<>(Person.class);
        final Map<String, Object> properties = source.getProperties();
        final String fakeName = "fake-name";

        properties.put(fakeName, "fake-value");
        properties.put("fake-name-0", "fake-value-0");
        properties.put("fake-name-1", "fake-value-1");

        source.setProperty(fakeName, null);

        Assert.assertEquals(source.getProperties().size(), 3); // one predefined property _classname
        Assert.assertNull(source.getProperties().get(fakeName));
    }
}
