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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
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

    @Before
    public void setup() {
        this.repository.deleteAll();
    }

    @After
    public void cleanup() {
        this.repository.deleteAll();
    }

    @Test
    public void testDeleteAll() {
        this.repository.save(this.person);

        Assert.assertTrue(this.repository.existsById(this.person.getId()));

        this.repository.deleteAll();

        Assert.assertFalse(this.repository.existsById(this.person.getId()));
    }

    @Test
    public void testDeleteById() {
        this.repository.save(this.person);
        this.repository.save(this.person0);

        Assert.assertTrue(this.repository.existsById(this.person.getId()));
        Assert.assertTrue(this.repository.existsById(this.person0.getId()));

        this.repository.deleteById(this.person.getId());

        Assert.assertFalse(this.repository.existsById(this.person.getId()));
        Assert.assertTrue(this.repository.existsById(this.person0.getId()));
    }

    @Test
    public void testDelete() {
        this.repository.save(this.person);
        this.repository.save(this.person0);

        Assert.assertTrue(this.repository.existsById(this.person.getId()));
        Assert.assertTrue(this.repository.existsById(this.person0.getId()));

        this.repository.delete(this.person);

        Assert.assertFalse(this.repository.existsById(this.person.getId()));
        Assert.assertTrue(this.repository.existsById(this.person0.getId()));
    }

    @Test
    public void testDeleteAllIds() {
        final List<Person> domains = Arrays.asList(this.person, this.person0);

        this.repository.save(this.person);
        this.repository.save(this.person0);

        this.repository.deleteAll(domains);

        Assert.assertFalse(this.repository.existsById(this.person.getId()));
        Assert.assertFalse(this.repository.existsById(this.person0.getId()));
    }

    @Test
    public void testSave() {
        this.repository.save(this.person);
        this.repository.save(this.person0);

        Assert.assertTrue(this.repository.existsById(this.person.getId()));
        Assert.assertTrue(this.repository.existsById(this.person0.getId()));
    }

    @Test
    public void testSaveAll() {
        final List<Person> domains = Arrays.asList(this.person, this.person0);

        this.repository.saveAll(domains);

        Assert.assertTrue(this.repository.existsById(this.person.getId()));
        Assert.assertTrue(this.repository.existsById(this.person0.getId()));
    }

    @Test
    public void testFindById() {
        this.repository.save(this.person);

        final Person foundPerson = this.repository.findById(this.person.getId()).get();

        Assert.assertNotNull(foundPerson);
        Assert.assertEquals(foundPerson.getId(), this.person.getId());
        Assert.assertEquals(foundPerson.getName(), this.person.getName());

        Assert.assertFalse(this.repository.findById(this.person0.getId()).isPresent());
    }

    @Test
    public void testExistById() {
        Assert.assertFalse(this.repository.existsById(this.person.getId()));

        this.repository.save(this.person);

        Assert.assertTrue(this.repository.existsById(this.person.getId()));
    }

    @Test
    public void testFindAllById() {
        final List<Person> domains = Arrays.asList(this.person, this.person0);
        final List<String> ids = Arrays.asList(this.person.getId(), this.person0.getId());

        this.repository.saveAll(domains);

        final List<Person> foundDomains = (List<Person>) this.repository.findAllById(ids);

        domains.sort((a, b) -> (a.getId().compareTo(b.getId())));
        foundDomains.sort((a, b) -> (a.getId().compareTo(b.getId())));

        Assert.assertArrayEquals(domains.toArray(), foundDomains.toArray());
    }

    @Test
    public void testDomainClassFindAll() {
        final List<Person> domains = Arrays.asList(this.person, this.person0);
        List<Person> foundDomains = (List<Person>) this.repository.findAll(Person.class);

        Assert.assertTrue(foundDomains.isEmpty());

        this.repository.saveAll(domains);

        foundDomains = (List<Person>) this.repository.findAll(Person.class);

        Assert.assertEquals(domains.size(), foundDomains.size());

        domains.sort((a, b) -> (a.getId().compareTo(b.getId())));
        foundDomains.sort((a, b) -> (a.getId().compareTo(b.getId())));

        Assert.assertArrayEquals(domains.toArray(), foundDomains.toArray());
    }

    @Test
    public void testVertexCount() {
        Assert.assertEquals(this.repository.count(), 0);
        Assert.assertEquals(this.repository.edgeCount(), 0);
        Assert.assertEquals(this.repository.vertexCount(), 0);

        this.repository.save(this.person);
        this.repository.save(this.person0);

        Assert.assertEquals(this.repository.count(), 2);
        Assert.assertEquals(this.repository.edgeCount(), 0);
        Assert.assertEquals(this.repository.vertexCount(), this.repository.count());
    }

    @Test
    public void testDeleteAllByType() {
        this.repository.save(this.person);
        this.repository.save(this.person0);

        this.repository.deleteAll(GremlinEntityType.VERTEX);

        Assert.assertFalse(this.repository.findById(this.person.getId()).isPresent());
        Assert.assertFalse(this.repository.findById(this.person0.getId()).isPresent());
    }

    @Test
    public void testDeleteAllByClass() {
        this.repository.save(this.person);
        this.repository.save(this.person0);
        this.projectRepository.save(this.project);

        this.repository.deleteAll(Person.class);

        Assert.assertFalse(this.repository.findById(this.person.getId()).isPresent());
        Assert.assertFalse(this.repository.findById(this.person0.getId()).isPresent());
        Assert.assertTrue(this.projectRepository.findById(this.project.getId()).isPresent());
    }

    @Test
    public void testFindAll() {
        final List<Person> persons = Arrays.asList(this.person, this.person0);
        this.repository.saveAll(persons);

        final List<Person> foundPersons = Lists.newArrayList(this.repository.findAll());

        foundPersons.sort(Comparator.comparing(Person::getId));
        persons.sort(Comparator.comparing(Person::getId));

        Assert.assertEquals(persons, foundPersons);

        this.repository.deleteAll();
        Assert.assertFalse(this.repository.findAll().iterator().hasNext());
    }
}

