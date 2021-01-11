// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query;

import com.azure.spring.data.gremlin.common.GremlinConfig;
import com.azure.spring.data.gremlin.common.GremlinFactory;
import com.azure.spring.data.gremlin.common.TestConstants;
import com.azure.spring.data.gremlin.common.TestGremlinProperties;
import com.azure.spring.data.gremlin.common.domain.InvalidDependency;
import com.azure.spring.data.gremlin.common.domain.Network;
import com.azure.spring.data.gremlin.common.domain.Person;
import com.azure.spring.data.gremlin.common.domain.Project;
import com.azure.spring.data.gremlin.common.domain.Relationship;
import com.azure.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceGraph;
import com.azure.spring.data.gremlin.exception.GremlinQueryException;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedEntityTypeException;
import com.azure.spring.data.gremlin.mapping.GremlinMappingContext;
import com.azure.spring.data.gremlin.repository.support.GremlinEntityInformation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.annotation.Persistent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@PropertySource(value = {"classpath:application.properties"})
@ContextConfiguration(classes = {GremlinTemplateIT.TestConfiguration.class})
@EnableConfigurationProperties(TestGremlinProperties.class)
public class GremlinTemplateIT {

    private final Person person = new Person(TestConstants.VERTEX_PERSON_ID, TestConstants.VERTEX_PERSON_NAME);
    private final Person person0 = new Person(TestConstants.VERTEX_PERSON_0_ID, TestConstants.VERTEX_PERSON_0_NAME);
    private final Person person1 = new Person(TestConstants.VERTEX_PERSON_1_ID, TestConstants.VERTEX_PERSON_1_NAME);

    private final Project project = new Project(TestConstants.VERTEX_PROJECT_ID, TestConstants.VERTEX_PROJECT_NAME,
            TestConstants.VERTEX_PROJECT_URI);
    private final Project project0 = new Project(TestConstants.VERTEX_PROJECT_0_ID, TestConstants.VERTEX_PROJECT_0_NAME,
            TestConstants.VERTEX_PROJECT_0_URI);

    private final Relationship relationship = new Relationship(TestConstants.EDGE_RELATIONSHIP_ID,
            TestConstants.EDGE_RELATIONSHIP_NAME, TestConstants.EDGE_RELATIONSHIP_LOCATION,
            this.person, this.project);
    private final Relationship relationship0 = new Relationship(TestConstants.EDGE_RELATIONSHIP_0_ID,
            TestConstants.EDGE_RELATIONSHIP_0_NAME, TestConstants.EDGE_RELATIONSHIP_0_LOCATION,
            this.person0, this.project);
    private final Relationship relationship1 = new Relationship(TestConstants.EDGE_RELATIONSHIP_1_ID,
            TestConstants.EDGE_RELATIONSHIP_1_NAME, TestConstants.EDGE_RELATIONSHIP_1_LOCATION,
            this.person1, this.project);
    private final Relationship relationship2 = new Relationship(TestConstants.EDGE_RELATIONSHIP_2_ID,
            TestConstants.EDGE_RELATIONSHIP_2_NAME, TestConstants.EDGE_RELATIONSHIP_2_LOCATION,
            this.person, this.project0);

    private Network network;

    private final GremlinEntityInformation<Person, String> personInfo = new GremlinEntityInformation<>(Person.class);
    private final GremlinEntityInformation<Project, String> projectInfo = new GremlinEntityInformation<>(Project.class);
    private final GremlinEntityInformation<Relationship, String> relationshipInfo =
            new GremlinEntityInformation<>(Relationship.class);
    private final GremlinEntityInformation<Network, String> networkInfo = new GremlinEntityInformation<>(Network.class);

    private final GremlinSource<Person> personSource = personInfo.createGremlinSource();
    private final GremlinSource<Project> projectSource = projectInfo.createGremlinSource();
    private final GremlinSource<Relationship> relationshipSource = relationshipInfo.createGremlinSource();
    private final GremlinSource<Network> networkSource = networkInfo.createGremlinSource();

    @Autowired
    private ApplicationContext context;

    @Autowired
    private GremlinFactory gremlinFactory;

    private GremlinTemplate template;

    @Before
    public void setup() throws ClassNotFoundException {
        final GremlinMappingContext mappingContext = new GremlinMappingContext();

        mappingContext.setInitialEntitySet(new EntityScanner(this.context).scan(Persistent.class));

        final MappingGremlinConverter converter = new MappingGremlinConverter(mappingContext);

        this.template = new GremlinTemplate(gremlinFactory, converter);
        this.template.deleteAll();
        this.network = new Network();
    }

    private void buildTestGraph() {
        this.network.vertexAdd(this.person);
        this.network.vertexAdd(this.person0);
        this.network.vertexAdd(this.person1);
        this.network.vertexAdd(this.project);
        this.network.vertexAdd(this.project0);

        this.network.edgeAdd(this.relationship);
        this.network.edgeAdd(this.relationship0);
        this.network.edgeAdd(this.relationship1);
        this.network.edgeAdd(this.relationship2);

        this.template.insert(this.network, this.networkSource);
    }

    @After
    public void cleanup() {
        this.template.deleteAll();
    }

    @Test
    public void testVertexDeleteAll() {
        this.buildTestGraph();

        Person personVertex = this.template.findVertexById(this.person.getId(), this.personSource);
        Project projectVertex = this.template.findVertexById(this.project.getId(), this.projectSource);
        Relationship relationshipEdge = this.template.findEdgeById(this.relationship.getId(), this.relationshipSource);

        Assert.assertNotNull(personVertex);
        Assert.assertNotNull(projectVertex);
        Assert.assertNotNull(relationshipEdge);

        this.template.deleteAll();

        personVertex = this.template.findVertexById(this.person.getId(), this.personSource);
        projectVertex = this.template.findVertexById(this.project.getId(), this.projectSource);
        relationshipEdge = this.template.findEdgeById(this.relationship.getId(), this.relationshipSource);

        Assert.assertNull(personVertex);
        Assert.assertNull(projectVertex);
        Assert.assertNull(relationshipEdge);

        Assert.assertTrue(this.template.findAll(this.personSource).isEmpty());
        Assert.assertTrue(this.template.findAll(this.projectSource).isEmpty());
        Assert.assertTrue(this.template.findAll(this.relationshipSource).isEmpty());
    }

    @Test
    public void testVertexInsertNormal() {
        this.template.insert(this.person0, this.personSource);

        final Person foundPerson = this.template.findVertexById(this.person0.getId(), this.personSource);

        Assert.assertNotNull(foundPerson);
        Assert.assertEquals(foundPerson.getId(), this.person0.getId());
        Assert.assertEquals(foundPerson.getName(), this.person0.getName());
    }

    @Test(expected = GremlinQueryException.class)
    public void testVertexInsertException() {
        this.template.insert(this.person, this.personSource);

        final Person repeated = new Person(this.person.getId(), this.person.getName());

        this.template.insert(repeated, this.personSource);
    }

    @Test
    public void testEdgeInsertNormal() {
        this.template.insert(this.person, this.personSource);
        this.template.insert(this.project, this.projectSource);
        this.template.insert(this.relationship, this.relationshipSource);

        final Relationship foundRelationship = this.template.findById(this.relationship.getId(), relationshipSource);

        Assert.assertNotNull(foundRelationship);
        Assert.assertEquals(foundRelationship.getId(), this.relationship.getId());
        Assert.assertEquals(foundRelationship.getName(), this.relationship.getName());
        Assert.assertEquals(foundRelationship.getLocation(), this.relationship.getLocation());
    }

    @Test(expected = GremlinQueryException.class)
    public void testEdgeInsertException() {
        this.template.insert(this.person, this.personSource);
        this.template.insert(this.project, this.projectSource);
        this.template.insert(this.relationship, this.relationshipSource);

        final Relationship repeated = new Relationship(this.relationship.getId(), this.relationship.getName(),
                this.relationship.getLocation(), this.person, this.project);

        this.template.insert(repeated, this.relationshipSource);
    }

    @Test
    public void testFindVertexById() {
        Person foundPerson = this.template.findVertexById(this.person1.getId(), this.personSource);
        Assert.assertNull(foundPerson);

        this.template.insert(this.person1, this.personSource);

        foundPerson = this.template.findVertexById(this.person1.getId(), this.personSource);

        Assert.assertNotNull(foundPerson);
        Assert.assertEquals(foundPerson.getId(), this.person1.getId());
        Assert.assertEquals(foundPerson.getName(), this.person1.getName());
    }

    @Test(expected = GremlinUnexpectedEntityTypeException.class)
    public void testFindVertexByIdException() {
        this.template.insert(this.person, this.personSource);
        this.template.insert(this.project0, this.projectSource);
        this.template.insert(this.relationship2, this.relationshipSource);

        this.template.findVertexById(this.project.getId(), this.relationshipSource);
    }

    @Test
    public void testFindEdgeById() {
        Relationship foundRelationship = this.template.findEdgeById(this.relationship2.getId(), relationshipSource);
        Assert.assertNull(foundRelationship);

        this.template.insert(this.person, this.personSource);
        this.template.insert(this.project0, this.projectSource);
        this.template.insert(this.relationship2, this.relationshipSource);

        foundRelationship = this.template.findEdgeById(this.relationship2.getId(), this.relationshipSource);

        Assert.assertNotNull(foundRelationship);
        Assert.assertEquals(foundRelationship.getId(), this.relationship2.getId());
        Assert.assertEquals(foundRelationship.getName(), this.relationship2.getName());
        Assert.assertEquals(foundRelationship.getLocation(), this.relationship2.getLocation());
    }

    @Test(expected = GremlinUnexpectedEntityTypeException.class)
    public void testFindEdgeByIdException() {
        this.template.insert(this.person, this.personSource);
        this.template.insert(this.project0, this.projectSource);
        this.template.insert(this.relationship2, this.relationshipSource);

        this.template.findEdgeById(this.relationship2.getId(), this.projectSource);
    }

    @Test
    public void testFindById() {
        this.buildTestGraph();
        final Person foundPerson = this.template.findById(this.person1.getId(), this.personSource);

        Assert.assertNotNull(foundPerson);
        Assert.assertEquals(foundPerson.getId(), this.person1.getId());
        Assert.assertEquals(foundPerson.getName(), this.person1.getName());

        final Relationship foundRelationship = this.template.findById(this.relationship.getId(), relationshipSource);

        Assert.assertNotNull(foundRelationship);
        Assert.assertEquals(foundRelationship.getId(), this.relationship.getId());
        Assert.assertEquals(foundRelationship.getName(), this.relationship.getName());
        Assert.assertEquals(foundRelationship.getLocation(), this.relationship.getLocation());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFindByIdException() {
        this.template.findById(this.network.getId(), this.networkSource);
    }

    @Test(expected = GremlinQueryException.class)
    public void testUpdateException() {
        this.personSource.setId(this.person.getId());
        this.template.update(this.person, this.personSource);
    }

    @Test
    public void testUpdateVertex() {
        this.template.insert(this.person, this.personSource);

        final String updatedName = "updated-person-name";
        final Person updatedPerson = new Person(this.person.getId(), updatedName);

        this.template.update(updatedPerson, this.personSource);

        final Person foundPerson = this.template.findById(updatedPerson.getId(), this.personSource);

        Assert.assertNotNull(foundPerson);
        Assert.assertEquals(this.person.getId(), foundPerson.getId());
        Assert.assertEquals(updatedPerson.getId(), foundPerson.getId());
        Assert.assertEquals(updatedPerson.getName(), foundPerson.getName());
    }

    @Test
    public void testUpdateEdge() {
        this.template.insert(this.person, this.personSource);
        this.template.insert(this.project0, this.projectSource);
        this.template.insert(this.relationship2, this.relationshipSource);

        final String updatedName = "updated-relation-name";
        final String updatedLocation = "updated-location";
        final Relationship updatedRelationship = new Relationship(TestConstants.EDGE_RELATIONSHIP_2_ID,
                updatedName, updatedLocation, this.person, this.project0);

        this.template.update(updatedRelationship, this.relationshipSource);

        final Relationship foundRelationship = this.template.findById(updatedRelationship.getId(), relationshipSource);

        Assert.assertNotNull(foundRelationship);
        Assert.assertEquals(this.relationship2.getId(), foundRelationship.getId());
        Assert.assertEquals(updatedRelationship.getId(), foundRelationship.getId());
        Assert.assertEquals(updatedRelationship.getName(), foundRelationship.getName());
        Assert.assertEquals(updatedRelationship.getLocation(), foundRelationship.getLocation());
    }

    @Test
    public void testUpdateGraph() {
        this.buildTestGraph();

        final String updatedName = "update-person-name";
        final String updatedLocation = "update-location";
        final String updatedUri = "http://localhost:2222";

        final Person person = (Person) this.network.getVertexList().get(0);
        final Project project = (Project) this.network.getVertexList().get(3);
        final Relationship relationship = (Relationship) this.network.getEdgeList().get(0);

        person.setName(updatedName);
        project.setUri(updatedUri);
        relationship.setLocation(updatedLocation);

        this.template.update(network, this.networkInfo.createGremlinSource());

        final Person foundPerson = this.template.findById(person.getId(), this.personSource);
        final Project foundProject = this.template.findById(project.getId(), this.projectSource);
        final Relationship foundRelationship = this.template.findById(relationship.getId(), this.relationshipSource);

        Assert.assertNotNull(foundPerson);
        Assert.assertNotNull(foundProject);
        Assert.assertNotNull(foundRelationship);

        Assert.assertEquals(foundPerson.getId(), person.getId());
        Assert.assertEquals(foundPerson.getName(), person.getName());

        Assert.assertEquals(foundProject.getId(), project.getId());
        Assert.assertEquals(foundProject.getUri(), project.getUri());

        Assert.assertEquals(foundRelationship.getId(), relationship.getId());
        Assert.assertEquals(foundRelationship.getLocation(), relationship.getLocation());
    }

    @Test
    public void testSaveVertex() {
        this.personSource.setId(this.person.getId());
        this.template.save(this.person, this.personSource);

        Person foundPerson = this.template.findById(this.person.getId(), this.personSource);

        Assert.assertNotNull(foundPerson);
        Assert.assertEquals(foundPerson.getId(), this.person.getId());
        Assert.assertEquals(foundPerson.getName(), this.person.getName());

        final String updatedName = "update-person-name";
        final Person updatedPerson = new Person(this.person.getId(), updatedName);

        this.personSource.setId(updatedPerson.getId());
        this.template.save(updatedPerson, this.personSource);

        foundPerson = this.template.findById(updatedPerson.getId(), this.personSource);

        Assert.assertNotNull(foundPerson);
        Assert.assertEquals(foundPerson.getId(), updatedPerson.getId());
        Assert.assertEquals(foundPerson.getName(), updatedPerson.getName());
    }

    @Test
    public void testSaveEdge() {
        this.template.insert(this.person, this.personSource);
        this.template.insert(this.project, this.projectSource);
        this.relationshipSource.setId(this.relationship.getId());
        this.template.save(this.relationship, this.relationshipSource);

        Relationship foundRelationship = this.template.findById(this.relationship.getId(), this.relationshipSource);

        Assert.assertNotNull(foundRelationship);
        Assert.assertEquals(foundRelationship.getId(), this.relationship.getId());
        Assert.assertEquals(foundRelationship.getName(), this.relationship.getName());
        Assert.assertEquals(foundRelationship.getLocation(), this.relationship.getLocation());

        final String updatedName = "updated-relation-name";
        final String updatedLocation = "updated-location";
        final Relationship updatedRelationship = new Relationship(TestConstants.EDGE_RELATIONSHIP_2_ID,
                updatedName, updatedLocation, this.person, this.project);

        this.relationshipSource.setId(updatedRelationship.getId());
        this.template.save(updatedRelationship, this.relationshipSource);

        foundRelationship = this.template.findById(updatedRelationship.getId(), this.relationshipSource);

        Assert.assertNotNull(foundRelationship);
        Assert.assertEquals(this.relationship2.getId(), foundRelationship.getId());
        Assert.assertEquals(updatedRelationship.getId(), foundRelationship.getId());
        Assert.assertEquals(updatedRelationship.getName(), foundRelationship.getName());
        Assert.assertEquals(updatedRelationship.getLocation(), foundRelationship.getLocation());
    }

    @Test
    public void testSaveGraph() {
        this.network.vertexAdd(this.person);
        this.network.vertexAdd(this.project);
        this.network.edgeAdd(this.relationship);

        this.template.save(network, this.networkSource);

        final Person personFound = this.template.findById(this.person.getId(), this.personSource);

        Assert.assertNotNull(personFound);
        Assert.assertEquals(personFound.getId(), this.person.getId());

        Relationship relationshipFound = this.template.findById(this.relationship.getId(), this.relationshipSource);

        Assert.assertNotNull(relationshipFound);
        Assert.assertEquals(relationshipFound.getId(), this.relationship.getId());

        final String updatedName = "updated-name";
        this.relationship.setName(updatedName);

        this.template.save(network, this.networkInfo.createGremlinSource());

        relationshipFound = this.template.findById(this.relationship.getId(), this.relationshipSource);

        Assert.assertNotNull(relationshipFound);
        Assert.assertEquals(relationshipFound.getId(), this.relationship.getId());
        Assert.assertEquals(relationshipFound.getName(), updatedName);
    }

    @Test
    public void testFindAllVertex() {
        List<Person> personList = this.template.findAll(this.personSource);

        Assert.assertTrue(personList.isEmpty());

        final List<Person> personCollection = Arrays.asList(this.person, this.person0, this.person1);
        personCollection.forEach(person -> this.template.insert(person, this.personSource));

        personList = this.template.findAll(this.personSource);

        Assert.assertFalse(personList.isEmpty());
        Assert.assertEquals(personList.size(), personCollection.size());

        personList.sort((a, b) -> (a.getId().compareTo(b.getId())));
        personCollection.sort((a, b) -> (a.getId().compareTo(b.getId())));

        Assert.assertArrayEquals(personList.toArray(), personCollection.toArray());
    }

    @Test
    public void testFindAllEdge() {
        this.template.insert(this.person, this.personSource);
        this.template.insert(this.person0, this.personSource);
        this.template.insert(this.person1, this.personSource);
        this.template.insert(this.project, this.projectSource);
        this.template.insert(this.project0, this.projectSource);

        List<Relationship> relationshipList = this.template.findAll(this.relationshipSource);

        Assert.assertTrue(relationshipList.isEmpty());

        final List<Relationship> relationshipCollection = Arrays.asList(this.relationship, this.relationship0,
                this.relationship1, this.relationship2);
        relationshipCollection.forEach(relationship -> this.template.insert(relationship, this.relationshipSource));

        relationshipList = this.template.findAll(this.relationshipSource);

        Assert.assertFalse(relationshipList.isEmpty());
        Assert.assertEquals(relationshipList.size(), relationshipCollection.size());

        relationshipList.sort((a, b) -> (a.getId().compareTo(b.getId())));
        relationshipCollection.sort((a, b) -> (a.getId().compareTo(b.getId())));

        Assert.assertArrayEquals(relationshipList.toArray(), relationshipCollection.toArray());
    }

    @Test
    public void testVertexDeleteById() {
        this.template.deleteById(this.person.getId(), this.personSource);
        this.template.insert(this.person, this.personSource);
        this.template.deleteById(this.person0.getId(), this.personSource);

        Person foundPerson = this.template.findById(this.person.getId(), this.personSource);
        Assert.assertNotNull(foundPerson);

        this.template.deleteById(this.person.getId(), this.personSource);

        foundPerson = this.template.findById(this.person.getId(), this.personSource);
        Assert.assertNull(foundPerson);
    }

    @Test
    public void testEdgeDeleteById() {
        this.template.deleteById(this.relationship.getId(), this.relationshipSource);

        this.template.insert(this.person, this.personSource);
        this.template.insert(this.project, this.projectSource);
        this.template.insert(this.relationship, this.relationshipSource);

        this.template.deleteById(this.relationship0.getId(), this.relationshipSource);

        Relationship foundRelationship = this.template.findById(this.relationship.getId(), this.relationshipSource);
        Assert.assertNotNull(foundRelationship);

        this.template.deleteById(this.relationship.getId(), this.relationshipSource);

        foundRelationship = this.template.findById(this.relationship.getId(), this.relationshipSource);
        Assert.assertNull(foundRelationship);
    }

    @Test
    public void testGraphDeleteById() {
        this.network.setId("fake-id");
        this.template.deleteById(this.network.getId(), this.relationshipSource);

        final Relationship foundRelationship = this.template.findById(this.relationship.getId(), relationshipSource);
        Assert.assertNull(foundRelationship);

        final Person foundPerson = this.template.findById(this.person.getId(), this.personSource);
        Assert.assertNull(foundPerson);
    }

    @Test
    public void testIsEmptyGraph() {
        Assert.assertTrue(this.template.isEmptyGraph(this.networkSource));

        this.network.vertexAdd(this.person);
        this.network.vertexAdd(this.project);
        this.network.edgeAdd(this.relationship);
        this.template.insert(this.network, this.networkSource);

        Assert.assertFalse(this.template.isEmptyGraph(this.networkSource));
    }

    @Test(expected = GremlinQueryException.class)
    public void testIsEmptyGraphException() {
        this.template.isEmptyGraph(this.relationshipSource);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testInvalidDependencySaveException() {
        final InvalidDependency dependency = new InvalidDependency(this.relationship.getId(),
                this.relationship.getName(), this.person.getId(), this.project.getId());
        final GremlinSource<InvalidDependency> source = new GremlinSourceGraph<>(InvalidDependency.class);

        this.personSource.setId(this.person.getId());
        this.template.save(this.person, this.personSource);
        this.projectSource.setId(this.project.getId());
        this.template.save(this.project, this.projectSource);
        this.relationshipSource.setId(this.relationship.getId());
        this.template.save(this.relationship, this.relationshipSource);

        this.template.findById(dependency.getId(), source);
    }

    @Configuration
    static class TestConfiguration {

        @Autowired
        private TestGremlinProperties properties;

        @Bean
        public GremlinFactory getGremlinFactory() {
            return new GremlinFactory(getGremlinConfig());
        }

        @Bean
        public GremlinConfig getGremlinConfig() {
            return GremlinConfig.builder(properties.getEndpoint(), properties.getUsername(), properties.getPassword())
                    .sslEnabled(properties.isSslEnabled())
                    .port(properties.getPort())
                    .build();
        }
    }
}

