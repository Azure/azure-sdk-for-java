// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.core;

import com.azure.data.cosmos.PartitionKey;
import com.microsoft.azure.spring.data.cosmosdb.CosmosDbFactory;
import com.microsoft.azure.spring.data.cosmosdb.config.CosmosDBConfig;
import com.microsoft.azure.spring.data.cosmosdb.core.convert.MappingCosmosConverter;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.CosmosMappingContext;
import com.microsoft.azure.spring.data.cosmosdb.core.query.CosmosPageRequest;
import com.microsoft.azure.spring.data.cosmosdb.core.query.Criteria;
import com.microsoft.azure.spring.data.cosmosdb.core.query.CriteriaType;
import com.microsoft.azure.spring.data.cosmosdb.core.query.DocumentQuery;
import com.microsoft.azure.spring.data.cosmosdb.domain.PartitionPerson;
import com.microsoft.azure.spring.data.cosmosdb.repository.TestRepositoryConfig;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.CosmosEntityInformation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.microsoft.azure.spring.data.cosmosdb.common.PageTestUtils.validateLastPage;
import static com.microsoft.azure.spring.data.cosmosdb.common.PageTestUtils.validateNonLastPage;
import static com.microsoft.azure.spring.data.cosmosdb.common.TestConstants.*;
import static com.microsoft.azure.spring.data.cosmosdb.core.query.CriteriaType.IS_EQUAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class CosmosTemplatePartitionIT {
    private static final PartitionPerson TEST_PERSON = new PartitionPerson(ID_1, FIRST_NAME, LAST_NAME,
            HOBBIES, ADDRESSES);

    private static final PartitionPerson TEST_PERSON_2 = new PartitionPerson(ID_2, NEW_FIRST_NAME,
            TEST_PERSON.getLastName(), HOBBIES, ADDRESSES);

    private static CosmosTemplate cosmosTemplate;
    private static String containerName;
    private static CosmosEntityInformation<PartitionPerson, String> personInfo;
    private static boolean initialized;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CosmosDBConfig dbConfig;

    @Before
    public void setUp() throws ClassNotFoundException {
        if (!initialized) {
            final CosmosDbFactory cosmosDbFactory = new CosmosDbFactory(dbConfig);
            final CosmosMappingContext mappingContext = new CosmosMappingContext();

            personInfo = new CosmosEntityInformation<>(PartitionPerson.class);
            mappingContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));

            final MappingCosmosConverter dbConverter = new MappingCosmosConverter(mappingContext, null);

            cosmosTemplate = new CosmosTemplate(cosmosDbFactory, dbConverter, dbConfig.getDatabase());
            containerName = personInfo.getContainerName();

            cosmosTemplate.createContainerIfNotExists(personInfo);
            initialized = true;
        }

        cosmosTemplate.insert(PartitionPerson.class.getSimpleName(), TEST_PERSON,
                new PartitionKey(TEST_PERSON.getLastName()));
    }

    @After
    public void cleanup() {
        cosmosTemplate.deleteAll(personInfo.getContainerName(), PartitionPerson.class);
    }

    @AfterClass
    public static void afterClassCleanup() {
        cosmosTemplate.deleteContainer(personInfo.getContainerName());
    }

    @Test
    public void testFindWithPartition() {
        Criteria criteria = Criteria.getInstance(IS_EQUAL, PROPERTY_LAST_NAME, Arrays.asList(LAST_NAME));
        DocumentQuery query = new DocumentQuery(criteria);
        List<PartitionPerson> result = cosmosTemplate.find(query, PartitionPerson.class,
                PartitionPerson.class.getSimpleName());

        assertThat(result.size()).isEqualTo(1);
        assertEquals(TEST_PERSON, result.get(0));

        criteria = Criteria.getInstance(IS_EQUAL, PROPERTY_ID, Arrays.asList(ID_1));
        query = new DocumentQuery(criteria);
        result = cosmosTemplate.find(query, PartitionPerson.class, PartitionPerson.class.getSimpleName());

        assertThat(result.size()).isEqualTo(1);
        assertEquals(TEST_PERSON, result.get(0));
    }


    @Test
    public void testFindByIdWithPartition() {
        final PartitionPerson partitionPersonById = cosmosTemplate.findById(TEST_PERSON.getId(),
            PartitionPerson.class,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON)));

        assertEquals(TEST_PERSON, partitionPersonById);
    }

    @Test
    public void testFindByNonExistIdWithPartition() {
        final Criteria criteria = Criteria.getInstance(IS_EQUAL, PROPERTY_ID, Arrays.asList(NOT_EXIST_ID));
        final DocumentQuery query = new DocumentQuery(criteria);

        final List<PartitionPerson> result = cosmosTemplate.find(query, PartitionPerson.class,
                PartitionPerson.class.getSimpleName());
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void testUpsertNewDocumentPartition() {
        final String firstName = NEW_FIRST_NAME + "_" + UUID.randomUUID().toString();
        final PartitionPerson newPerson = new PartitionPerson(TEST_PERSON.getId(),
            firstName, NEW_LAST_NAME,
            null, null);

        final String partitionKeyValue = newPerson.getLastName();
        final PartitionPerson partitionPerson =
            cosmosTemplate.upsertAndReturnEntity(PartitionPerson.class.getSimpleName(), newPerson
                , new PartitionKey(partitionKeyValue));

        final List<PartitionPerson> result = cosmosTemplate.findAll(PartitionPerson.class);

        assertThat(result.size()).isEqualTo(2);
        assertThat(partitionPerson.getFirstName()).isEqualTo(firstName);
    }

    @Test
    public void testUpdatePartition() {
        final PartitionPerson updated = new PartitionPerson(TEST_PERSON.getId(), UPDATED_FIRST_NAME,
                TEST_PERSON.getLastName(), TEST_PERSON.getHobbies(), TEST_PERSON.getShippingAddresses());
        final PartitionPerson partitionPerson =
            cosmosTemplate.upsertAndReturnEntity(PartitionPerson.class.getSimpleName(), updated,
                new PartitionKey(updated.getLastName()));

        assertEquals(partitionPerson, updated);
    }

    @Test
    public void testDeleteByIdPartition() {
        // insert new document with same partition key
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getLastName()));

        final List<PartitionPerson> inserted = cosmosTemplate.findAll(PartitionPerson.class);
        assertThat(inserted.size()).isEqualTo(2);
        assertThat(inserted.get(0).getLastName()).isEqualTo(TEST_PERSON.getLastName());
        assertThat(inserted.get(1).getLastName()).isEqualTo(TEST_PERSON.getLastName());

        cosmosTemplate.deleteById(PartitionPerson.class.getSimpleName(),
                TEST_PERSON.getId(), new PartitionKey(TEST_PERSON.getLastName()));

        final List<PartitionPerson> result = cosmosTemplate.findAll(PartitionPerson.class);
        assertThat(result.size()).isEqualTo(1);
        assertEquals(result.get(0), TEST_PERSON_2);
    }

    @Test
    public void testCountForPartitionedCollection() {
        final long prevCount = cosmosTemplate.count(containerName);
        assertThat(prevCount).isEqualTo(1);

        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getLastName()));

        final long newCount = cosmosTemplate.count(containerName);
        assertThat(newCount).isEqualTo(2);
    }

    @Test
    public void testCountForPartitionedCollectionByQuery() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getLastName()));

        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
                Arrays.asList(TEST_PERSON_2.getFirstName()));
        final DocumentQuery query = new DocumentQuery(criteria);

        final long count = cosmosTemplate.count(query, PartitionPerson.class, containerName);
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testNonExistFieldValue() {
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
                Arrays.asList("non-exist-first-name"));
        final DocumentQuery query = new DocumentQuery(criteria);

        final long count = cosmosTemplate.count(query, PartitionPerson.class, containerName);
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void testPartitionedFindAllPageableMultiPages() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getLastName()));

        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_1, null);
        final Page<PartitionPerson> page1 = cosmosTemplate.findAll(pageRequest, PartitionPerson.class, containerName);

        assertThat(page1.getContent().size()).isEqualTo(PAGE_SIZE_1);
        validateNonLastPage(page1, PAGE_SIZE_1);

        final Page<PartitionPerson> page2 = cosmosTemplate.findAll(page1.getPageable(),
                PartitionPerson.class, containerName);
        assertThat(page2.getContent().size()).isEqualTo(1);
        validateLastPage(page2, PAGE_SIZE_1);
    }

    @Test
    public void testPartitionedPaginationQuery() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getLastName()));

        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
                Arrays.asList(FIRST_NAME));
        final PageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_2, null);
        final DocumentQuery query = new DocumentQuery(criteria).with(pageRequest);

        final Page<PartitionPerson> page = cosmosTemplate.paginationQuery(query, PartitionPerson.class, containerName);
        assertThat(page.getContent().size()).isEqualTo(1);
        validateLastPage(page, page.getContent().size());
    }
}
