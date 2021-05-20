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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GremlinScriptUnitTest {

    @Test
    public void testVertexWriteException() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinResultVertexReader().read(singletonList(new Result(new Object())),
                new GremlinSourceEdge<>()));
    }

    @Test
    public void testEdgeReadException() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinResultEdgeReader().read(singletonList(new Result(new Object())),
                new GremlinSourceVertex<>()));
    }

    @Test
    public void testGeneratePropertyException() {
        final Map<String, Object> properties = new HashMap<>();
        final GremlinSource<?> source = new GremlinSourceVertex<>();
        properties.put("person", source);
        assertThrows(GremlinUnexpectedEntityTypeException.class,
            () -> GremlinScriptLiteralHelper.generateProperties(properties));
    }

    @Test
    public void testGenerateHasException() {
        final GremlinSource<?> source = new GremlinSourceVertex<>();
        assertThrows(GremlinUnexpectedEntityTypeException.class,
            () -> GremlinScriptLiteralHelper.generateHas("fake-name", source));
    }

    @Test
    public void testEdgeDeleteByIdScriptException() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinScriptLiteralEdge().generateDeleteByIdScript(new GremlinSourceVertex<>()));
    }

    @Test
    public void testGraphDeleteByIdScriptException() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinScriptLiteralGraph().generateDeleteByIdScript(new GremlinSourceVertex<>()));
    }

    @Test
    public void testGraphFindAllScriptException() {
        assertThrows(UnsupportedOperationException.class,
            () -> new GremlinScriptLiteralGraph().generateFindAllScript(new GremlinSourceVertex<>()));
    }

    @Test
    public void testGraphCountException() {
        assertThrows(UnsupportedOperationException.class,
            () -> new GremlinScriptLiteralGraph().generateCountScript(new GremlinSourceVertex<>()));
    }

    @Test
    public void testEdgeCountScriptException() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinScriptLiteralEdge().generateCountScript(new GremlinSourceVertex<>()));
    }

    @Test
    public void testVertexDeleteByIdScriptException() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinScriptLiteralGraph().generateDeleteByIdScript(new GremlinSourceEdge<>()));
    }

    @Test
    public void testVertexCountScriptException() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinScriptLiteralVertex().generateCountScript(new GremlinSourceEdge<>()));
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

        Assertions.assertEquals(source.getProperties().size(), 3); // one predefined property _classname
        Assertions.assertNull(source.getProperties().get(fakeName));
    }
}
