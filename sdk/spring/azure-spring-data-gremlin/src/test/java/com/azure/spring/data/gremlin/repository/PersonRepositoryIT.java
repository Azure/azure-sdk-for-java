// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository;

import com.azure.spring.data.gremlin.common.GremlinEntityType;
import com.azure.spring.data.gremlin.common.TestConstants;
import com.azure.spring.data.gremlin.common.TestRepositoryConfiguration;
import com.azure.spring.data.gremlin.common.domain.Person;
import com.azure.spring.data.gremlin.common.domain.Project;
import com.azure.spring.data.gremlin.common.repository.PersonRepository;
import com.azure.spring.data.gremlin.common.repository.ProjectRepository;
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
public class PersonRepositoryIT {

    private final Person person = new Person(TestConstants.VERTEX_PERSON_ID, TestConstants.VERTEX_PERSON_NAME);
    private final Person person0 = new Person(TestConstants.VERTEX_PERSON_0_ID, TestConstants.VERTEX_PERSON_0_NAME);
    private final Project project = new Project(TestConstants.VERTEX_PROJECT_ID, TestConstants.VERTEX_PROJECT_NAME,
            TestConstants.VERTEX_PROJECT_URI);

    @Autowired
    private PersonRepository repository;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    public void setup() {
        this.repository.deleteAll();
    }

    @AfterEach
    public void cleanup() {
        this.repository.deleteAll();
    }

    @Test
    public void testDeleteAll() {
        this.repository.save(this.person);

        Assertions.assertTrue(this.repository.existsById(this.person.getId()));

        this.repository.deleteAll();

        Assertions.assertFalse(this.repository.existsById(this.person.getId()));
    }

    @Test
    public void testDeleteById() {
        this.repository.save(this.person);
        this.repository.save(this.person0);

        Assertions.assertTrue(this.repository.existsById(this.person.getId()));
        Assertions.assertTrue(this.repository.existsById(this.person0.getId()));

        this.repository.deleteById(this.person.getId());

        Assertions.assertFalse(this.repository.existsById(this.person.getId()));
        Assertions.assertTrue(this.repository.existsById(this.person0.getId()));
    }

    @Test
    public void testDelete() {
        this.repository.save(this.person);
        this.repository.save(this.person0);

        Assertions.assertTrue(this.repository.existsById(this.person.getId()));
        Assertions.assertTrue(this.repository.existsById(this.person0.getId()));

        this.repository.delete(this.person);

        Assertions.assertFalse(this.repository.existsById(this.person.getId()));
        Assertions.assertTrue(this.repository.existsById(this.person0.getId()));
    }

    @Test
    public void testDeleteAllIds() {
        final List<Person> domains = Arrays.asList(this.person, this.person0);

        this.repository.save(this.person);
        this.repository.save(this.person0);

        this.repository.deleteAll(domains);

        Assertions.assertFalse(this.repository.existsById(this.person.getId()));
        Assertions.assertFalse(this.repository.existsById(this.person0.getId()));
    }

    @Test
    public void testSave() {
        this.repository.save(this.person);
        this.repository.save(this.person0);

        Assertions.assertTrue(this.repository.existsById(this.person.getId()));
        Assertions.assertTrue(this.repository.existsById(this.person0.getId()));
    }

    @Test
    public void testSaveAll() {
        final List<Person> domains = Arrays.asList(this.person, this.person0);

        this.repository.saveAll(domains);

        Assertions.assertTrue(this.repository.existsById(this.person.getId()));
        Assertions.assertTrue(this.repository.existsById(this.person0.getId()));
    }

    @Test
    public void testFindById() {
        this.repository.save(this.person);

        final Person foundPerson = this.repository.findById(this.person.getId()).get();

        Assertions.assertNotNull(foundPerson);
        Assertions.assertEquals(foundPerson.getId(), this.person.getId());
        Assertions.assertEquals(foundPerson.getName(), this.person.getName());

        Assertions.assertFalse(this.repository.findById(this.person0.getId()).isPresent());
    }

    @Test
    public void testExistById() {
        Assertions.assertFalse(this.repository.existsById(this.person.getId()));

        this.repository.save(this.person);

        Assertions.assertTrue(this.repository.existsById(this.person.getId()));
    }

    @Test
    public void testFindAllById() {
        final List<Person> domains = Arrays.asList(this.person, this.person0);
        final List<String> ids = Arrays.asList(this.person.getId(), this.person0.getId());

        this.repository.saveAll(domains);

        final List<Person> foundDomains = (List<Person>) this.repository.findAllById(ids);

        domains.sort((a, b) -> (a.getId().compareTo(b.getId())));
        foundDomains.sort((a, b) -> (a.getId().compareTo(b.getId())));

        Assertions.assertArrayEquals(domains.toArray(), foundDomains.toArray());
    }

    @Test
    public void testDomainClassFindAll() {
        final List<Person> domains = Arrays.asList(this.person, this.person0);
        List<Person> foundDomains = (List<Person>) this.repository.findAll(Person.class);

        Assertions.assertTrue(foundDomains.isEmpty());

        this.repository.saveAll(domains);

        foundDomains = (List<Person>) this.repository.findAll(Person.class);

        Assertions.assertEquals(domains.size(), foundDomains.size());

        domains.sort((a, b) -> (a.getId().compareTo(b.getId())));
        foundDomains.sort((a, b) -> (a.getId().compareTo(b.getId())));

        Assertions.assertArrayEquals(domains.toArray(), foundDomains.toArray());
    }

    @Test
    public void testVertexCount() {
        Assertions.assertEquals(this.repository.count(), 0);
        Assertions.assertEquals(this.repository.edgeCount(), 0);
        Assertions.assertEquals(this.repository.vertexCount(), 0);

        this.repository.save(this.person);
        this.repository.save(this.person0);

        Assertions.assertEquals(this.repository.count(), 2);
        Assertions.assertEquals(this.repository.edgeCount(), 0);
        Assertions.assertEquals(this.repository.vertexCount(), this.repository.count());
    }

    @Test
    public void testDeleteAllByType() {
        this.repository.save(this.person);
        this.repository.save(this.person0);

        this.repository.deleteAll(GremlinEntityType.VERTEX);

        Assertions.assertFalse(this.repository.findById(this.person.getId()).isPresent());
        Assertions.assertFalse(this.repository.findById(this.person0.getId()).isPresent());
    }

    @Test
    public void testDeleteAllByClass() {
        this.repository.save(this.person);
        this.repository.save(this.person0);
        this.projectRepository.save(this.project);

        this.repository.deleteAll(Person.class);

        Assertions.assertFalse(this.repository.findById(this.person.getId()).isPresent());
        Assertions.assertFalse(this.repository.findById(this.person0.getId()).isPresent());
        Assertions.assertTrue(this.projectRepository.findById(this.project.getId()).isPresent());
    }

    @Test
    public void testFindAll() {
        final List<Person> persons = Arrays.asList(this.person, this.person0);
        this.repository.saveAll(persons);

        final List<Person> foundPersons = Lists.newArrayList(this.repository.findAll());

        foundPersons.sort(Comparator.comparing(Person::getId));
        persons.sort(Comparator.comparing(Person::getId));

        Assertions.assertEquals(persons, foundPersons);

        this.repository.deleteAll();
        Assertions.assertFalse(this.repository.findAll().iterator().hasNext());
    }
}

