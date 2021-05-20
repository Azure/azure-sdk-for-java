// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository;

import com.azure.spring.data.gremlin.common.GremlinEntityType;
import com.azure.spring.data.gremlin.common.TestConstants;
import com.azure.spring.data.gremlin.common.TestRepositoryConfiguration;
import com.azure.spring.data.gremlin.common.domain.Person;
import com.azure.spring.data.gremlin.common.domain.Project;
import com.azure.spring.data.gremlin.common.domain.Relationship;
import com.azure.spring.data.gremlin.common.repository.PersonRepository;
import com.azure.spring.data.gremlin.common.repository.ProjectRepository;
import com.azure.spring.data.gremlin.common.repository.RelationshipRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRepositoryConfiguration.class)
public class RelationshipRepositoryIT {

    private final Person person = new Person(TestConstants.VERTEX_PERSON_ID, TestConstants.VERTEX_PERSON_NAME);
    private final Person person0 = new Person(TestConstants.VERTEX_PERSON_0_ID, TestConstants.VERTEX_PERSON_0_NAME);
    private final Project project = new Project(TestConstants.VERTEX_PROJECT_ID, TestConstants.VERTEX_PROJECT_NAME,
            TestConstants.VERTEX_PROJECT_URI);
    private final Relationship relationship = new Relationship(TestConstants.EDGE_RELATIONSHIP_ID,
            TestConstants.EDGE_RELATIONSHIP_NAME, TestConstants.EDGE_RELATIONSHIP_LOCATION,
            this.person, this.project);
    private final Relationship relationship0 = new Relationship(TestConstants.EDGE_RELATIONSHIP_0_ID,
            TestConstants.EDGE_RELATIONSHIP_0_NAME, TestConstants.EDGE_RELATIONSHIP_0_LOCATION,
            this.person0, this.project);

    @Autowired
    private RelationshipRepository relationshipRepo;

    @Autowired
    private PersonRepository personRepo;

    @Autowired
    private ProjectRepository projectRepo;

    @BeforeEach
    public void setup() {
        this.relationshipRepo.deleteAll();
    }

    @AfterEach
    public void cleanup() {
        this.relationshipRepo.deleteAll();
    }

    @Test
    public void testDeleteAll() {
        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.save(this.relationship);

        Assertions.assertTrue(this.relationshipRepo.existsById(this.relationship.getId()));

        this.relationshipRepo.deleteAll();

        Assertions.assertFalse(this.relationshipRepo.existsById(this.person.getId()));
    }

    @Test
    public void testDeleteById() {
        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.save(this.relationship);

        Assertions.assertTrue(this.relationshipRepo.existsById(this.relationship.getId()));

        this.relationshipRepo.deleteById(this.relationship.getId());

        Assertions.assertFalse(this.relationshipRepo.existsById(this.relationship.getId()));
    }

    @Test
    public void testDelete() {
        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.save(this.relationship);

        Assertions.assertTrue(this.relationshipRepo.existsById(this.relationship.getId()));

        this.relationshipRepo.delete(this.relationship);

        Assertions.assertFalse(this.relationshipRepo.existsById(this.relationship.getId()));
    }

    @Test
    public void testDeleteAllIds() {
        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.save(this.relationship);

        final List<Relationship> domains = Arrays.asList(this.relationship);

        this.relationshipRepo.deleteAll(domains);

        Assertions.assertFalse(this.relationshipRepo.existsById(this.relationship.getId()));
    }

    @Test
    public void testSave() {
        Assertions.assertFalse(this.relationshipRepo.existsById(this.relationship.getId()));

        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.save(this.relationship);

        Assertions.assertTrue(this.relationshipRepo.existsById(this.relationship.getId()));
    }

    @Test
    public void testSaveAll() {
        Assertions.assertFalse(this.relationshipRepo.existsById(this.relationship.getId()));

        final List<Relationship> domains = Arrays.asList(this.relationship);

        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.saveAll(domains);

        Assertions.assertTrue(this.relationshipRepo.existsById(this.relationship.getId()));
    }

    @Test
    public void testFindById() {
        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.save(this.relationship);

        final Relationship foundRelationship = this.relationshipRepo.findById(this.relationship.getId()).get();

        Assertions.assertNotNull(foundRelationship);
        Assertions.assertEquals(foundRelationship.getId(), this.relationship.getId());
        Assertions.assertEquals(foundRelationship.getName(), this.relationship.getName());

        Assertions.assertFalse(this.relationshipRepo.findById(this.person.getId()).isPresent());
    }

    @Test
    public void testExistById() {
        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.save(this.relationship);

        Assertions.assertFalse(this.relationshipRepo.existsById(this.person.getId()));
        Assertions.assertTrue(this.relationshipRepo.existsById(this.relationship.getId()));
    }


    @Test
    public void testFindAllById() {
        final List<Relationship> domains = Arrays.asList(this.relationship);
        final List<String> ids = Arrays.asList(this.relationship.getId());

        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.saveAll(domains);

        final List<Relationship> foundDomains = (List<Relationship>) this.relationshipRepo.findAllById(ids);

        domains.sort((a, b) -> (a.getId().compareTo(b.getId())));
        foundDomains.sort((a, b) -> (a.getId().compareTo(b.getId())));

        Assertions.assertArrayEquals(domains.toArray(), foundDomains.toArray());
    }

    @Test
    public void testVertexCount() {
        Assertions.assertEquals(this.personRepo.count(), 0);
        Assertions.assertEquals(this.projectRepo.edgeCount(), 0);
        Assertions.assertEquals(this.relationshipRepo.vertexCount(), 0);

        this.personRepo.save(this.person0);
        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.save(this.relationship);

        Assertions.assertEquals(this.personRepo.vertexCount(), 3);
        Assertions.assertEquals(this.projectRepo.vertexCount(), 3);
        Assertions.assertEquals(this.relationshipRepo.edgeCount(), 1);
        Assertions.assertEquals(this.relationshipRepo.count(), 4);
    }

    @Test
    public void testRelationshipFindByName() {
        this.personRepo.save(this.person0);
        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.save(this.relationship);

        final List<Relationship> relationships = this.relationshipRepo.findByLocation(this.relationship.getLocation());

        Assertions.assertEquals(relationships.size(), 1);
        Assertions.assertEquals(relationships.get(0), this.relationship);

        this.relationshipRepo.deleteAll();

        Assertions.assertTrue(this.relationshipRepo.findByLocation(this.relationship.getLocation()).isEmpty());
    }

    @Test
    public void testDeleteAllByType() {
        this.personRepo.save(this.person0);
        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.save(this.relationship);

        this.relationshipRepo.deleteAll(GremlinEntityType.EDGE);

        Assertions.assertFalse(this.relationshipRepo.findById(this.relationship.getId()).isPresent());
        Assertions.assertTrue(this.personRepo.existsById(this.person.getId()));
        Assertions.assertTrue(this.personRepo.existsById(this.person0.getId()));
        Assertions.assertTrue(this.projectRepo.existsById(this.project.getId()));
    }

    @Test
    public void testDeleteAllByClass() {
        this.personRepo.save(this.person0);
        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.deleteAll(Relationship.class);

        Assertions.assertFalse(this.relationshipRepo.findById(this.relationship.getId()).isPresent());
        Assertions.assertTrue(this.personRepo.findById(this.person0.getId()).isPresent());
        Assertions.assertTrue(this.projectRepo.findById(this.project.getId()).isPresent());
    }

    @Test
    public void testFindByNameAndLocation() {
        this.personRepo.save(this.person0);
        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.save(this.relationship);

        final List<Relationship> domains = this.relationshipRepo.findByNameAndLocation(relationship.getName(),
                relationship.getLocation());

        Assertions.assertEquals(domains.size(), 1);
        Assertions.assertEquals(domains.get(0), this.relationship);
        Assertions.assertTrue(relationshipRepo.findByNameAndLocation(relationship.getName(), "faker").isEmpty());
    }

    @Test
    public void testByNameOrId() {
        this.personRepo.save(this.person0);
        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);
        this.relationshipRepo.save(this.relationship);
        this.relationshipRepo.save(this.relationship0);

        final List<Relationship> domains = Arrays.asList(this.relationship, this.relationship0);
        List<Relationship> foundDomains = relationshipRepo.findByNameOrId(relationship.getName(),
                relationship0.getId());

        domains.sort(Comparator.comparing(Relationship::getId));
        foundDomains.sort(Comparator.comparing(Relationship::getId));

        Assertions.assertEquals(foundDomains.size(), 2);
        Assertions.assertEquals(foundDomains, domains);

        foundDomains = this.relationshipRepo.findByNameOrId("fake-name", relationship0.getId());

        Assertions.assertEquals(foundDomains.size(), 1);
        Assertions.assertEquals(foundDomains.get(0), this.relationship0);
    }

    @Test
    public void testFindAll() {
        this.personRepo.save(this.person0);
        this.personRepo.save(this.person);
        this.projectRepo.save(this.project);

        final List<Relationship> relationships = Arrays.asList(this.relationship, this.relationship0);

        this.relationshipRepo.saveAll(relationships);

        final List<Relationship> foundRelationships = Lists.newArrayList(this.relationshipRepo.findAll());

        foundRelationships.sort(Comparator.comparing(Relationship::getId));
        relationships.sort(Comparator.comparing(Relationship::getId));

        Assertions.assertEquals(foundRelationships, relationships);

        this.relationshipRepo.deleteAll();

        Assertions.assertFalse(this.relationshipRepo.findAll().iterator().hasNext());
    }
}

