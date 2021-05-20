// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion;

import com.azure.spring.data.gremlin.common.TestConstants;
import com.azure.spring.data.gremlin.common.domain.Person;
import com.azure.spring.data.gremlin.common.domain.Project;
import com.azure.spring.data.gremlin.common.domain.Relationship;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.mapping.GremlinMappingContext;
import com.azure.spring.data.gremlin.repository.support.GremlinEntityInformation;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

public class MappingGremlinConverterUnitTest {

    private MappingGremlinConverter converter;
    private GremlinMappingContext mappingContext;
    private AutoCloseable closeable;

    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    public void setup() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.mappingContext = new GremlinMappingContext();

        this.mappingContext.setApplicationContext(this.applicationContext);
        this.mappingContext.afterPropertiesSet();
        this.mappingContext.getPersistentEntity(Person.class);

        this.converter = new MappingGremlinConverter(this.mappingContext);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void testMappingGremlinConverterGetter() {
        Assertions.assertEquals(this.converter.getMappingContext(), this.mappingContext);
        Assertions.assertNotNull(this.converter.getConversionService());

        final Person person = new Person(TestConstants.VERTEX_PERSON_ID, TestConstants.VERTEX_PERSON_NAME);
        FieldUtils.getAllFields(Person.class);

        Assertions.assertNotNull(this.converter.getPropertyAccessor(person));
        Assertions.assertEquals(converter.getIdFieldValue(person), TestConstants.VERTEX_PERSON_ID);
    }

    @Test
    public void testMappingGremlinConverterVertexRead() {
        final Person person = new Person(TestConstants.VERTEX_PERSON_ID, TestConstants.VERTEX_PERSON_NAME);
        final GremlinEntityInformation<Person, String> info = new GremlinEntityInformation<>(Person.class);
        final GremlinSource<Person> source = info.createGremlinSource();

        this.converter.write(person, source);

        Assertions.assertTrue(source.getId().isPresent());
        Assertions.assertEquals(source.getId().get(), person.getId());
        Assertions.assertEquals(source.getProperties().get(TestConstants.PROPERTY_NAME), person.getName());
    }

    @Test
    public void testMappingGremlinConverterEdgeRead() {
        final Person person = new Person(TestConstants.VERTEX_PERSON_ID, TestConstants.VERTEX_PERSON_NAME);
        final Project project = new Project(TestConstants.VERTEX_PROJECT_ID, TestConstants.VERTEX_PROJECT_NAME,
                TestConstants.VERTEX_PROJECT_URI);
        final Relationship relationship = new Relationship(TestConstants.EDGE_RELATIONSHIP_ID,
                TestConstants.EDGE_RELATIONSHIP_NAME, TestConstants.EDGE_RELATIONSHIP_LOCATION, person, project);
        final GremlinEntityInformation<Relationship, String> info = new GremlinEntityInformation<>(Relationship.class);
        final GremlinSource<Relationship> source = info.createGremlinSource();

        this.converter.write(relationship, source);

        Assertions.assertTrue(source.getId().isPresent());
        Assertions.assertEquals(source.getId().get(), relationship.getId());
        Assertions.assertEquals(source.getProperties().get(TestConstants.PROPERTY_NAME), relationship.getName());
    }
}

