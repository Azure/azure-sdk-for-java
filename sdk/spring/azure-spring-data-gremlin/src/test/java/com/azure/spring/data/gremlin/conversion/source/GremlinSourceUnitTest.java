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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
public class GremlinSourceUnitTest {

    private MappingGremlinConverter converter;

    @Autowired
    private ApplicationContext context;

    @BeforeEach
    public void setup() throws ClassNotFoundException {
        final GremlinMappingContext mappingContext = new GremlinMappingContext();

        mappingContext.setInitialEntitySet(new EntityScanner(this.context).scan(Persistent.class));

        this.converter = new MappingGremlinConverter(mappingContext);
    }

    @Test
    public void testVertexWriteException() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinSourceVertexWriter<>().write(new Object(), this.converter, new GremlinSourceEdge<>()));
    }

    @Test
    public void testVertexReadException() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinSourceVertexReader<>().read(Object.class, this.converter, new GremlinSourceEdge<>()));
    }

    @Test
    public void testEdgeWriteException() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinSourceEdgeWriter<>().write(new Object(), this.converter, new GremlinSourceVertex<>()));
    }

    @Test
    public void testEdgeReadException() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinSourceEdgeReader<>().read(Object.class, this.converter, new GremlinSourceVertex<>()));
    }

    @Test
    public void testGraphWriteException() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinSourceGraphWriter<>().write(new Object(), this.converter, new GremlinSourceVertex<>()));
    }

    @Test
    public void testGraphReadException() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinSourceEdgeReader<>().read(Object.class, this.converter, new GremlinSourceVertex<>()));
    }

    @Test
    public void testGraphAddSourceException() {
        assertThrows(GremlinUnexpectedSourceTypeException.class,
            () -> new GremlinSourceGraph<>().addGremlinSource(new GremlinSourceGraph<>()));
    }

    @Test
    public void testVertexWithPredefinedProperty() {
        @SuppressWarnings("unchecked") final GremlinSource<TestVertex> source =
            new GremlinSourceVertex<>(TestVertex.class);

        assertThrows(GremlinEntityInformationException.class, () ->
            new GremlinSourceVertexWriter<TestVertex>().write(new TestVertex("fake-id", "fake-name"),
                this.converter,
                source));
    }

    @Test
    public void testEdgeWithPredefinedProperty() {
        @SuppressWarnings("unchecked") final GremlinSource<TestEdge> source = new GremlinSourceEdge<>(TestEdge.class);

        assertThrows(GremlinEntityInformationException.class, () ->
            new GremlinSourceEdgeWriter<TestEdge>().write(new TestEdge("fake-id", "fake-name", "1", "2"),
                this.converter,
                source));
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
