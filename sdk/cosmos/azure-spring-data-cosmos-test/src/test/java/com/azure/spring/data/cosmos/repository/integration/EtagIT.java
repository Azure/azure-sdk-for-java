// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.PersonWithEtag;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.PersonWithEtagRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.azure.spring.data.cosmos.common.TestConstants.ADDRESSES;
import static com.azure.spring.data.cosmos.common.TestConstants.FIRST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.HOBBIES;
import static com.azure.spring.data.cosmos.common.TestConstants.LAST_NAME;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class EtagIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    CosmosTemplate template;
    @Autowired
    PersonWithEtagRepository personWithEtagRepository;

    @Before
    public void setup() {
        collectionManager.ensureContainersCreatedAndEmpty(template, PersonWithEtag.class);
    }

    private static PersonWithEtag createPersonWithEtag() {
        return new PersonWithEtag(UUID.randomUUID().toString(), FIRST_NAME, LAST_NAME, HOBBIES, ADDRESSES);
    }

    @Test
    public void testCrudOperationsShouldApplyEtag() {
        final PersonWithEtag insertedPersonWithEtag = personWithEtagRepository.save(createPersonWithEtag());
        Assert.assertNotNull(insertedPersonWithEtag.getEtag());

        insertedPersonWithEtag.setFirstName(LAST_NAME);
        final PersonWithEtag updatedPersonWithEtag = personWithEtagRepository.save(insertedPersonWithEtag);
        Assert.assertNotNull(updatedPersonWithEtag.getEtag());
        Assert.assertNotEquals(insertedPersonWithEtag.getEtag(), updatedPersonWithEtag.getEtag());

        final Optional<PersonWithEtag> foundPersonWithEtag = personWithEtagRepository.findById(insertedPersonWithEtag.getId());
        Assert.assertTrue(foundPersonWithEtag.isPresent());
        Assert.assertNotNull(foundPersonWithEtag.get().getEtag());
        Assert.assertEquals(updatedPersonWithEtag.getEtag(), foundPersonWithEtag.get().getEtag());
    }

    @Test
    public void testCrudListOperationsShouldApplyEtag() {
        final List<PersonWithEtag> people = new ArrayList<>();
        people.add(createPersonWithEtag());
        people.add(createPersonWithEtag());

        final List<PersonWithEtag> insertedPeople = toList(personWithEtagRepository.saveAll(people));
        insertedPeople.forEach(person -> Assert.assertNotNull(person.getEtag()));

        insertedPeople.forEach(person -> person.setFirstName(LAST_NAME));
        final List<PersonWithEtag> updatedPeople = toList(personWithEtagRepository.saveAll(insertedPeople));
        for (int i = 0; i < updatedPeople.size(); i++) {
            PersonWithEtag insertedPersonWithEtag = insertedPeople.get(i);
            PersonWithEtag updatedPersonWithEtag = updatedPeople.get(i);
            Assert.assertEquals(insertedPersonWithEtag.getId(), updatedPersonWithEtag.getId());
            Assert.assertNotNull(updatedPersonWithEtag.getEtag());
            Assert.assertNotEquals(insertedPersonWithEtag.getEtag(), updatedPersonWithEtag.getEtag());
        }

        final List<String> peopleIds = updatedPeople.stream()
            .map(PersonWithEtag::getId)
            .collect(Collectors.toList());
        final List<PersonWithEtag> foundPeople = toList(personWithEtagRepository.findAllById(peopleIds));
        for (int i = 0; i < foundPeople.size(); i++) {
            PersonWithEtag updatedPersonWithEtag = updatedPeople.get(i);
            PersonWithEtag foundPersonWithEtag = foundPeople.get(i);
            Assert.assertNotNull(foundPersonWithEtag.getEtag());
            Assert.assertEquals(updatedPersonWithEtag.getEtag(), foundPersonWithEtag.getEtag());
        }
    }

    private List<PersonWithEtag> toList(Iterable<PersonWithEtag> people) {
        return StreamSupport.stream(people.spliterator(), false)
            .collect(Collectors.toList());
    }

    @Test
    public void testShouldFailIfEtagDoesNotMatch() {
        PersonWithEtag insertedPersonWithEtag = personWithEtagRepository.save(createPersonWithEtag());
        insertedPersonWithEtag.setFirstName(LAST_NAME);

        PersonWithEtag updatedPersonWithEtag = personWithEtagRepository.save(insertedPersonWithEtag);
        updatedPersonWithEtag.setEtag(insertedPersonWithEtag.getEtag());

        try {
            personWithEtagRepository.save(updatedPersonWithEtag);
            Assert.fail();
        } catch (CosmosAccessException ex) {
        }

        try {
            personWithEtagRepository.delete(updatedPersonWithEtag);
            Assert.fail();
        } catch (CosmosAccessException ex) {
        }
    }

}
