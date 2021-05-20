// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository.support;

import com.azure.spring.data.gremlin.common.TestConstants;
import com.azure.spring.data.gremlin.common.domain.Network;
import com.azure.spring.data.gremlin.common.domain.Person;
import com.azure.spring.data.gremlin.common.domain.Relationship;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceEdge;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceGraph;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceVertex;
import com.azure.spring.data.gremlin.exception.GremlinInvalidEntityIdFieldException;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedEntityTypeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class GremlinEntityInformationUnitTest {

    @Test
    public void testVertexEntityInformation() {
        final Person person = new Person(TestConstants.VERTEX_PERSON_ID, TestConstants.VERTEX_PERSON_NAME);
        final GremlinEntityInformation<Person, String> personInfo = new GremlinEntityInformation<>(Person.class);

        Assertions.assertNotNull(personInfo.getIdField());
        Assertions.assertEquals(personInfo.getId(person), TestConstants.VERTEX_PERSON_ID);
        Assertions.assertEquals(personInfo.getIdType(), String.class);
        Assertions.assertTrue(personInfo.createGremlinSource() instanceof GremlinSourceVertex);
    }

    @Test
    public void testEdgeEntityInformation() {
        final GremlinEntityInformation<Relationship, String> relationshipInfo =
            new GremlinEntityInformation<>(Relationship.class);

        Assertions.assertNotNull(relationshipInfo.getIdField());
        Assertions.assertTrue(relationshipInfo.createGremlinSource() instanceof GremlinSourceEdge);
    }

    @Test
    public void testGraphEntityInformation() {
        final GremlinEntityInformation<Network, String> networkInfo = new GremlinEntityInformation<>(Network.class);

        Assertions.assertNotNull(networkInfo.getIdField());
        Assertions.assertTrue(networkInfo.createGremlinSource() instanceof GremlinSourceGraph);
    }

    @Test
    public void testEntityInformationException() {
        assertThrows(GremlinUnexpectedEntityTypeException.class,
            ()->new GremlinEntityInformation<TestDomain, String>(TestDomain.class).createGremlinSource());
    }

    @Test
    public void testEntityInformationNoIdException() {
        assertThrows(GremlinInvalidEntityIdFieldException.class,
            ()-> new GremlinEntityInformation<TestNoIdDomain, String>(TestNoIdDomain.class));
    }

    @Test
    public void testEntityInformationMultipleIdException() {
        assertThrows(GremlinInvalidEntityIdFieldException.class,
            ()->  new GremlinEntityInformation<TestMultipleIdDomain, String>(TestMultipleIdDomain.class));
    }

    @Test
    public void testEntityInformationNoStringIdException() {
        assertThrows(GremlinInvalidEntityIdFieldException.class,
            ()->   new GremlinEntityInformation<TestNoStringIdDomain, String>(TestNoStringIdDomain.class));
    }

    @Test
    public void testEntityInformationIdFieldAndIdAnnotation() {
        assertThrows(GremlinInvalidEntityIdFieldException.class,
            ()-> new GremlinEntityInformation<TestIdFieldAndIdAnnotation, String>(TestIdFieldAndIdAnnotation.class));
    }

    private class TestDomain {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    private class TestNoIdDomain {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private class TestMultipleIdDomain {
        @Id
        private String name;

        @Id
        private String location;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    private class TestIdFieldAndIdAnnotation {
        @Id
        private String name;

        @Id
        private String where;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getWhere() {
            return where;
        }

        public void setWhere(String where) {
            this.where = where;
        }
    }

    private class TestNoStringIdDomain {
        @Id
        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
