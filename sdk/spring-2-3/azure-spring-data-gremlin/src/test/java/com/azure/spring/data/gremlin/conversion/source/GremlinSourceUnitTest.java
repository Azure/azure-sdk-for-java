// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.source;

import com.azure.spring.data.gremlin.annotation.EdgeFrom;
import com.azure.spring.data.gremlin.annotation.EdgeTo;
import com.azure.spring.data.gremlin.annotation.Vertex;
import com.azure.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.azure.spring.data.gremlin.exception.GremlinEntityInformationException;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import com.azure.spring.data.gremlin.mapping.GremlinMappingContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class GremlinSourceUnitTest {

    private MappingGremlinConverter converter;

    @Autowired
    private ApplicationContext context;

    @Before
    public void setup() throws ClassNotFoundException {
        final GremlinMappingContext mappingContext = new GremlinMappingContext();

        mappingContext.setInitialEntitySet(new EntityScanner(this.context).scan(Persistent.class));

        this.converter = new MappingGremlinConverter(mappingContext);
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testVertexWriteException() {
        new GremlinSourceVertexWriter<>().write(new Object(), this.converter, new GremlinSourceEdge<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testVertexReadException() {
        new GremlinSourceVertexReader<>().read(Object.class, this.converter, new GremlinSourceEdge<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testEdgeWriteException() {
        new GremlinSourceEdgeWriter<>().write(new Object(), this.converter, new GremlinSourceVertex<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testEdgeReadException() {
        new GremlinSourceEdgeReader<>().read(Object.class, this.converter, new GremlinSourceVertex<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testGraphWriteException() {
        new GremlinSourceGraphWriter<>().write(new Object(), this.converter, new GremlinSourceVertex<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testGraphReadException() {
        new GremlinSourceEdgeReader<>().read(Object.class, this.converter, new GremlinSourceVertex<>());
    }

    @Test(expected = GremlinUnexpectedSourceTypeException.class)
    public void testGraphAddSourceException() {
        new GremlinSourceGraph<>().addGremlinSource(new GremlinSourceGraph<>());
    }

    @Test(expected = GremlinEntityInformationException.class)
    public void testVertexWithPredefinedProperty() {
        @SuppressWarnings("unchecked") final GremlinSource<TestVertex> source = new GremlinSourceVertex<>(TestVertex.class);

        new GremlinSourceVertexWriter<TestVertex>().write(new TestVertex("fake-id", "fake-name"), this.converter,
            source);
    }

    @Test(expected = GremlinEntityInformationException.class)
    public void testEdgeWithPredefinedProperty() {
        @SuppressWarnings("unchecked") final GremlinSource<TestEdge> source = new GremlinSourceEdge<>(TestEdge.class);

        new GremlinSourceEdgeWriter<TestEdge>().write(new TestEdge("fake-id", "fake-name", "1", "2"), this.converter, source);
    }

    @Vertex
    private static class TestVertex {

        private String id;

        private String classname;

        TestVertex(String id, String classname) {
            this.id = id;
            this.classname = classname;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getClassname() {
            return classname;
        }

        public void setClassname(String classname) {
            this.classname = classname;
        }
    }

    @Vertex
    private static class TestEdge {

        private String id;

        private String classname;

        @EdgeFrom
        private String from;

        @EdgeTo
        private String to;

        TestEdge(String id, String classname, String from, String to) {
            this.id = id;
            this.classname = classname;
            this.from = from;
            this.to = to;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getClassname() {
            return classname;
        }

        public void setClassname(String classname) {
            this.classname = classname;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }
}
