// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Contact;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ContactRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class SingleResponseRepositoryIT {

    private static final Contact TEST_CONTACT = new Contact("testId", "faketitle");

    private static final CosmosEntityInformation<Contact, String> entityInformation =
        new CosmosEntityInformation<>(Contact.class);

    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private ContactRepository repository;

    @Autowired
    private CosmosTemplate template;

    @Before
    public void setUp() {
        if (!isSetupDone) {
            staticTemplate = template;
            template.createContainerIfNotExists(entityInformation);
        }
        repository.save(TEST_CONTACT);
        isSetupDone = true;
    }

    @After
    public void cleanup() {
        repository.deleteAll();
    }

    @AfterClass
    public static void afterClassCleanup() {
        staticTemplate.deleteContainer(entityInformation.getContainerName());
    }

    @Test
    public void testShouldFindSingleEntity() {
        final Contact contact = repository.findOneByTitle(TEST_CONTACT.getTitle());

        Assert.assertEquals(TEST_CONTACT, contact);
    }

    @Test
    public void testShouldFindSingleOptionalEntity() {
        final Optional<Contact> contact = repository.findOptionallyByTitle(TEST_CONTACT.getTitle());
        Assert.assertTrue(contact.isPresent());
        Assert.assertEquals(TEST_CONTACT, contact.get());

        Assert.assertFalse(repository.findOptionallyByTitle("not here").isPresent());
    }

    @Test(expected = CosmosAccessException.class)
    public void testShouldFailIfMultipleResultsReturned() {
        repository.save(new Contact("testId2", TEST_CONTACT.getTitle()));

        repository.findOneByTitle(TEST_CONTACT.getTitle());
    }

    @Test
    public void testShouldAllowListAndIterableResponses() {
        final List<Contact> contactList = repository.findByTitle(TEST_CONTACT.getTitle());
        Assert.assertEquals(TEST_CONTACT, contactList.get(0));
        Assert.assertEquals(1, contactList.size());

        final Iterator<Contact> contactIterator = repository.findByLogicId(TEST_CONTACT.getLogicId()).iterator();
        Assert.assertTrue(contactIterator.hasNext());
        Assert.assertEquals(TEST_CONTACT, contactIterator.next());
        Assert.assertFalse(contactIterator.hasNext());
    }

}
