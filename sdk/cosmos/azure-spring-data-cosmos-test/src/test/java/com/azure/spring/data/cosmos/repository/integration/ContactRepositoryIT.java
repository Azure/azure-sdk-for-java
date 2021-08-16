// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Contact;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ContactRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ContactRepositoryIT {

    private static final Integer INT_VALUE_1 = 25;
    private static final Integer INT_VALUE_2 = 32;
    private static final Integer INT_VALUE_3 = 43;
    private static final Contact TEST_CONTACT1 = new Contact("testId", "faketitle", INT_VALUE_1, true);
    private static final Contact TEST_CONTACT2 = new Contact("testId2", "faketitle2", INT_VALUE_2, false);
    private static final Contact TEST_CONTACT3 = new Contact("testId3", "faketitle3", INT_VALUE_1, false);
    private static final Contact TEST_CONTACT4 = new Contact("testId4", "faketitle4", INT_VALUE_3, true);
    private static final Contact TEST_CONTACT5 = new Contact("testId5", "faketitle3", INT_VALUE_3, true);

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    ContactRepository repository;

    @Autowired
    private CosmosTemplate template;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Contact.class);
        repository.save(TEST_CONTACT1);
        repository.save(TEST_CONTACT2);
        repository.save(TEST_CONTACT3);
        repository.save(TEST_CONTACT4);
        repository.save(TEST_CONTACT5);
    }

    @Test
    public void testFindAll() {
        final List<Contact> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(5);
        Assert.assertEquals(Arrays.asList(TEST_CONTACT1, TEST_CONTACT2, TEST_CONTACT3, TEST_CONTACT4,
                                          TEST_CONTACT5), result);

        final Contact contact = repository.findById(TEST_CONTACT1.getLogicId()).get();

        assertThat(contact.getLogicId()).isEqualTo(TEST_CONTACT1.getLogicId());
        assertThat(contact.getTitle()).isEqualTo(TEST_CONTACT1.getTitle());
    }

    @Test
    public void testCountAndDeleteByID() {
        final Contact contact2 = new Contact("newid", "newtitle");
        repository.save(contact2);
        final List<Contact> all = TestUtils.toList(repository.findAll());
        assertThat(all.size()).isEqualTo(6);

        long count = repository.count();
        assertThat(count).isEqualTo(6);

        repository.deleteById(contact2.getLogicId());

        final List<Contact> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(5);
        assertThat(result.get(0).getLogicId()).isEqualTo(TEST_CONTACT1.getLogicId());
        assertThat(result.get(0).getTitle()).isEqualTo(TEST_CONTACT1.getTitle());

        count = repository.count();
        assertThat(count).isEqualTo(5);
    }

    @Test
    public void testCountAndDeleteEntity() {
        final Contact contact2 = new Contact("newid", "newtitle");
        repository.save(contact2);
        final List<Contact> all = TestUtils.toList(repository.findAll());
        assertThat(all.size()).isEqualTo(6);

        repository.delete(contact2);

        final List<Contact> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(5);
        Assert.assertEquals(Arrays.asList(TEST_CONTACT1, TEST_CONTACT2, TEST_CONTACT3, TEST_CONTACT4,
                                          TEST_CONTACT5), result);
        assertThat(result.get(0).getLogicId()).isEqualTo(TEST_CONTACT1.getLogicId());
        assertThat(result.get(0).getTitle()).isEqualTo(TEST_CONTACT1.getTitle());
    }

    @Test
    public void testUpdateEntity() {
        final Contact updatedContact = new Contact(TEST_CONTACT1.getLogicId(), "updated");

        final Contact savedContact = repository.save(updatedContact);

        //  Test save operation return saved entity
        assertThat(savedContact.getLogicId()).isEqualTo(updatedContact.getLogicId());
        assertThat(savedContact.getTitle()).isEqualTo(updatedContact.getTitle());

        final Contact contact = repository.findById(TEST_CONTACT1.getLogicId()).get();

        assertThat(contact.getLogicId()).isEqualTo(updatedContact.getLogicId());
        assertThat(contact.getTitle()).isEqualTo(updatedContact.getTitle());
    }

    @Test
    public void testBatchOperations() {

        final Contact contact1 = new Contact("newid1", "newtitle");
        final Contact contact2 = new Contact("newid2", "newtitle");
        final ArrayList<Contact> contacts = new ArrayList<Contact>();
        contacts.add(contact1);
        contacts.add(contact2);
        final Iterable<Contact> savedContacts = repository.saveAll(contacts);

        final AtomicInteger savedCount = new AtomicInteger();
        savedContacts.forEach(se -> {
            savedCount.incrementAndGet();
            assertThat(contacts.contains(se)).isTrue();
        });

        assertThat(savedCount.get()).isEqualTo(contacts.size());

        final ArrayList<String> ids = new ArrayList<String>();
        ids.add(contact1.getLogicId());
        ids.add(contact2.getLogicId());
        final List<Contact> result = Lists.newArrayList(repository.findAllById(ids));

        assertThat(result.size()).isEqualTo(2);

        repository.deleteAll(contacts);

        final List<Contact> result2 = Lists.newArrayList(repository.findAllById(ids));
        assertThat(result2.size()).isEqualTo(0);
    }

    @Test
    public void testCustomQuery() {
        final List<Contact> result = TestUtils.toList(repository.findByTitle(TEST_CONTACT1.getTitle()));

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getLogicId()).isEqualTo(TEST_CONTACT1.getLogicId());
        assertThat(result.get(0).getTitle()).isEqualTo(TEST_CONTACT1.getTitle());

    }

    @Test
    @Ignore //  TODO(kuthapar): v3 doesn't support creation of items without id.
    public void testNullIdContact() {
        final Contact nullIdContact = new Contact(null, "testTitile");
        final Contact savedContact = repository.save(nullIdContact);

        Assert.assertNotNull(savedContact.getLogicId());
        Assert.assertEquals(nullIdContact.getTitle(), savedContact.getTitle());
    }

    @Test
    public void testFindById() {
        final Optional<Contact> optional = repository.findById(TEST_CONTACT1.getLogicId());

        Assert.assertTrue(optional.isPresent());
        Assert.assertEquals(TEST_CONTACT1, optional.get());
        Assert.assertFalse(repository.findById("").isPresent());
    }

    @Test
    public void testFindByIdNotFound() {
        final Optional<Contact> optional = repository.findById("unknown-id");

        Assert.assertFalse(optional.isPresent());
    }

    @Test
    public void testShouldFindSingleEntity() {
        final Contact contact = repository.findOneByTitle(TEST_CONTACT1.getTitle());

        Assert.assertEquals(TEST_CONTACT1, contact);
    }

    @Test
    public void testShouldFindSingleOptionalEntity() {
        final Optional<Contact> contact = repository.findOptionallyByTitle(TEST_CONTACT1.getTitle());
        Assert.assertTrue(contact.isPresent());
        Assert.assertEquals(TEST_CONTACT1, contact.get());

        Assert.assertFalse(repository.findOptionallyByTitle("not here").isPresent());
    }

    @Test(expected = CosmosAccessException.class)
    public void testShouldFailIfMultipleResultsReturned() {
        repository.save(new Contact("testId2", TEST_CONTACT1.getTitle()));

        repository.findOneByTitle(TEST_CONTACT1.getTitle());
    }

    @Test
    public void testShouldAllowListAndIterableResponses() {
        final List<Contact> contactList = TestUtils.toList(repository.findByTitle(TEST_CONTACT1.getTitle()));
        Assert.assertEquals(TEST_CONTACT1, contactList.get(0));
        Assert.assertEquals(1, contactList.size());

        final Iterator<Contact> contactIterator = repository.findByLogicId(TEST_CONTACT1.getLogicId()).iterator();
        Assert.assertTrue(contactIterator.hasNext());
        Assert.assertEquals(TEST_CONTACT1, contactIterator.next());
        Assert.assertFalse(contactIterator.hasNext());
    }

    @Test
    public void testAnnotatedQueries() {
        List<Contact> valueContacts = repository.getContactsByTitleAndValue(43, TEST_CONTACT5.getTitle());
        Assert.assertEquals(1, valueContacts.size());
        Assert.assertEquals(TEST_CONTACT5, valueContacts.get(0));

        List<Contact> contactsWithOffset = repository.getContactsWithOffsetLimit(1, 2);
        Assert.assertEquals(2, contactsWithOffset.size());
        Assert.assertEquals(TEST_CONTACT2, contactsWithOffset.get(0));
        Assert.assertEquals(TEST_CONTACT3, contactsWithOffset.get(1));

        List<ObjectNode> groupByContacts = repository.selectGroupBy();
        Assert.assertEquals(3, groupByContacts.size());
    }

    @Test
    public void testAnnotatedQueriesDistinctIntValue() {
        List<Integer> valueContacts = repository.findDistinctIntValueValues();
        assertThat(valueContacts).isEqualTo(Arrays.asList(INT_VALUE_1, INT_VALUE_2, INT_VALUE_3));
    }

    @Test
    public void testAnnotatedQueriesDistinctStatus() {
        List<Boolean> statusContacts = repository.findDistinctStatusValues();
        assertThat(statusContacts).isEqualTo(Arrays.asList(Boolean.TRUE, Boolean.FALSE));
    }
}
