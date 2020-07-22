// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin;

import com.microsoft.spring.data.gremlin.domain.Network;
import com.microsoft.spring.data.gremlin.domain.Person;
import com.microsoft.spring.data.gremlin.domain.Relation;
import com.microsoft.spring.data.gremlin.repository.NetworkRepository;
import com.microsoft.spring.data.gremlin.repository.PersonRepository;
import com.microsoft.spring.data.gremlin.repository.RelationRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RepositoryConfiguration.class)
@Ignore
public class GremlinRepositoryIntegrationTest {
    private static final String PERSON_ID = "89757";
    private static final String PERSON_ID_0 = "0123456789";
    private static final String PERSON_ID_1 = "666666";
    private static final String PERSON_NAME = "person-name";
    private static final String PERSON_NAME_0 = "person-No.0";
    private static final String PERSON_NAME_1 = "person-No.1";
    private static final String PERSON_AGE = "4";
    private static final String PERSON_AGE_0 = "18";
    private static final String PERSON_AGE_1 = "27";

    private static final String RELATION_ID = "2333";
    private static final String RELATION_NAME = "brother";

    private final Person person = new Person(PERSON_ID, PERSON_NAME, PERSON_AGE);
    private final Person person0 = new Person(PERSON_ID_0, PERSON_NAME_0, PERSON_AGE_0);
    private final Person person1 = new Person(PERSON_ID_1, PERSON_NAME_1, PERSON_AGE_1);
    private final Relation relation = new Relation(RELATION_ID, RELATION_NAME, person0, person1);
    private final Network network = new Network();

    @Autowired
    private PersonRepository personRepo;

    @Autowired
    private RelationRepository relationRepo;

    @Autowired
    private NetworkRepository networkRepo;

    @Before
    public void setup() {
        this.networkRepo.deleteAll();
    }

    @After
    public void cleanup() {
        this.networkRepo.deleteAll();
    }

    @Test
    public void testRepository() {
        this.network.getVertexes().add(this.person);
        this.network.getVertexes().add(this.person0);
        this.network.getVertexes().add(this.person1);
        this.network.getEdges().add(this.relation);

        this.networkRepo.save(this.network);

        final Optional<Person> personOptional = this.personRepo.findById(this.person.getId());
        Assert.assertTrue(personOptional.isPresent());

        final Person personFound = personOptional.get();
        Assert.assertEquals(personFound.getId(), this.person.getId());
        Assert.assertEquals(personFound.getName(), this.person.getName());
        Assert.assertEquals(personFound.getAge(), this.person.getAge());

        final Optional<Relation> relationOptional = this.relationRepo.findById(this.relation.getId());
        Assert.assertTrue(relationOptional.isPresent());

        final Relation relationFound = relationOptional.get();

        Assert.assertEquals(relationFound.getId(), this.relation.getId());
        Assert.assertEquals(relationFound.getName(), this.relation.getName());
    }
}

