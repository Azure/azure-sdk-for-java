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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class MappingGremlinConverterUnitTest {

    private MappingGremlinConverter converter;
    private GremlinMappingContext mappingContext;

    @Mock
    private ApplicationContext applicationContext;

    @Before
    public void setup() {
        this.mappingContext = new GremlinMappingContext();

        this.mappingContext.setApplicationContext(this.applicationContext);
        this.mappingContext.afterPropertiesSet();
        this.mappingContext.getPersistentEntity(Person.class);

        this.converter = new MappingGremlinConverter(this.mappingContext);
    }

    @Test
    public void testMappingGremlinConverterGetter() {
        Assert.assertEquals(this.converter.getMappingContext(), this.mappingContext);
        Assert.assertNotNull(this.converter.getConversionService());

        final Person person = new Person(TestConstants.VERTEX_PERSON_ID, TestConstants.VERTEX_PERSON_NAME);
        FieldUtils.getAllFields(Person.class);

        Assert.assertNotNull(this.converter.getPropertyAccessor(person));
        Assert.assertEquals(converter.getIdFieldValue(person), TestConstants.VERTEX_PERSON_ID);
    }

    @Test
    public void testMappingGremlinConverterVertexRead() {
        final Person person = new Person(TestConstants.VERTEX_PERSON_ID, TestConstants.VERTEX_PERSON_NAME);
        final GremlinEntityInformation<Person, String> info = new GremlinEntityInformation<>(Person.class);
        final GremlinSource<Person> source = info.createGremlinSource();

        this.converter.write(person, source);

        Assert.assertTrue(source.getId().isPresent());
        Assert.assertEquals(source.getId().get(), person.getId());
        Assert.assertEquals(source.getProperties().get(TestConstants.PROPERTY_NAME), person.getName());
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

        Assert.assertTrue(source.getId().isPresent());
        Assert.assertEquals(source.getId().get(), relationship.getId());
        Assert.assertEquals(source.getProperties().get(TestConstants.PROPERTY_NAME), relationship.getName());
    }
}

